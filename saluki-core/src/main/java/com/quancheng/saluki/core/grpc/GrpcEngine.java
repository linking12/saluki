/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the confidential and
 * proprietary information of Quancheng-ec.com ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Quancheng-ec.com.
 */
package com.quancheng.saluki.core.grpc;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLException;

import org.apache.commons.pool2.BaseKeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.quancheng.saluki.core.common.Constants;
import com.quancheng.saluki.core.common.GrpcURL;
import com.quancheng.saluki.core.common.NamedThreadFactory;
import com.quancheng.saluki.core.grpc.client.GrpcClientStrategy;
import com.quancheng.saluki.core.grpc.client.GrpcProtocolClient;
import com.quancheng.saluki.core.grpc.exception.RpcFrameworkException;
import com.quancheng.saluki.core.grpc.interceptor.HeaderClientInterceptor;
import com.quancheng.saluki.core.grpc.interceptor.HeaderServerInterceptor;
import com.quancheng.saluki.core.grpc.server.GrpcServerStrategy;
import com.quancheng.saluki.core.grpc.util.SslUtil;
import com.quancheng.saluki.core.registry.Registry;
import com.quancheng.saluki.core.registry.RegistryProvider;

import io.grpc.Attributes;
import io.grpc.Channel;
import io.grpc.ClientInterceptor;
import io.grpc.ClientInterceptors;
import io.grpc.Internal;
import io.grpc.LoadBalancer;
import io.grpc.ServerInterceptor;
import io.grpc.ServerInterceptors;
import io.grpc.ServerServiceDefinition;
import io.grpc.ServerTransportFilter;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NegotiationType;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.NettyServerBuilder;
import io.grpc.util.TransmitStatusRuntimeExceptionInterceptor;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

/**
 * @author shimingliu 2016年12月14日 下午10:43:19
 * @version GrpcEngine1.java, v 0.0.1 2016年12月14日 下午10:43:19 shimingliu
 */
@Internal
public final class GrpcEngine {

  private static final Logger log = LoggerFactory.getLogger(GrpcEngine.class);

  private final GrpcURL registryUrl;

  private final Registry registry;

  private final Map<String, Set<GrpcURL>> subscribeGroupCache = Maps.newConcurrentMap();

  private GenericKeyedObjectPool<String, Channel> channelPool;

  public GrpcEngine(GrpcURL registryUrl) {
    this.registryUrl = registryUrl;
    this.registry = RegistryProvider.asFactory().newRegistry(registryUrl);
  }

  private void initChannelPool() {
    GenericKeyedObjectPoolConfig config = new GenericKeyedObjectPoolConfig();
    config.setMaxTotal(Integer.MAX_VALUE);
    config.setMaxTotalPerKey(10);
    config.setBlockWhenExhausted(true);
    config.setMinIdlePerKey(3);
    config.setMaxIdlePerKey(10);
    config.setMaxWaitMillis(1000L);
    config.setNumTestsPerEvictionRun(Integer.MAX_VALUE);
    config.setTestOnBorrow(false);
    config.setTestOnReturn(false);
    config.setTestWhileIdle(false);
    config.setTimeBetweenEvictionRunsMillis(60000L);
    config.setMinEvictableIdleTimeMillis(30000);
    config.setTestWhileIdle(false);
    this.channelPool =
        new GenericKeyedObjectPool<String, Channel>(new GrpcChannelFactory(), config);

  }

  public Object getClient(GrpcURL refUrl) throws Exception {
    if (channelPool == null) {
      initChannelPool();
    }
    GrpcProtocolClient.ChannelCall channelCall = new GrpcProtocolClient.ChannelCall() {

      @Override
      public Channel borrowChannel(final GrpcURL realRefUrl) {
        GrpcURL subscribeUrl = realRefUrl;
        if (subscribeUrl == null) {
          subscribeUrl = refUrl;
        }
        String group = cacheSubscribeUrl(subscribeUrl);
        try {
          return channelPool.borrowObject(group);
        } catch (Exception e) {
          throw new java.lang.IllegalArgumentException("Grpc borrow Channel failed", e);
        }
      }

      private String cacheSubscribeUrl(GrpcURL subscribeUrl) {
        String group = subscribeUrl.getGroup();
        Set<GrpcURL> refUrls = subscribeGroupCache.get(group);
        if (refUrls == null) {
          refUrls = Sets.newLinkedHashSet();
          refUrls.add(subscribeUrl);
          subscribeGroupCache.put(group, refUrls);
        } else {
          if (!refUrls.contains(subscribeUrl)) {
            refUrls.add(subscribeUrl);
          }
        }
        return group;
      }

      @Override
      public void returnChannel(final GrpcURL realRefUrl, final Channel channel) {
        GrpcURL realRefUrltemp = realRefUrl;
        if (realRefUrltemp == null) {
          realRefUrltemp = refUrl;
        }
        String group = realRefUrltemp.getGroup();
        channelPool.returnObject(group, channel);
      }

    };
    GrpcClientStrategy strategy = new GrpcClientStrategy(refUrl, channelCall);
    return strategy.getGrpcClient();
  }

