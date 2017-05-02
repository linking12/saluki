package com.quancheng.saluki.core.grpc.client.async;

import java.net.SocketAddress;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.AbstractFuture;
import com.google.common.util.concurrent.ListenableFuture;
import com.quancheng.saluki.core.common.NamedThreadFactory;
import com.quancheng.saluki.core.grpc.client.GrpcAsyncCall;
import com.quancheng.saluki.core.grpc.client.async.AsyncCallInternal.AsyncCallClientInternal;
import com.quancheng.saluki.core.grpc.util.MetadataKeyUtil;

import io.grpc.Attributes.Key;
import io.grpc.CallOptions;
import io.grpc.ClientCall;
import io.grpc.Grpc;
import io.grpc.Metadata;
import io.grpc.Status;

public abstract class AbstractRetryingRpcListener<RequestT, ResponseT, ResultT> extends ClientCall.Listener<ResponseT> implements Runnable {

    private final static Logger                                log              = LoggerFactory.getLogger(AbstractRetryingRpcListener.class);

    private final RetryOptions                                 retryOptions;

    private final AsyncCallClientInternal<RequestT, ResponseT> rpc;

    private final RequestT                                     request;

    private final CallOptions                                  callOptions;

    private final ScheduledExecutorService                     retryExecutorService;

    private final Metadata                                     originalMetadata;

    protected final GrpcFuture<ResultT>                        completionFuture = new GrpcFuture<>();

    protected ClientCall<RequestT, ResponseT>                  call;

    private int                                                retryCount;

    public AbstractRetryingRpcListener(RetryOptions retryOptions, RequestT request,
                                       AsyncCallClientInternal<RequestT, ResponseT> retryableRpc,
                                       CallOptions callOptions, Metadata originalMetadata){
        this.retryOptions = retryOptions;
        this.request = request;
        this.rpc = retryableRpc;
        this.callOptions = callOptions;
        this.retryExecutorService = Executors.newScheduledThreadPool(1, new NamedThreadFactory("HaClientRetry", true));
        this.originalMetadata = originalMetadata;
    }

    @Override
    public void onClose(Status status, Metadata trailers) {
        try {
            cacheCurrentServer();
        } finally {
            NameResolverNotify notify = new NameResolverNotify(callOptions.getAffinity());
            Status.Code code = status.getCode();
            if (code == Status.Code.OK) {
                onOK();
                // 如果是重试导致成功的，重置状态，这里不能随便reset，会导致lb失败
                if (retryCount > 0) {
                    notify.resetChannel();
                }
                return;
            } else {
                if (retryCount > retryOptions.getReties() || !retryOptions.isEnableRetry()) {
                    String errorCause = trailers.get(MetadataKeyUtil.GRPC_ERRORCAUSE_VALUE);
                    Exception serverException = status.withDescription(errorCause).asRuntimeException();
                    completionFuture.setException(serverException);
                    notify.resetChannel();
                    return;
                } else {
                    log.error(String.format("Retrying failed call. Failure #%d", retryCount), status.getCause());
                    call = null;
                    notify.refreshChannel();
                    retryExecutorService.schedule(new RetryListenerWrap(this), retryOptions.nextBackoffMillis(),
                                                  TimeUnit.MILLISECONDS);
                    retryCount += 1;
                }
            }
        }
    }

    private void cacheCurrentServer() {
        SocketAddress currentServer = call.getAttributes().get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR);
        HashMap<Key<?>, Object> data = Maps.newHashMap();
        data.put(GrpcAsyncCall.CURRENT_ADDR_KEY, currentServer);
        GrpcAsyncCall.updateAffinity(callOptions.getAffinity(), data);
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
        rpc.start(this.call, this.request, this, metadata);
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
            // 这个的stackTrace置为空的原因是，这个eror是服务端抛出的，但是这里的trace却是打印客户端的信息，所以要置为空
            throwable.setStackTrace(new StackTraceElement[] {});
            return super.setException(throwable);
        }
    }

}
