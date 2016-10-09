package com.quancheng.saluki.core.grpc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

import com.quancheng.saluki.core.common.SalukiConstants;
import com.quancheng.saluki.core.service.GenericService;
import com.quancheng.saluki.core.utils.ClassHelper;
import com.quancheng.saluki.core.utils.ReflectUtil;

import io.grpc.CallOptions;
import io.grpc.ClientCall;
import io.grpc.stub.ClientCalls;

public final class ProtocolProxy<T> {

    private final String              protocol;

    private final GrpcChannelCallable channelCallable;

    private final int                 callType;

    private final int                 rpcTimeout;

    private final boolean             isGeneric;

    private volatile Class<?>         protocolClzz;

    public ProtocolProxy(String protocol, GrpcChannelCallable channelCallBack, int rpcTimeout, int callType,
                         boolean isGeneric) throws ClassNotFoundException{
        this.protocol = protocol;
        this.channelCallable = channelCallBack;
        this.rpcTimeout = rpcTimeout;
        this.callType = callType;
        this.isGeneric = isGeneric;
    }

    public Object getProxy() {
        if (isGeneric) {
            return this.getJavaProxy();
        } else {
            try {
                protocolClzz = ReflectUtil.name2class(protocol);
                boolean isInterface = Modifier.isInterface(protocolClzz.getModifiers());
                if (isInterface) {
                    return this.getJavaProxy();
                } else {
                    return this.getGrpcStub();
                }
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("no class find in classpath", e);
            }
        }
    }

    private T getGrpcStub() {
        String clzName = this.protocol;
        if (StringUtils.contains(clzName, "$")) {
            try {
                String parentName = StringUtils.substringBefore(clzName, "$");
                Class<?> clzz = ReflectUtil.name2class(parentName);
                Method method;
                switch (callType) {
                    case SalukiConstants.RPCTYPE_ASYNC:
                        method = clzz.getMethod("newFutureStub", io.grpc.Channel.class);
                        break;
                    case SalukiConstants.RPCTYPE_BLOCKING:
                        method = clzz.getMethod("newBlockingStub", io.grpc.Channel.class);
                        break;
                    default:
                        method = clzz.getMethod("newFutureStub", io.grpc.Channel.class);
                        break;
                }
                @SuppressWarnings("unchecked")
                T value = (T) method.invoke(null, channelCallable.getGrpcChannel(clzName));
                return value;
            } catch (Exception e) {
                throw new IllegalArgumentException("stub definition not correct，do not edit proto generat file", e);
            }
        } else {
            throw new IllegalArgumentException("stub definition not correct，do not edit proto generat file");
        }
    }

    @SuppressWarnings("unchecked")
    private Object getJavaProxy() {
        Object proxy;
        if (this.isGeneric) {
            proxy = Proxy.newProxyInstance(ClassHelper.getClassLoader(), new Class[] { GenericService.class },
                                           new JavaProxyInvoker());
        } else {
            proxy = (T) Proxy.newProxyInstance(ClassHelper.getClassLoader(), new Class[] { protocolClzz },
                                               new JavaProxyInvoker());
        }
        return proxy;
    }

    private class JavaProxyInvoker implements InvocationHandler {

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            io.grpc.MethodDescriptor<com.google.protobuf.GeneratedMessageV3, com.google.protobuf.GeneratedMessageV3> methodDescriptor;
            if (isGeneric && method.getName().equals("$invoke")) {
                String protocol = (String) args[0];
                String methodName = (String) args[1];
                String[] parameterTypes = (String[]) args[2];
                Object[] param = (Object[]) args[3];
                if (parameterTypes.length != 2) {
                    throw new IllegalArgumentException("generic call,the request and response type must be set "
                                                       + parameterTypes + " length is " + args.length);
                }
                if (param.length > 1) {
                    throw new IllegalArgumentException("grpc not support multiple args,args is " + param + " length is "
                                                       + param.length);
                }
                com.google.protobuf.GeneratedMessageV3[] paramType = concoctParamInstance(parameterTypes);
                methodDescriptor = GrpcUtils.createMethodDescriptor(protocol, methodName, paramType[0], paramType[1]);
            } else {
                if (args.length > 1) {
                    throw new IllegalArgumentException("grpc not support multiple args,args is " + args + " length is "
                                                       + args.length);
                }
                methodDescriptor = GrpcUtils.createMethodDescriptor(protocol, method);
            }
            ClientCall<com.google.protobuf.GeneratedMessageV3, com.google.protobuf.GeneratedMessageV3> newCall = channelCallable.getGrpcChannel(protocol).newCall(methodDescriptor,
                                                                                                                                                                  CallOptions.DEFAULT);
            com.google.protobuf.GeneratedMessageV3 arg = (com.google.protobuf.GeneratedMessageV3) args[0];
            switch (callType) {
                case SalukiConstants.RPCTYPE_ASYNC:
                    return ClientCalls.futureUnaryCall(newCall, arg).get(rpcTimeout, TimeUnit.SECONDS);
                case SalukiConstants.RPCTYPE_BLOCKING:
                    return ClientCalls.blockingUnaryCall(newCall, arg);
                default:
                    return ClientCalls.futureUnaryCall(newCall, arg).get(rpcTimeout, TimeUnit.SECONDS);
            }
        }

        private com.google.protobuf.GeneratedMessageV3[] concoctParamInstance(String[] parameterTypes) {
            com.google.protobuf.GeneratedMessageV3[] paramInstance = new com.google.protobuf.GeneratedMessageV3[parameterTypes.length];
            for (int i = 0; i < parameterTypes.length; i++) {
                String parameterTypeStr = parameterTypes[i];
                try {
                    Class<?> parameterType = ReflectUtil.name2class(parameterTypeStr);
                    if (com.google.protobuf.GeneratedMessageV3.class.isAssignableFrom(parameterType)) {
                        Object obj = ReflectUtil.classInstance(parameterType);
                        paramInstance[i] = (com.google.protobuf.GeneratedMessageV3) obj;
                    } else {
                        throw new IllegalArgumentException("grpc paramter must instanceof com.google.protobuf.GeneratedMessageV3"
                                                           + " but the type is " + parameterType);
                    }
                } catch (ClassNotFoundException e) {
                    throw new IllegalArgumentException("not found paramter in classpath, " + " but the type is "
                                                       + parameterTypeStr);
                }
            }
            return paramInstance;
        }

    }

}
