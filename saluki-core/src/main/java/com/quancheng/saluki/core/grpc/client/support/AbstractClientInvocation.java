package com.quancheng.saluki.core.grpc.client.support;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Lists;
import com.google.protobuf.Message;
import com.quancheng.saluki.core.common.SalukiConstants;
import com.quancheng.saluki.core.grpc.client.SalukiResponse;
import com.quancheng.saluki.core.grpc.client.SalukiReuqest;
import com.quancheng.saluki.core.grpc.filter.Filter;
import com.quancheng.saluki.core.grpc.filter.GrpcRequest;
import com.quancheng.saluki.core.grpc.filter.GrpcResponse;
import com.quancheng.saluki.core.utils.ClassHelper;

import io.grpc.CallOptions;
import io.grpc.ClientCall;
import io.grpc.stub.ClientCalls;

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

    private final GrpcRequest   request;

    private final List<Filter>  filters;

    private final SalukiReuqest salukiRequest;

    private final Object        lock = new Object();

    public AbstractClientInvocation(GrpcRequest request){
        this.request = request;
        this.filters = this.doInnerFilter();
        this.salukiRequest = new SalukiReuqest();
    }

    public GrpcRequest getRequest() {
        return request;
    }

    protected abstract void doReBuildRequest(Method method, Object[] args);

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        Class<?>[] parameterTypes = method.getParameterTypes();
        if ("toString".equals(methodName) && parameterTypes.length == 0) {
            return AbstractClientInvocation.this.toString();
        }
        synchronized (lock) {
            doReBuildRequest(method, args);
            salukiRequest.setRequest(request);
        }
        for (Filter filter : filters) {
            filter.before(request);
        }
        ClientCall<Message, Message> newCall = salukiRequest.getChannel().newCall(salukiRequest.getMethodDescriptor(),
                                                                                  CallOptions.DEFAULT);
        Message resp = null;

        switch (salukiRequest.getRequest().getMethodRequest().getCallType()) {
            case SalukiConstants.RPCTYPE_ASYNC:
                resp = ClientCalls.futureUnaryCall(newCall, salukiRequest.getRequestArg()).get(
                                                                                               salukiRequest.getRequest().getMethodRequest().getCallTimeout(),
                                                                                               TimeUnit.SECONDS);
                break;
            case SalukiConstants.RPCTYPE_BLOCKING:
                resp = ClientCalls.blockingUnaryCall(newCall, salukiRequest.getRequestArg());
                break;
            default:
                resp = ClientCalls.futureUnaryCall(newCall, salukiRequest.getRequestArg()).get(
                                                                                               salukiRequest.getRequest().getMethodRequest().getCallTimeout(),
                                                                                               TimeUnit.SECONDS);
                break;
        }
        GrpcResponse response = new GrpcResponse();
        response.setMessage(resp);
        response.setReturnType(request.getMethodRequest().getResponseType());
        for (Filter filter : filters) {
            filter.after(response);
        }
        SalukiResponse salukiResponse = new SalukiResponse(response);
        return salukiResponse.getResponseArg();
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
