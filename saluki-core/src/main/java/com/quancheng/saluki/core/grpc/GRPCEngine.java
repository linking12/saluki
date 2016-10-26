package com.quancheng.saluki.core.grpc;

import java.util.Map;

import com.quancheng.saluki.core.common.SalukiConstants;
import com.quancheng.saluki.core.common.SalukiURL;
import com.quancheng.saluki.core.grpc.client.GrpcClientContext;
import com.quancheng.saluki.core.grpc.client.GrpcProtocolClient;
import com.quancheng.saluki.core.grpc.interceptor.HeaderClientInterceptor;
import com.quancheng.saluki.core.grpc.interceptor.HeaderServerInterceptor;
import com.quancheng.saluki.core.grpc.server.GrpcServerContext;
import com.quancheng.saluki.core.registry.Registry;
import com.quancheng.saluki.core.registry.RegistryProvider;

import io.grpc.Channel;
import io.grpc.ClientInterceptors;
import io.grpc.LoadBalancer;
import io.grpc.ManagedChannelBuilder;
import io.grpc.ServerInterceptors;
import io.grpc.ServerServiceDefinition;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.netty.NettyServerBuilder;

public class GRPCEngine {

    private final SalukiURL registryUrl;

    private final Registry  registry;

    public GRPCEngine(SalukiURL registryUrl){
        this.registryUrl = registryUrl;
        this.registry = RegistryProvider.asFactory().newRegistry(registryUrl);
    }

    public Object getProxy(SalukiURL refUrl) throws Exception {
        boolean localProcess = refUrl.getParameter(SalukiConstants.GRPC_IN_LOCAL_PROCESS, Boolean.FALSE);
        GrpcProtocolClient.ChannelCall call = new GrpcProtocolClient.ChannelCall() {

            @Override
            public Channel getChannel() {
                Channel channel;
                if (localProcess) {
                    channel = InProcessChannelBuilder.forName(SalukiConstants.GRPC_IN_LOCAL_PROCESS).build();
                } else {
                    channel = ManagedChannelBuilder.forTarget(registryUrl.toJavaURI().toString())//
                                                   .nameResolverFactory(new SalukiNameResolverProvider(refUrl))//
                                                   .loadBalancerFactory(buildLoadBalanceFactory())//
                                                   .usePlaintext(true)//
                                                   .build();//
                }
                return ClientInterceptors.intercept(channel, new HeaderClientInterceptor());
            }

        };
        GrpcClientContext context = new GrpcClientContext(refUrl, call);
        return context.getGrpcClient();
    }

    private LoadBalancer.Factory buildLoadBalanceFactory() {
        return SalukiRoundRobinLoadBalanceFactory.getInstance();
    }

    public SalukiServer getServer(Map<SalukiURL, Object> providerUrls, int port) throws Exception {
        final NettyServerBuilder remoteServer = NettyServerBuilder.forPort(port);
        final InProcessServerBuilder injvmServer = InProcessServerBuilder.forName(SalukiConstants.GRPC_IN_LOCAL_PROCESS);
        for (Map.Entry<SalukiURL, Object> entry : providerUrls.entrySet()) {
            SalukiURL providerUrl = entry.getKey();
            Object protocolImpl = entry.getValue();
            GrpcServerContext context = new GrpcServerContext(providerUrl, protocolImpl);
            ServerServiceDefinition serviceDefinition = ServerInterceptors.intercept(context.getServerDefintion(),
                                                                                     new HeaderServerInterceptor());
            remoteServer.addService(serviceDefinition);
            injvmServer.addService(serviceDefinition);
            String registryPort = System.getProperty(SalukiConstants.REGISTRY_PORT,
                                                     Integer.valueOf(providerUrl.getPort()).toString());
            providerUrl = providerUrl.setPort(Integer.valueOf(registryPort).intValue());
            registry.register(providerUrl);
        }
        return new SalukiServer(injvmServer.build().start(), remoteServer.build().start());
    }

}
