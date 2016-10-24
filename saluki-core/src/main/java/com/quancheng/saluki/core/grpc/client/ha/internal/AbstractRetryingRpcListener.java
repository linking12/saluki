package com.quancheng.saluki.core.grpc.client.ha.internal;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.AbstractFuture;
import com.google.common.util.concurrent.ListenableFuture;
import com.quancheng.saluki.core.grpc.client.ha.HaAsyncRpc;
import com.quancheng.saluki.core.grpc.client.ha.RetryOptions;
import com.quancheng.saluki.core.grpc.client.ha.notify.HaRetryNotify;
import com.quancheng.saluki.core.utils.NamedThreadFactory;

import io.grpc.CallOptions;
import io.grpc.ClientCall;
import io.grpc.Metadata;
import io.grpc.Status;

public abstract class AbstractRetryingRpcListener<RequestT, ResponseT, ResultT> extends ClientCall.Listener<ResponseT> implements Runnable {

    protected final static Logger                 LOG              = LoggerFactory.getLogger(AbstractRetryingRpcListener.class);
    private final RetryOptions                    retryOptions;
    private final HaAsyncRpc<RequestT, ResponseT> rpc;
    private final RequestT                        request;
    private final CallOptions                     callOptions;
    private final ScheduledExecutorService        retryExecutorService;
    private int                                   retryCount;
    private final Metadata                        originalMetadata;
    protected final GrpcFuture<ResultT>           completionFuture = new GrpcFuture<>();
    protected ClientCall<RequestT, ResponseT>     call;

    public AbstractRetryingRpcListener(RetryOptions retryOptions, RequestT request,
                                       HaAsyncRpc<RequestT, ResponseT> retryableRpc, CallOptions callOptions,
                                       Metadata originalMetadata){
        this.retryOptions = retryOptions;
        this.request = request;
        this.rpc = retryableRpc;
        this.callOptions = callOptions;
        this.retryExecutorService = Executors.newScheduledThreadPool(1, new NamedThreadFactory("HaClientRetry", true));
        this.originalMetadata = originalMetadata;
    }

    @Override
    public void onClose(Status status, Metadata trailers) {
        Status.Code code = status.getCode();
        if (code == Status.Code.OK) {
            onOK();
            return;
        } else {
            HaRetryNotify notify = new HaRetryNotify(callOptions.getAffinity());
            if (retryCount > retryOptions.getReties() || !retryOptions.isEnableRetry()) {
                completionFuture.setException(status.asException());
                notify.resetChannel();
                return;
            } else {
                LOG.error(String.format("Retrying failed call. Failure #%d", retryCount), status.getCause());
                call = null;
                notify.onRefreshChannel();
                retryExecutorService.schedule(this, retryOptions.nextBackoffMillis(), TimeUnit.MILLISECONDS);
                retryCount += 1;
            }
        }
    }

    protected abstract void onOK();

    public ListenableFuture<ResultT> getCompletionFuture() {
        return completionFuture;
    }

    @Override
    public void run() {
        Metadata metadata = new Metadata();
        metadata.merge(originalMetadata);
        this.call = rpc.newCall(callOptions);
        rpc.start(this.call, getRetryRequest(), this, metadata);
    }

    protected RequestT getRetryRequest() {
        return request;
    }

    public void start() {
        run();
    }

    public void cancel() {
        if (this.call != null) {
            call.cancel("User requested cancelation.", null);
        }
    }

    protected class GrpcFuture<RespT> extends AbstractFuture<RespT> {

        @Override
        protected void interruptTask() {
            if (call != null) {
                call.cancel("Request interrupted.", null);
            }
        }

        @Override
        protected boolean set(@Nullable RespT resp) {
            return super.set(resp);
        }

        @Override
        protected boolean setException(Throwable throwable) {
            return super.setException(throwable);
        }
    }
}
