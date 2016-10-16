package com.quancheng.saluki.core.grpc.client;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.cache.Cache;
import com.google.protobuf.GeneratedMessageV3;
import com.quancheng.saluki.core.common.SalukiConstants;
import com.quancheng.saluki.core.utils.ReflectUtil;

import io.grpc.Channel;
import io.grpc.MethodDescriptor;

public class GrpcStubClient<T> extends AbstractProtocolClient<T> {

    public GrpcStubClient(Cache<String, Channel> channelCache, String protocol, Class<?> protocolClass,
                          Callable<Channel> channelCallable, int rpcTimeout, int callType){
        super(channelCache, protocol, protocolClass, channelCallable, rpcTimeout, callType);
    }

    @Override
    public T getClient() {
        String protocol = getProtocolClzz().getName();
        if (StringUtils.contains(protocol, "$")) {
            try {
                String parentName = StringUtils.substringBefore(protocol, "$");
                Class<?> clzz = ReflectUtil.name2class(parentName);
                Method method;
                switch (this.getCallType()) {
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
                T value = (T) method.invoke(null, getChannel());
                return value;
            } catch (Exception e) {
                throw new IllegalArgumentException("stub definition not correct，do not edit proto generat file", e);
            }
        } else {
            throw new IllegalArgumentException("stub definition not correct，do not edit proto generat file");
        }
    }

    @Override
    protected MethodDescriptor<GeneratedMessageV3, GeneratedMessageV3> doCreateMethodDesc(Method method,
                                                                                          Object[] args) {
        return null;
    }

    @Override
    protected Pair<GeneratedMessageV3, Class<?>> doProcessArgs(Method method, Object[] args) throws Throwable {
        return null;
    }

}
