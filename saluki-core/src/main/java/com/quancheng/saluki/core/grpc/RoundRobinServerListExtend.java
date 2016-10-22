package com.quancheng.saluki.core.grpc;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;

import io.grpc.EquivalentAddressGroup;
import io.grpc.Status;
import io.grpc.TransportManager;

public class RoundRobinServerListExtend<T> {

    private final TransportManager<T>              tm;
    private final List<EquivalentAddressGroup>     list;
    private final Iterator<EquivalentAddressGroup> cyclingIter;
    private final T                                requestDroppingTransport;
    private volatile EquivalentAddressGroup        currentServer;

    private RoundRobinServerListExtend(TransportManager<T> tm, List<EquivalentAddressGroup> list){
        this.tm = tm;
        this.list = list;
        this.cyclingIter = Iterators.cycle(list);
        this.requestDroppingTransport = tm.createFailingTransport(Status.UNAVAILABLE.withDescription("Throttled by LB"));
    }

    public T getTransportForNextServer() {
        EquivalentAddressGroup currentServer;
        synchronized (cyclingIter) {
            currentServer = cyclingIter.next();
        }
        if (currentServer == null) {
            return requestDroppingTransport;
        }
        this.currentServer = currentServer;
        return tm.getTransport(currentServer);
    }

    public SocketAddress getCurrentServer() {
        Iterator<SocketAddress> it = this.currentServer.getAddresses().iterator();
        while (it.hasNext()) {
            return it.next();
        }
        return null;
    }

    public List<SocketAddress> getServers() {
        List<SocketAddress> addresses = new ArrayList<SocketAddress>(list.size());
        for (EquivalentAddressGroup group : list) {
            List<SocketAddress> address = group.getAddresses();
            addresses.addAll(address);
        }
        return addresses;
    }

    @NotThreadSafe
    public static class Builder<T> {

        private final ImmutableList.Builder<EquivalentAddressGroup> listBuilder = ImmutableList.builder();
        private final TransportManager<T>                           tm;

        public Builder(TransportManager<T> tm){
            this.tm = tm;
        }

        public void add(@Nullable SocketAddress address) {
            listBuilder.add(new EquivalentAddressGroup(address));
        }

        public void addList(List<SocketAddress> addresses) {
            listBuilder.add(new EquivalentAddressGroup(addresses));
        }

        public RoundRobinServerListExtend<T> build() {
            return new RoundRobinServerListExtend<T>(tm, listBuilder.build());
        }
    }

}
