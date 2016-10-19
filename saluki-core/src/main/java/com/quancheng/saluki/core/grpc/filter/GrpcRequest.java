package com.quancheng.saluki.core.grpc.filter;

import java.io.Serializable;

import com.quancheng.saluki.core.grpc.client.GrpcProtocolClient;

public class GrpcRequest implements Serializable {

    private static final long              serialVersionUID = 1L;

    private String                         serviceName;

    private Class<?>                       serviceClass;

    private GrpcProtocolClient.ChannelCall call;

    private MethodRequest                  methodRequest;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Class<?> getServiceClass() {
        return serviceClass;
    }

    public void setServiceClass(Class<?> serviceClass) {
        this.serviceClass = serviceClass;
    }

    public GrpcProtocolClient.ChannelCall getCall() {
        return call;
    }

    public void setCall(GrpcProtocolClient.ChannelCall call) {
        this.call = call;
    }

    public MethodRequest getMethodRequest() {
        return methodRequest;
    }

    public void setMethodRequest(MethodRequest methodRequest) {
        this.methodRequest = methodRequest;
    }

    public static class MethodRequest {

        private String   methodName;

        private Class<?> requestType;

        private Class<?> responseType;

        private Object   arg;

        private int      callType;

        private int      callTimeout;

        public String getMethodName() {
            return methodName;
        }

        public void setMethodName(String methodName) {
            this.methodName = methodName;
        }

        public Class<?> getRequestType() {
            return requestType;
        }

        public void setRequestType(Class<?> requestType) {
            this.requestType = requestType;
        }

        public Class<?> getResponseType() {
            return responseType;
        }

        public void setResponseType(Class<?> responseType) {
            this.responseType = responseType;
        }

        public Object getArg() {
            return arg;
        }

        public void setArg(Object arg) {
            this.arg = arg;
        }

        public int getCallType() {
            return callType;
        }

        public void setCallType(int callType) {
            this.callType = callType;
        }

        public int getCallTimeout() {
            return callTimeout;
        }

        public void setCallTimeout(int callTimeout) {
            this.callTimeout = callTimeout;
        }

    }
}
