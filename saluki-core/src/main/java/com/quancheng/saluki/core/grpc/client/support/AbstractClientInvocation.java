package com.quancheng.saluki.core.grpc.client.support;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Ticker;
import com.google.common.base.Verify;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.Uninterruptibles;
import com.google.protobuf.Message;
import com.quancheng.saluki.core.common.SalukiConstants;
import com.quancheng.saluki.core.grpc.client.SalukiResponse;
import com.quancheng.saluki.core.grpc.client.SalukiReuqest;
import com.quancheng.saluki.core.grpc.filter.Filter;
import com.quancheng.saluki.core.grpc.filter.GrpcRequest;
import com.quancheng.saluki.core.grpc.filter.GrpcResponse;
import com.quancheng.saluki.core.utils.ClassHelper;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.Status;
import io.grpc.stub.ClientCalls;
import io.grpc.stub.StreamObserver;

/**
 * <strong>描述：</strong>TODO 描述 <br>
 * <strong>功能：</strong><br>
 * <strong>使用场景：</strong><br>
 * <strong>注意事项：</strong>
 * <ul>
 * <li></li>
 * </ul>
 * 
 * @author shimingliu 2016年10月18日 下午11:20:15
 * @version $Id: AbstractClientInvocation.java, v 0.0.1 2016年10月18日 下午11:20:15 shimingliu Exp $
 */
public abstract class AbstractClientInvocation implements InvocationHandler {

    private final GrpcRequest            request;

    private final List<Filter>           filters;

    private final Cache<String, Channel> channelCache;

    public AbstractClientInvocation(GrpcRequest request){
        this.request = request;
        this.filters = this.doInnerFilter();
        this.channelCache = CacheBuilder.newBuilder()//
                                        .maximumSize(10L)//
                                        .softValues()//
                                        .ticker(Ticker.systemTicker())//
                                        .build();
    }

    protected GrpcRequest getRequest() {
        return request;
    }

    private Channel getChannel(SalukiReuqest request) {
        try {
            return channelCache.get(request.getRequest().getServiceName(), new Callable<Channel>() {

                @Override
                public Channel call() throws Exception {
                    return request.getChannel();
                }
            });
        } catch (ExecutionException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    protected abstract void doReBuildRequest(Method method, Object[] args);

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        Class<?>[] parameterTypes = method.getParameterTypes();
        if ("toString".equals(methodName) && parameterTypes.length == 0) {
            return AbstractClientInvocation.this.toString();
        }
        doReBuildRequest(method, args);
        for (Filter filter : filters) {
            filter.before(request);
        }
        SalukiReuqest salukiRequest = new SalukiReuqest(request);
        GrpcResponse response = new GrpcResponse();
        switch (salukiRequest.getRequest().getMethodRequest().getCallType()) {
            case SalukiConstants.RPCTYPE_ASYNC:
                futureUnaryCall(salukiRequest, response);
                break;
            case SalukiConstants.RPCTYPE_BLOCKING:
                blockingUnaryCall(salukiRequest, response);
                break;
            default:
                futureUnaryCall(salukiRequest, response);
                break;
        }
        response.setReturnType(request.getMethodRequest().getResponseType());
        for (Filter filter : filters) {
            filter.after(response);
        }
        SalukiResponse salukiResponse = new SalukiResponse(response);
        return salukiResponse.getResponseArg();
    }

    private void asyncUnaryCall(SalukiReuqest request, GrpcResponse response) {
        final CountDownLatch latch = new CountDownLatch(1);
        ClientCall<Message, Message> newCall = getChannel(request).newCall(request.getMethodDescriptor(),
                                                                           CallOptions.DEFAULT);
        ClientCalls.asyncUnaryCall(newCall, request.getRequestArg(), new StreamObserver<Message>() {

            @Override
            public void onNext(Message resp) {
                response.setMessage(resp);
            }

            @Override
            public void onError(Throwable t) {
                Status status = Status.fromThrowable(t);
                Verify.verify(status.getCode() == Status.Code.INTERNAL);
                latch.countDown();
            }

            @Override
            public void onCompleted() {

            }

        });
        int timeout = request.getRequest().getMethodRequest().getCallTimeout();
        if (!Uninterruptibles.awaitUninterruptibly(latch, timeout, TimeUnit.SECONDS)) {
            throw new RuntimeException("timeout!");
        }
    }

    private void futureUnaryCall(SalukiReuqest request, GrpcResponse response) {
        final CountDownLatch latch = new CountDownLatch(1);
        ClientCall<Message, Message> newCall = getChannel(request).newCall(request.getMethodDescriptor(),
                                                                           CallOptions.DEFAULT);
        ListenableFuture<Message> future = ClientCalls.futureUnaryCall(newCall, request.getRequestArg());
        Futures.addCallback(future, new FutureCallback<Message>() {

            @Override
            public void onSuccess(Message resp) {
                response.setMessage(resp);
            }

            @Override
            public void onFailure(Throwable t) {
                Status status = Status.fromThrowable(t);
                Verify.verify(status.getCode() == Status.Code.INTERNAL);
                latch.countDown();
            }

        });
        int timeout = request.getRequest().getMethodRequest().getCallTimeout();
        if (!Uninterruptibles.awaitUninterruptibly(latch, timeout, TimeUnit.SECONDS)) {
            throw new RuntimeException("timeout!");
        }
    }

    private void blockingUnaryCall(SalukiReuqest request, GrpcResponse response) {
        ClientCall<Message, Message> newCall = getChannel(request).newCall(request.getMethodDescriptor(),
                                                                           CallOptions.DEFAULT);
        try {
            Message resp = ClientCalls.blockingUnaryCall(newCall, request.getRequestArg());
            response.setMessage(resp);
        } catch (Throwable e) {
            Status status = Status.fromThrowable(e);
            Verify.verify(status.getCode() == Status.Code.INTERNAL);
        }

    }

    private List<Filter> doInnerFilter() {
        Iterable<Filter> candidates = ServiceLoader.load(Filter.class, ClassHelper.getClassLoader());
        List<Filter> list = Lists.newArrayList();
        for (Filter current : candidates) {
            list.add(current);
        }
        return list;
    }

}
