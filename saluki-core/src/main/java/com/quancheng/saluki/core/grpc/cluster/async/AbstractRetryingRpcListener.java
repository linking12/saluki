package com.quancheng.saluki.core.grpc.cluster.async;

import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.util.BackOff;
import com.google.api.client.util.Sleeper;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.AbstractFuture;
import com.google.common.util.concurrent.ListenableFuture;
import com.quancheng.saluki.core.grpc.cluster.RetriesExhaustedException;
import com.quancheng.saluki.core.grpc.cluster.config.RetryOptions;

import io.grpc.CallOptions;
import io.grpc.ClientCall;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

public abstract class AbstractRetryingRpcListener<RequestT, ResponseT, ResultT> extends ClientCall.Listener<ResponseT> implements Runnable {

    protected final static Logger LOG = LoggerFactory.getLogger(AbstractRetryingRpcListener.class);

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

    @VisibleForTesting
    BackOff                                           currentBackoff;
    @VisibleForTesting
    Sleeper                                           sleeper          = Sleeper.DEFAULT;

    private final SalukiAsyncRpc<RequestT, ResponseT> rpc;
    private final RetryOptions                        retryOptions;
    private final RequestT                            request;
    private final CallOptions                         callOptions;
    private final ScheduledExecutorService            retryExecutorService;
    private int                                       failedCount;
    private final Metadata                            originalMetadata;

    protected final GrpcFuture<ResultT>               completionFuture = new GrpcFuture<>();
    protected ClientCall<RequestT, ResponseT>         call;

    public AbstractRetryingRpcListener(RetryOptions retryOptions, RequestT request,
                                       SalukiAsyncRpc<RequestT, ResponseT> retryableRpc, CallOptions callOptions,
                                       ScheduledExecutorService retryExecutorService, Metadata originalMetadata){
        this.retryOptions = retryOptions;
        this.request = request;
        this.rpc = retryableRpc;
        this.callOptions = callOptions;
        this.retryExecutorService = retryExecutorService;
        this.originalMetadata = originalMetadata;
    }

    @Override
    public void onClose(Status status, Metadata trailers) {
        Status.Code code = status.getCode();
        // OK
        if (code == Status.Code.OK) {
            onOK();
            return;
        }
        // Non retry scenario
        if (!retryOptions.enableRetries() || !retryOptions.isRetryable(code) || !rpc.isRetryable(getRetryRequest())) {
            completionFuture.setException(status.asRuntimeException());
            return;
        }
        // Attempt retry with backoff
        long nextBackOff = getNextBackoff();
        failedCount += 1;
        // Backoffs timed out.
        if (nextBackOff == BackOff.STOP) {
            String message = String.format("Exhausted retries after %d failures.", failedCount);
            StatusRuntimeException cause = status.asRuntimeException();
            completionFuture.setException(new RetriesExhaustedException(message, cause));
            return;
        }
        // Perform Retry
        LOG.info("Retrying failed call. Failure #%d, got: %s", status.getCause(), failedCount, status);
        call = null;
        retryExecutorService.schedule(this, nextBackOff, TimeUnit.MILLISECONDS);
    }

    protected abstract void onOK();

    private long getNextBackoff() {
        if (this.currentBackoff == null) {
            this.currentBackoff = retryOptions.createBackoff();
        }
        try {
            return currentBackoff.nextBackOffMillis();
        } catch (IOException e) {
            return BackOff.STOP;
        }
    }

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
}
