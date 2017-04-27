/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.core.grpc;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.grpc.ConnectivityState.IDLE;
import static io.grpc.ConnectivityState.READY;
import static io.grpc.ConnectivityState.TRANSIENT_FAILURE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nullable;

import com.google.common.annotations.VisibleForTesting;

import io.grpc.Attributes;
import io.grpc.ConnectivityStateInfo;
import io.grpc.EquivalentAddressGroup;
import io.grpc.Internal;
import io.grpc.LoadBalancer;
import io.grpc.LoadBalancer.Helper;
import io.grpc.ResolvedServerInfo;
import io.grpc.ResolvedServerInfoGroup;
import io.grpc.Status;

/**
 * @author shimingliu 2016年12月14日 下午5:31:11
 * @version ThrallRoundRobinLoadBalanceFactory1.java, v 0.0.1 2016年12月14日 下午5:31:11 shimingliu
 */

@Internal
public class GrpcRoundRobinLoadBalanceFactory extends LoadBalancer.Factory {

    private static final GrpcRoundRobinLoadBalanceFactory instance = new GrpcRoundRobinLoadBalanceFactory();

    private GrpcRoundRobinLoadBalanceFactory(){
    }

    public static GrpcRoundRobinLoadBalanceFactory getInstance() {
        return instance;
    }

    @Override
    public LoadBalancer newLoadBalancer(Helper helper) {
        return new GrpcRoundRobinLoadBalancer(helper);
    }

    private static class GrpcRoundRobinLoadBalancer extends LoadBalancer {

        private final Helper                                                helper;
        private final Map<EquivalentAddressGroup, Subchannel>               subchannels = new HashMap<EquivalentAddressGroup, Subchannel>();

        private volatile Attributes                                         nameResovleCache;

        @VisibleForTesting
        static final Attributes.Key<AtomicReference<ConnectivityStateInfo>> STATE_INFO  = Attributes.Key.of("state-info");

        GrpcRoundRobinLoadBalancer(Helper helper){
            this.helper = checkNotNull(helper, "helper");
        }

        @Override
        public void handleResolvedAddresses(List<ResolvedServerInfoGroup> servers, Attributes attributes) {
            // 保存namesovle的attibutes信息
            this.nameResovleCache = attributes;
            Set<EquivalentAddressGroup> currentAddrs = subchannels.keySet();
            Set<EquivalentAddressGroup> latestAddrs = resolvedServerInfoGroupToEquivalentAddressGroup(servers);
            Set<EquivalentAddressGroup> addedAddrs = setsDifference(latestAddrs, currentAddrs);
            Set<EquivalentAddressGroup> removedAddrs = setsDifference(currentAddrs, latestAddrs);
            for (EquivalentAddressGroup addressGroup : addedAddrs) {
                Attributes subchannelAttrs = Attributes.newBuilder().set(STATE_INFO,
                                                                         new AtomicReference<ConnectivityStateInfo>(ConnectivityStateInfo.forNonError(IDLE))).build();
                Subchannel subchannel = checkNotNull(helper.createSubchannel(addressGroup, subchannelAttrs),
                                                     "subchannel");
                subchannels.put(addressGroup, subchannel);
                subchannel.requestConnection();
            }
            for (EquivalentAddressGroup addressGroup : removedAddrs) {
                Subchannel subchannel = subchannels.remove(addressGroup);
                subchannel.shutdown();
            }
            updatePicker(getAggregatedError());
        }

        @Override
        public void handleNameResolutionError(Status error) {
            updatePicker(error);
        }

        @Override
        public void handleSubchannelState(Subchannel subchannel, ConnectivityStateInfo stateInfo) {
            if (!subchannels.containsValue(subchannel)) {
                return;
            }
            if (stateInfo.getState() == IDLE) {
                subchannel.requestConnection();
            }
            getSubchannelStateInfoRef(subchannel).set(stateInfo);
            updatePicker(getAggregatedError());
        }

        @Override
        public void shutdown() {
            for (Subchannel subchannel : getSubchannels()) {
                subchannel.shutdown();
            }
        }

        /**
         * Updates picker with the list of active subchannels (state == READY).
         */
        private void updatePicker(@Nullable Status error) {
            List<Subchannel> activeList = filterNonFailingSubchannels(getSubchannels());
            helper.updatePicker(new GrpcPicker(helper, activeList, error, nameResovleCache));
        }

        /**
         * Filters out non-ready subchannels.
         */
        private static List<Subchannel> filterNonFailingSubchannels(Collection<Subchannel> subchannels) {
            List<Subchannel> readySubchannels = new ArrayList<Subchannel>(subchannels.size());
            for (Subchannel subchannel : subchannels) {
                if (getSubchannelStateInfoRef(subchannel).get().getState() == READY) {
                    readySubchannels.add(subchannel);
                }
            }
            return readySubchannels;
        }

        /**
         * Converts list of {@link ResolvedServerInfoGroup} to {@link EquivalentAddressGroup} set.
         */
        private static Set<EquivalentAddressGroup> resolvedServerInfoGroupToEquivalentAddressGroup(List<ResolvedServerInfoGroup> groupList) {
            Set<EquivalentAddressGroup> addrs = new HashSet<EquivalentAddressGroup>();
            for (ResolvedServerInfoGroup group : groupList) {
                for (ResolvedServerInfo server : group.getResolvedServerInfoList()) {
                    addrs.add(new EquivalentAddressGroup(server.getAddress()));
                }
            }
            return addrs;
        }

        /**
         * If all subchannels are TRANSIENT_FAILURE, return the Status associated with an arbitrary subchannel
         * otherwise, return null.
         */
        @Nullable
        private Status getAggregatedError() {
            Status status = null;
            for (Subchannel subchannel : getSubchannels()) {
                ConnectivityStateInfo stateInfo = getSubchannelStateInfoRef(subchannel).get();
                if (stateInfo.getState() != TRANSIENT_FAILURE) {
                    return null;
                }
                status = stateInfo.getStatus();
            }
            return status;
        }

        @VisibleForTesting
        Collection<Subchannel> getSubchannels() {
            return subchannels.values();
        }

        private static AtomicReference<ConnectivityStateInfo> getSubchannelStateInfoRef(Subchannel subchannel) {
            return checkNotNull(subchannel.getAttributes().get(STATE_INFO), "STATE_INFO");
        }

        private static <T> Set<T> setsDifference(Set<T> a, Set<T> b) {
            Set<T> aCopy = new HashSet<T>(a);
            aCopy.removeAll(b);
            return aCopy;
        }
    }

}
