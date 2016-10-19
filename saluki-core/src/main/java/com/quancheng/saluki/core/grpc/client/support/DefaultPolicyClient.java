package com.quancheng.saluki.core.grpc.client.support;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

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

    private final String   interfaceName;

    private final Class<?> interfaceClass;

    public DefaultPolicyClient(String interfaceName){
        this.interfaceName = interfaceName;
        try {
            this.interfaceClass = ReflectUtil.name2class(interfaceName);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    public String getFullServiceName() {
        return this.interfaceName;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T getGrpcClient(GrpcProtocolClient.ChannelCall call, int callType, int callTimeout) {
        GrpcRequest request = this.buildGrpcRequest(call, callType, callTimeout);
        return (T) Proxy.newProxyInstance(ClassHelper.getClassLoader(), new Class[] { interfaceClass },
                                          new ClientInvocation(request));
    }

    private GrpcRequest buildGrpcRequest(GrpcProtocolClient.ChannelCall call, int callType, int callTimeout) {
        GrpcRequest request = new GrpcRequest();
        request.setCall(call);
        request.setServiceName(this.interfaceName);
        request.setServiceClass(this.interfaceClass);
        GrpcRequest.MethodRequest methodRequest = new GrpcRequest.MethodRequest();
        methodRequest.setCallType(callType);
        methodRequest.setCallTimeout(callTimeout);
        request.setMethodRequest(methodRequest);
        return request;
    }

    private class ClientInvocation extends AbstractClientInvocation {

        public ClientInvocation(GrpcRequest request){
            super(request);
        }

        @Override
        protected void doReBuildRequest(Method method, Object[] args) {
            boolean isNeglectMethod = ReflectUtil.neglectMethod(method);
            if (isNeglectMethod) {
                throw new IllegalArgumentException("remote call type do not support this method " + method.getName());
            }
            if (args.length != 1) {
                throw new IllegalArgumentException("grpc not support multiple args,args is " + args + " length is "
                                                   + args.length);
            }
            // one way
            Object arg = args[0];
            super.getRequest().getMethodRequest().setArg(arg);
            super.getRequest().getMethodRequest().setMethodName(method.getName());
            super.getRequest().getMethodRequest().setRequestType(arg.getClass());
            super.getRequest().getMethodRequest().setResponseType(method.getReturnType());
        }

    }

}
