/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the confidential and
 * proprietary information of Quancheng-ec.com ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Quancheng-ec.com.
 */
package com.quancheng.saluki.core.grpc;

import java.net.SocketAddress;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.quancheng.saluki.core.common.GrpcURL;

import io.grpc.Attributes;
import io.grpc.Internal;
import io.grpc.NameResolver;
import io.grpc.NameResolverProvider;

/**
 * @author shimingliu 2016年12月14日 下午5:15:00
 * @version ThrallNameResolverProvider1.java, v 0.0.1 2016年12月14日 下午5:15:00 shimingliu
 */
@Internal
public class GrpcNameResolverProvider extends NameResolverProvider {

  public static final Attributes.Key<String> GRPC_ROUTER_MESSAGE = Attributes.Key.of("grpc-router");

  public static final Attributes.Key<Map<List<SocketAddress>, GrpcURL>> GRPC_ADDRESS_GRPCURL_MAPPING =
      Attributes.Key.of("grpc-address-mapping");

  public static final Attributes.Key<List<SocketAddress>> REMOTE_ADDR_KEYS =
      Attributes.Key.of("remote-addresss");

  public static final Attributes.Key<NameResolver.Listener> NAMERESOVER_LISTENER =
      Attributes.Key.of("nameResolver-Listener");

  private final Set<GrpcURL> subscribeUrls;

  public GrpcNameResolverProvider(Set<GrpcURL> subscribeUrls) {
    this.subscribeUrls = subscribeUrls;
  }

  @Override
  protected boolean isAvailable() {
    return true;
  }

  @Override
  protected int priority() {
    return 5;
  }

  @Override
  public NameResolver newNameResolver(URI targetUri, Attributes params) {
    return new GrpcNameResolver(targetUri, params, subscribeUrls);
  }

  @Override
  public String getDefaultScheme() {
    return null;
  }

}
