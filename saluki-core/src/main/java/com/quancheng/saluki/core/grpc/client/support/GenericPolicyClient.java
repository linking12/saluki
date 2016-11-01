package com.quancheng.saluki.core.grpc.client.support;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.quancheng.saluki.core.common.SalukiURL;
import com.quancheng.saluki.core.grpc.client.GrpcProtocolClient;
import com.quancheng.saluki.core.grpc.client.GrpcRequest;
import com.quancheng.saluki.core.grpc.client.SalukiClassLoader;
import com.quancheng.saluki.core.grpc.service.GenericService;
import com.quancheng.saluki.core.utils.ClassHelper;
import com.quancheng.saluki.core.utils.ReflectUtil;

/**
 * <strong>描述：提供泛化代理对象方式的client/strong><br>
 * <strong>功能：提供动态代理对象</strong><br>
 * <strong>使用场景：存在接口</strong><br>
 * <strong>注意事项：</strong>
 * <ul>
 * <li></li>
 * </ul>
 * 
 * @author shimingliu 2016年10月18日 上午9:40:59
 * @version GenericPolicyClient.java, v 0.0.1 2016年10月18日 上午9:40:59 shimingliu
 */
public class GenericPolicyClient<T> implements GrpcProtocolClient<T> {

    private final Map<String, Integer> methodRetries;

    private final SalukiClassLoader    classLoader;

    private final SalukiURL            refUrl;

    public GenericPolicyClient(SalukiClassLoader classLoader, Map<String, Integer> methodRetries, SalukiURL refUrl){
        this.classLoader = classLoader;
        this.methodRetries = methodRetries;
        this.refUrl = refUrl;
    }

    public Class<?> doLoadClass(String className) {
        try {
            classLoader.addClassPath();
            return classLoader.loadClass(className);
        } catch (Exception e) {
            throw new IllegalArgumentException("grpc  responseType must instanceof com.google.protobuf.GeneratedMessageV3",
                                               new ClassNotFoundException("Class " + className + " not found"));
        }

    }

    @SuppressWarnings("unchecked")
    @Override
    public T getGrpcClient(GrpcProtocolClient.ChannelCall call, int callType, int callTimeout) {
        return (T) Proxy.newProxyInstance(ClassHelper.getClassLoader(), new Class[] { GenericService.class },
                                          new ClientInvocation(call, callType, callTimeout));
    }

    private class ClientInvocation extends AbstractClientInvocation {

        private final GrpcProtocolClient.ChannelCall call;
        private final int                            callType;
        private final int                            callTimeout;

        public ClientInvocation(com.quancheng.saluki.core.grpc.client.GrpcProtocolClient.ChannelCall call, int callType,
                                int callTimeout){
            super(GenericPolicyClient.this.methodRetries, GenericPolicyClient.this.refUrl);
            this.call = call;
            this.callType = callType;
            this.callTimeout = callTimeout;
        }

        @Override
        protected GrpcRequest buildGrpcRequest(Method method, Object[] args) {
            if (args.length != 4) {
                throw new IllegalArgumentException("generic call args invlid" + args + " args " + args);
            }
            GrpcRequest request = new GrpcRequest.Default(this.getServiceName(args), GenericService.class, call);
            GrpcRequest.MethodRequest methodRequest = new GrpcRequest.MethodRequest(this.getMethod(args),
                                                                                    this.getReqAndRepType(args).get(0),
                                                                                    this.getReqAndRepType(args).get(1),
                                                                                    this.getArg(args), callType,
                                                                                    callTimeout);
            request.setMethodRequest(methodRequest);
            return request;
        }

        private String getServiceName(Object[] args) {
            return (String) args[0];
        }

        private String getMethod(Object[] args) {
            return (String) args[1];
        }

        private List<Class<?>> getReqAndRepType(Object[] args) {
            String[] paramType = (String[]) args[2];
            int length = paramType.length;
            if (length != 2) {
                throw new IllegalArgumentException("generic call request type and response type must transmit"
                                                   + " but length is  " + length);
            }
            List<Class<?>> requestAndResponse = Lists.newArrayList();
            String requestType_ = paramType[0];
            String responseType_ = paramType[1];
            Class<?> requestType;
            Class<?> responseType;
            try {
                requestType = ReflectUtil.name2class(requestType_);
            } catch (ClassNotFoundException e) {
                requestType = GenericPolicyClient.this.doLoadClass(requestType_);
            }
            try {
                responseType = ReflectUtil.name2class(responseType_);
            } catch (ClassNotFoundException e) {
                responseType = GenericPolicyClient.this.doLoadClass(responseType_);
            }
            requestAndResponse.add(requestType);
            requestAndResponse.add(responseType);
            return requestAndResponse;

        }

        private Object getArg(Object[] args) {
            Object[] param = (Object[]) args[3];
            if (param.length != 1) {
                throw new IllegalArgumentException("grpc not support multiple args,args is " + args + " length is "
                                                   + args.length);
            }
            return param[0];

        }

    }
}
