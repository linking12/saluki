package com.quancheng.saluki.core.grpc;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.protobuf.GeneratedMessageV3;
import com.quancheng.saluki.core.common.SalukiConstants;
import com.quancheng.saluki.core.common.SalukiURL;
import com.quancheng.saluki.core.registry.Registry;
import com.quancheng.saluki.core.registry.RegistryProvider;
import com.quancheng.saluki.core.service.GenericService;
import com.quancheng.saluki.core.utils.ReflectUtil;

import io.grpc.Attributes;
import io.grpc.BindableService;
import io.grpc.Channel;
import io.grpc.LoadBalancer;
import io.grpc.ManagedChannelBuilder;
import io.grpc.MethodDescriptor;
import io.grpc.NameResolver;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerServiceDefinition;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.stub.ServerCalls;
import io.grpc.stub.ServerCalls.UnaryMethod;
import io.grpc.stub.StreamObserver;
import io.grpc.util.RoundRobinLoadBalancerFactory;

public class GRPCEngineImpl implements GRPCEngine {

    private static final Logger          log = LoggerFactory.getLogger(GRPCEngine.class);

    private final SalukiURL              registryUrl;

    private final Registry               registry;

    private final Cache<String, Channel> channelCache;

    public GRPCEngineImpl(SalukiURL registryUrl){
        this.registryUrl = registryUrl;
        this.registry = RegistryProvider.asFactory().newRegistry(registryUrl);
        channelCache = CacheBuilder.newBuilder().maximumSize(1000).build();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public <T> ProtocolProxy<T> getProxy(SalukiURL refUrl) throws Exception {
        boolean isLocal = refUrl.getParameter(SalukiConstants.GRPC_IN_LOCAL_PROCESS, false);
        /**
         * 使用回调函数来实现Channel的延迟加载:如果是原生的stub，延迟加载是不起作用的，只有使用代理的情况下，才起作用
         */
        ProtocolProxy.ChannelCallable channelCallable = new ProtocolProxy.ChannelCallable() {

            @Override
            public Channel getGrpcChannel(String serviceInterface) {
                try {
                    return channelCache.get(serviceInterface, new Callable<Channel>() {

                        @Override
                        public Channel call() throws Exception {
                            Channel channel;
                            if (isLocal) {
                                channel = InProcessChannelBuilder.forName(SalukiConstants.GRPC_IN_LOCAL_PROCESS).build();
                            } else {
                                channel = ManagedChannelBuilder.forTarget(registryUrl.toJavaURI().toString())//
                                                               .nameResolverFactory(buildNameResolverFactory(refUrl))//
                                                               .loadBalancerFactory(buildLoadBalanceFactory()).usePlaintext(true).build();//
                            }
                            return channel;
                        }

                    });
                } catch (ExecutionException e) {
                    throw new IllegalStateException("fetch Channel failed", e);
                }
            }

        };
        int rpcType = refUrl.getParameter(SalukiConstants.RPCTYPE_KEY, SalukiConstants.RPCTYPE_ASYNC);
        int rpcTimeOut = refUrl.getParameter(SalukiConstants.RPCTIMEOUT_KEY, SalukiConstants.DEFAULT_TIMEOUT);
        boolean isGeneric = refUrl.getParameter(SalukiConstants.GENERIC_KEY, SalukiConstants.DEFAULT_GENERIC);
        return new ProtocolProxy(refUrl.getServiceInterface(), channelCallable, rpcTimeOut, rpcType, isGeneric);
    }

    private NameResolver.Factory buildNameResolverFactory(SalukiURL refUrl) {
        final Attributes attributesParams = Attributes.newBuilder().set(SalukiConstants.PARAMS_DEFAULT_SUBCRIBE,
                                                                        refUrl).build();

        return new NameResolver.Factory() {

            @Override
            public NameResolver newNameResolver(URI targetUri, Attributes params) {
                Attributes allParams = Attributes.newBuilder().setAll(attributesParams).setAll(params).build();
                return new SalukiNameResolver(targetUri, allParams);
            }

            @Override
            public String getDefaultScheme() {
                return "consul";
            }

        };
    }

    private LoadBalancer.Factory buildLoadBalanceFactory() {
        return RoundRobinLoadBalancerFactory.getInstance();
    }

    @Override
    public Server getServer(Map<SalukiURL, Object> providerUrls, int port) throws Exception {
        final ServerBuilder<?> serverBuilder = ServerBuilder.forPort(port);
        for (Map.Entry<SalukiURL, Object> entry : providerUrls.entrySet()) {
            SalukiURL providerUrl = entry.getKey();
            Object protocolImpl = entry.getValue();
            // 如果是原生的grpc stub类
            if (protocolImpl instanceof BindableService) {
                BindableService bindableService = (BindableService) protocolImpl;
                ServerServiceDefinition serviceDefinition = bindableService.bindService();
                serverBuilder.addService(serviceDefinition);
                log.info("'{}' service has been registered.", bindableService.getClass().getName());
            } else {
                ServerServiceDefinition serviceDefinition = null;
                // 如果是泛化导出，直接导出类本身
                boolean isGeneric = providerUrl.getParameter(SalukiConstants.GENERIC_KEY,
                                                             SalukiConstants.DEFAULT_GENERIC);
                if (isGeneric) {
                    Class<?> protocolClass = protocolImpl.getClass();
                    serviceDefinition = this.doExport(protocolClass, protocolImpl);
                } else {
                    Class<?> interfaceClass = ReflectUtil.name2class(providerUrl.getServiceInterface());
                    if (interfaceClass.isAssignableFrom(protocolImpl.getClass())) {
                        serviceDefinition = this.doExport(interfaceClass, protocolImpl);
                    } else {
                        throw new IllegalStateException("protocolClass " + interfaceClass.getName()
                                                        + " is not implemented by protocolImpl which is of class "
                                                        + protocolImpl.getClass());
                    }
                }
                serverBuilder.addService(serviceDefinition);
            }
            registry.register(providerUrl);
        }
        return serverBuilder.build().start();
    }

    private ServerServiceDefinition doExport(Class<?> protocolClass, Object protocolImpl) {
        String serviceName = GrpcUtils.generateServiceName(protocolClass);
        ServerServiceDefinition.Builder serviceDefBuilder = ServerServiceDefinition.builder(serviceName);
        List<Method> methods = ReflectUtil.findAllPublicMethods(protocolClass);
        if (methods.isEmpty()) {
            throw new IllegalStateException("protocolClass " + serviceName + " not have export method"
                                            + protocolClass.getClass());
        }
        for (Method method : methods) {
            MethodDescriptor<GeneratedMessageV3, GeneratedMessageV3> methodDescriptor = GrpcUtils.createMethodDescriptor(protocolClass,
                                                                                                                         method);
            serviceDefBuilder.addMethod(methodDescriptor,
                                        ServerCalls.asyncUnaryCall(new MethodInvokation(protocolImpl, method)));
        }
        log.info("'{}' service has been registered.", serviceName);
        return serviceDefBuilder.build();
    }

    private class MethodInvokation implements UnaryMethod<com.google.protobuf.GeneratedMessageV3, com.google.protobuf.GeneratedMessageV3> {

        private final Object serviceToInvoke;
        private final Method method;

        public MethodInvokation(Object serviceToInvoke, Method method){
            this.serviceToInvoke = serviceToInvoke;
            this.method = method;
        }

        @Override
        public void invoke(GeneratedMessageV3 request, StreamObserver<GeneratedMessageV3> responseObserver) {
            try {
                Object[] requestParams = new Object[] { request };
                GeneratedMessageV3 returnObj = (GeneratedMessageV3) method.invoke(serviceToInvoke, requestParams);
                responseObserver.onNext(returnObj);
            } catch (Exception ex) {
                responseObserver.onError(ex);
            } finally {
                responseObserver.onCompleted();
            }
        }

    }

}