  public io.grpc.Server getServer(Map<GrpcURL, Object> providerUrls, int rpcPort) throws Exception {

    final NettyServerBuilder remoteServer = NettyServerBuilder.forPort(rpcPort)//
        .sslContext(buildServerSslContext())//
        .keepAliveTime(1, TimeUnit.DAYS)//
        .bossEventLoopGroup(createBossEventLoopGroup())//
        .workerEventLoopGroup(createWorkEventLoopGroup())//
        // This is a performance optimization that avoids the synchronization and queuing overhead
        // that comes with SerializingExecutor.
        // 性能优化，用户线程与Io线程的切换需要耗费时间的，而同步和入队需要耗费时间的
        .addTransportFilter(new ServerTransportFilter() {
          @Override
          public Attributes transportReady(Attributes transportAttrs) {
            log.debug("network transport is ready!");
            return transportAttrs;
          }

          @Override
          public void transportTerminated(Attributes transportAttrs) {
            log.debug("network transport is terminated!");
          }
        }).directExecutor();

    final List<ServerInterceptor> interceptors = Arrays.asList(HeaderServerInterceptor.instance(),
        TransmitStatusRuntimeExceptionInterceptor.instance());

    for (Map.Entry<GrpcURL, Object> entry : providerUrls.entrySet()) {
      GrpcURL providerUrl = entry.getKey();
      Object protocolImpl = entry.getValue();
      GrpcServerStrategy strategy = new GrpcServerStrategy(providerUrl, protocolImpl);
      ServerServiceDefinition serviceDefinition =
          ServerInterceptors.intercept(strategy.getServerDefintion(), interceptors);
      remoteServer.addService(serviceDefinition);
      int registryRpcPort = providerUrl.getParameter(Constants.REGISTRY_RPC_PORT_KEY, rpcPort);
      providerUrl = providerUrl.setPort(registryRpcPort);
      registry.register(providerUrl);
    }
    log.info("grpc server is build complete ");
    return remoteServer.build();

  }

  private SslContext buildClientSslContext() {
    try {
      InputStream certs = SslUtil.loadInputStreamCert("server.pem");
      return GrpcSslContexts
          .configure(SslContextBuilder.forClient()//
              .trustManager(certs))//
          .build();
    } catch (SSLException e) {
      throw new RpcFrameworkException(e);
    }
  }

  private LoadBalancer.Factory buildLoadBalanceFactory() {
    return GrpcRouteRoundRobinLbFactory.getInstance();
  }

  private SslContext buildServerSslContext() {
    try {
      InputStream certs = SslUtil.loadInputStreamCert("server.pem");
      InputStream keys = SslUtil.loadInputStreamCert("server_pkcs8.key");
      return GrpcSslContexts.configure(SslContextBuilder.forServer(certs, keys)).build();
    } catch (SSLException e) {
      throw new RpcFrameworkException(e);
    }
  }

  private NioEventLoopGroup createBossEventLoopGroup() {
    ThreadFactory threadFactory = new NamedThreadFactory("grpc-default-boss-ELG", true);
    return new NioEventLoopGroup(1, Executors.newCachedThreadPool(threadFactory));
  }

  private NioEventLoopGroup createWorkEventLoopGroup() {
    ThreadFactory threadFactory = new NamedThreadFactory("grpc-default-worker-ELG", true);
    return new NioEventLoopGroup(0, Executors.newCachedThreadPool(threadFactory));
  }

  private class GrpcChannelFactory extends BaseKeyedPooledObjectFactory<String, Channel> {

    private final List<ClientInterceptor> interceptors =
        Arrays.asList(HeaderClientInterceptor.instance());

    @Override
    public Channel create(String group) throws Exception {
      Set<GrpcURL> subscribeUrls = subscribeGroupCache.get(group);
      Channel channel = NettyChannelBuilder.forTarget(registryUrl.toJavaURI().toString())//
          .nameResolverFactory(new GrpcNameResolverProvider(subscribeUrls))//
          .loadBalancerFactory(buildLoadBalanceFactory())//
          .sslContext(buildClientSslContext())//
          .usePlaintext(false)//
          .negotiationType(NegotiationType.TLS)//
          .eventLoopGroup(createWorkEventLoopGroup())//
          .keepAliveTime(1, TimeUnit.DAYS)//
          .directExecutor()//
          .build();//
      return ClientInterceptors.intercept(channel, interceptors);
    }

    @Override
    public PooledObject<Channel> wrap(Channel value) {
      return new DefaultPooledObject<Channel>(value);
    }

  }
}
