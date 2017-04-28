/*
 * Copyright 1999-2012 DianRong.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.quancheng.saluki.core.grpc;

import static io.grpc.ConnectivityState.IDLE;

import java.net.SocketAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.quancheng.saluki.core.common.GrpcURL;
import com.quancheng.saluki.core.common.RpcContext;
import com.quancheng.saluki.core.grpc.client.GrpcAsyncCall;
import com.quancheng.saluki.core.grpc.router.GrpcRouter;
import com.quancheng.saluki.core.grpc.router.GrpcRouterFactory;

import io.grpc.Attributes;
import io.grpc.Attributes.Key;
import io.grpc.ConnectivityStateInfo;
import io.grpc.EquivalentAddressGroup;
import io.grpc.LoadBalancer.Helper;
import io.grpc.LoadBalancer.PickResult;
import io.grpc.LoadBalancer.PickSubchannelArgs;
import io.grpc.LoadBalancer.Subchannel;
import io.grpc.LoadBalancer.SubchannelPicker;
import io.grpc.Status;

/**
 * <strong>描述：</strong>TODO 描述 <br>
 * <strong>功能：</strong><br>
 * <strong>使用场景：</strong><br>
 * <strong>注意事项：</strong>
 * <ul>
 * <li></li>
 * </ul>
 * 
 * @author liushiming 2017年4月27日 下午4:20:35
 * @version $Id: GrpcPicker.java, v 0.0.1 2017年4月27日 下午4:20:35 liushiming Exp $
 */
public class GrpcPicker extends SubchannelPicker {

    private final Helper     helper;
    private final Status     status;
    private final Attributes nameResovleCache;
    private int              index = 0;
    private List<Subchannel> list;
    private int              size;

    GrpcPicker(Helper helper, List<Subchannel> list, Status status, Attributes nameResovleCache){
        this.helper = helper;
        this.list = list;
        this.size = list.size();
        this.status = status;
        this.nameResovleCache = nameResovleCache;
    }

    @Override
    public PickResult pickSubchannel(PickSubchannelArgs args) {
        Attributes affinity = args.getCallOptions().getAffinity();
        this.routerAddress(affinity);
        if (size > 0) {
            Subchannel subchannel = nextSubchannel();
            mergeNameResolver2Affinity(affinity, subchannel.getAddresses().getAddresses());
            return PickResult.withSubchannel(subchannel);
        }
        if (status != null) {
            return PickResult.withError(status);
        }

        return PickResult.withNoResult();
    }

    private Subchannel nextSubchannel() {
        if (size == 0) {
            throw new NoSuchElementException();
        }
        synchronized (this) {
            Subchannel val = list.get(index);
            index++;
            if (index >= size) {
                index = 0;
            }
            return val;
        }
    }

    private void mergeNameResolver2Affinity(Attributes affinity, List<SocketAddress> addresses) {
        HashMap<Key<?>, Object> data = Maps.newHashMap();
        for (Key<?> key : this.nameResovleCache.keys()) {
            Object obj = this.nameResovleCache.get(key);
            data.put(key, obj);
        }
        for (Key<?> key : affinity.keys()) {
            Object obj = affinity.get(key);
            data.put(key, obj);
        }
        data.put(GrpcAsyncCall.ROUNDROBINED_REMOTE_ADDR_KEYS, addresses);
        GrpcAsyncCall.updateAffinity(affinity, data);
    }

    private void routerAddress(Attributes affinity) {
        GrpcRouter grpcRouter = null;
        try {
            String currentRouterRule = null;
            // 从线程上下文去路由规则
            if (RpcContext.getContext().containAttachment("routerRule")) {
                currentRouterRule = RpcContext.getContext().getAttachment("routerRule");
            }
            // 从配置中心获取路由规则并覆盖线程上下文的路由规则
            String configRouterRule = nameResovleCache.get(GrpcNameResolverProvider.GRPC_ROUTER_MESSAGE);
            if (configRouterRule != null) {
                currentRouterRule = configRouterRule;
            }
            if (currentRouterRule != null) {
                grpcRouter = GrpcRouterFactory.getInstance().createRouter(currentRouterRule);
            }
        } finally {
            if (grpcRouter != null) {
                List<Subchannel> subchannels = this.list;
                GrpcURL refUrl = affinity.get(GrpcAsyncCall.GRPC_REF_URL);
                grpcRouter.setRefUrl(refUrl);
                List<Subchannel> routedSubchannes = Lists.newArrayList();
                for (Subchannel subchannel : subchannels) {
                    List<SocketAddress> updatedServers = Lists.newArrayList();
                    List<SocketAddress> addresses = subchannel.getAddresses().getAddresses();
                    for (SocketAddress server : addresses) {
                        List<GrpcURL> providerUrls = findGrpcURLByAddress(server);
                        if (grpcRouter.match(providerUrls)) {
                            updatedServers.add(server);
                        } else {
                            subchannel.shutdown();
                        }
                    }
                    if (updatedServers.isEmpty()) {
                        throw new IllegalArgumentException("The router condition has stoped all server address");
                    } else {
                        Subchannel routedSubchannel = this.buildSubchannel(updatedServers);
                        routedSubchannes.add(routedSubchannel);
                        routedSubchannel.requestConnection();
                    }
                }
                this.list = Collections.unmodifiableList(routedSubchannes);
                this.size = routedSubchannes.size();
            }
        }
    }

    private Subchannel buildSubchannel(List<SocketAddress> socks) {
        Attributes subchannelAttrs = Attributes.newBuilder().set(Attributes.Key.of("state-info"),
                                                                 new AtomicReference<ConnectivityStateInfo>(ConnectivityStateInfo.forNonError(IDLE))).build();
        Subchannel subchannel = helper.createSubchannel(new EquivalentAddressGroup(socks), subchannelAttrs);
        return subchannel;
    }

    private List<GrpcURL> findGrpcURLByAddress(SocketAddress address) {
        Map<List<SocketAddress>, GrpcURL> addressMapping = nameResovleCache.get(GrpcNameResolverProvider.GRPC_ADDRESS_GRPCURL_MAPPING);
        List<GrpcURL> providerUrls = Lists.newArrayList();
        if (!addressMapping.isEmpty()) {
            for (Map.Entry<List<SocketAddress>, GrpcURL> entry : addressMapping.entrySet()) {
                List<SocketAddress> allAddress = entry.getKey();
                if (allAddress.contains(address)) {
                    providerUrls.add(entry.getValue());
                }
            }
        }
        return providerUrls;
    }

}
