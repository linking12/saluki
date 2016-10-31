package com.quancheng.saluki.core.grpc.server.support;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.protobuf.Message;
import com.quancheng.saluki.core.common.RpcContext;
import com.quancheng.saluki.core.common.SalukiConstants;
import com.quancheng.saluki.core.grpc.exception.RpcFrameworkException;
import com.quancheng.saluki.core.grpc.exception.RpcServiceException;
import com.quancheng.saluki.core.grpc.utils.PojoProtobufUtils;
import com.quancheng.saluki.core.utils.ReflectUtil;
import com.quancheng.saluki.serializer.exception.ProtobufException;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.ServerCalls.UnaryMethod;
import io.grpc.stub.StreamObserver;

public class ServerInvocation implements UnaryMethod<Message, Message> {

    private static final Logger log = LoggerFactory.getLogger(ServerInvocation.class);

    private final Object        serviceToInvoke;
    private final Method        method;

    public ServerInvocation(Object serviceToInvoke, Method method){
        this.serviceToInvoke = serviceToInvoke;
        this.method = method;
    }

    @Override
    public void invoke(Message request, StreamObserver<Message> responseObserver) {
        try {
            String remoteAddress = RpcContext.getContext().getAttachment(SalukiConstants.REMOTE_ADDRESS);
            log.debug(String.format("receiver %s request from %s", new Gson().toJson(request), remoteAddress));
            Class<?> requestType = ReflectUtil.getTypedReq(method);
            Object req = PojoProtobufUtils.Protobuf2Pojo(request, requestType);
            Object[] requestParams = new Object[] { req };
            Object response = method.invoke(serviceToInvoke, requestParams);
            Message message = PojoProtobufUtils.Pojo2Protobuf(response);
            responseObserver.onNext(message);
            responseObserver.onCompleted();
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            // 由于反射调用method，获得的异常都是经过反射异常包装过的，所以我们需要取target error
            Throwable target = e.getCause();
            if (log.isInfoEnabled()) {
                log.info(target.getMessage(), target);
            }
            RpcServiceException rpcBizError = new RpcServiceException(target);
            StatusRuntimeException statusException = Status.INTERNAL.withDescription(rpcBizError.getMessage())//
                                                                    .withCause(rpcBizError).asRuntimeException();
            responseObserver.onError(statusException);
        } catch (ProtobufException e) {
            RpcFrameworkException rpcFramworkError = new RpcFrameworkException(e);
            StatusRuntimeException statusException = Status.INTERNAL.withDescription(rpcFramworkError.getMessage())//
                                                                    .withCause(rpcFramworkError).asRuntimeException();
            responseObserver.onError(statusException);
        }
    }

}
