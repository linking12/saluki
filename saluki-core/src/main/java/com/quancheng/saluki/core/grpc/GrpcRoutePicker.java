/*
 * Copyright 1999-2012 DianRong.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.quancheng.saluki.core.grpc;

import java.net.SocketAddress;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import com.google.common.collect.Lists;
import com.quancheng.saluki.core.common.GrpcURL;
import com.quancheng.saluki.core.grpc.client.internal.unary.GrpcUnaryClientCall;
import com.quancheng.saluki.core.grpc.router.GrpcRouter;
import com.quancheng.saluki.core.grpc.router.GrpcRouterFactory;

import io.grpc.Attributes;
import io.grpc.EquivalentAddressGroup;
import io.grpc.LoadBalancer.PickResult;
import io.grpc.LoadBalancer.PickSubchannelArgs;
import io.grpc.LoadBalancer.Subchannel;
import io.grpc.LoadBalancer.SubchannelPicker;
import io.grpc.Status;

/**
 * @author liushiming 2017年4月27日 下午4:20:35
 * @version $Id: GrpcPicker.java, v 0.0.1 2017年4月27日 下午4:20:35 liushiming Exp $
 */
public class GrpcRoutePicker extends SubchannelPicker {

  private final Status status;
  private final Attributes nameResovleCache;
  private final List<Subchannel> list;
  private final int size;
  private final Object LOCK = new Object();
  private int index = 0;

  GrpcRoutePicker(List<Subchannel> list, Status status, Attributes nameResovleCache) {
    this.list = list;
    this.size = list.size();
    this.status = status;
    this.nameResovleCache = nameResovleCache;
  }

  @Override
  public PickResult pickSubchannel(PickSubchannelArgs args) {
    Map<String, Object> affinity =
        args.getCallOptions().getOption(GrpcUnaryClientCall.CALLOPTIONS_CUSTOME_KEY);
    GrpcURL refUrl = (GrpcURL) affinity.get(GrpcUnaryClientCall.GRPC_REF_URL);
    if (size > 0) {
      Subchannel subchannel = nextSubchannel(refUrl);
      affinity.put(GrpcUnaryClientCall.GRPC_NAMERESOVER_ATTRIBUTES, nameResovleCache);
      return PickResult.withSubchannel(subchannel);
    }
    if (status != null) {
      return PickResult.withError(status);
    }
    return PickResult.withNoResult();
  }

  private Subchannel nextSubchannel(GrpcURL refUrl) {
    if (size == 0) {
      throw new NoSuchElementException();
    }
    synchronized (this) {
      Subchannel val = list.get(index);
      boolean discard = discard(refUrl, val);
      index++;
      if (index >= size) {
        index = 0;
      }
      if (discard) {
        return nextSubchannel(refUrl);
      } else {
        return val;
      }
    }
  }


  private boolean discard(GrpcURL refUrl, Subchannel subchannel) {
    boolean discard = false;
    if (refUrl != null) {
      synchronized (LOCK) {
        GrpcRouter grpcRouter = GrpcRouterFactory.getInstance().getGrpcRouter(refUrl.getGroup());
        if (grpcRouter != null) {
          grpcRouter.setRefUrl(refUrl);
          EquivalentAddressGroup addressGroup = subchannel.getAddresses();
          List<SocketAddress> currentAddress = addressGroup.getAddresses();
          List<SocketAddress> deletAddress = Lists.newArrayList();
          for (SocketAddress server : currentAddress) {
            List<GrpcURL> providerUrls = findGrpcURLByAddress(server);
            if (!grpcRouter.match(providerUrls)) {
              deletAddress.add(server);
            }
          }
          if (!deletAddress.isEmpty()) {
            discard = true;
          }
        }
      }
    }

    return discard;
  }

  private List<GrpcURL> findGrpcURLByAddress(SocketAddress address) {
    Map<List<SocketAddress>, GrpcURL> addressMapping =
        nameResovleCache.get(GrpcNameResolverProvider.GRPC_ADDRESS_GRPCURL_MAPPING);
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
