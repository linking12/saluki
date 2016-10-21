package com.quancheng.saluki.core.grpc.cluster.io;

import com.google.common.util.concurrent.SettableFuture;

import java.util.concurrent.Executor;

public class CancellationToken {

    private final SettableFuture<Void> cancelledFuture = SettableFuture.create();

    public void addListener(Runnable runnable, Executor executor) {
        cancelledFuture.addListener(runnable, executor);
    }

    public void cancel() {
        cancelledFuture.set(null);
    }
}
