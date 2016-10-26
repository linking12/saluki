package com.quancheng.saluki.core.grpc.client.support;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

import com.quancheng.saluki.core.grpc.client.GrpcProtocolClient;
import com.quancheng.saluki.core.grpc.filter.GrpcRequest;
import com.quancheng.saluki.core.utils.ClassHelper;
import com.quancheng.saluki.core.utils.ReflectUtil;

/**
 * <strong>描述：提供动态代理对象方式的client/strong><br>
 * <strong>功能：提供动态代理对象</strong><br>
 * <strong>使用场景：存在接口</strong><br>
 * <strong>注意事项：</strong>
 * <ul>
 * <li></li>
 * </ul>
 * 
 * @author shimingliu 2016年10月18日 上午9:40:59
 * @version DefaultPolicyClient.java, v 0.0.1 2016年10月18日 上午9:40:59 shimingliu
 */
public class DefaultPolicyClient<T> implements GrpcProtocolClient<T> {

    private final Map<String, Integer> methodRetries;

    private final String               interfaceName;

    private final Class<?>             interfaceClass;

    public DefaultPolicyClient(String interfaceName, Map<String, Integer> methodRetries){
        this.interfaceName = interfaceName;
        try {
            this.interfaceClass = ReflectUtil.name2class(interfaceName);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
        this.methodRetries = methodRetries;
    }

    public String getFullServiceName() {
        return this.interfaceName;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T getGrpcClient(GrpcProtocolClient.ChannelCall call, int callType, int callTimeout) {
        return (T) Proxy.newProxyInstance(ClassHelper.getClassLoader(), new Class[] { interfaceClass },
                                          new ClientInvocation(call, callType, callTimeout));
    }

    private class ClientInvocation extends AbstractClientInvocation {

        private final GrpcProtocolClient.ChannelCall call;
        private final int                            callType;
        private final int                            callTimeout;

        public ClientInvocation(com.quancheng.saluki.core.grpc.client.GrpcProtocolClient.ChannelCall call, int callType,
                                int callTimeout){
            super(DefaultPolicyClient.this.methodRetries);
            this.call = call;
            this.callType = callType;
            this.callTimeout = callTimeout;
        }

        @Override
        protected GrpcRequest buildGrpcRequest(Method method, Object[] args) {
            boolean isNeglectMethod = ReflectUtil.neglectMethod(method);
            if (isNeglectMethod) {
                throw new IllegalArgumentException("remote call type do not support this method " + method.getName());
            }
            if (args.length != 1) {
                throw new IllegalArgumentException("grpc not support multiple args,args is " + args + " length is "
                                                   + args.length);
            }
            Object arg = args[0];
            GrpcRequest request = new GrpcRequest.Default(interfaceName, interfaceClass, call);
            GrpcRequest.MethodRequest methodRequest = new GrpcRequest.MethodRequest(method.getName(), arg.getClass(),
                                                                                    method.getReturnType(), arg,
                                                                                    callType, callTimeout);
            request.setMethodRequest(methodRequest);
            return request;
        }

    }

}
