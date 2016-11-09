package com.quancheng.saluki.core.grpc.client;

import java.io.Serializable;

import com.google.protobuf.Message;
import com.quancheng.saluki.core.grpc.utils.MethodDescriptorUtils;
import com.quancheng.saluki.core.grpc.utils.PojoProtobufUtils;
import com.quancheng.saluki.serializer.exception.ProtobufException;

import io.grpc.Channel;
import io.grpc.MethodDescriptor;

public interface GrpcRequest {

    public Message getRequestArg() throws ProtobufException;

    public MethodDescriptor<Message, Message> getMethodDescriptor();

    public Channel getChannel();

    public String getServiceName();

    public Class<?> getServiceClass();

    public GrpcProtocolClient.ChannelCall getCall();

    public MethodRequest getMethodRequest();

    public void setMethodRequest(MethodRequest methodRequest);

    public static class Default implements GrpcRequest, Serializable {

        private static final long serialVersionUID = 1L;

        public Default(String serviceName, Class<?> serviceClass, GrpcProtocolClient.ChannelCall call){
            super();
            this.serviceName = serviceName;
            this.serviceClass = serviceClass;
            this.call = call;
        }

        public Message getRequestArg() throws ProtobufException {
            Object arg = this.getMethodRequest().getArg();
            return PojoProtobufUtils.Pojo2Protobuf(arg);
        }

        public MethodDescriptor<Message, Message> getMethodDescriptor() {
            Message argsReq = MethodDescriptorUtils.buildDefaultInstance(this.getMethodRequest().getRequestType());
            Message argsRep = MethodDescriptorUtils.buildDefaultInstance(this.getMethodRequest().getResponseType());
            return MethodDescriptorUtils.createMethodDescriptor(this.getServiceName(),
                                                                this.getMethodRequest().getMethodName(), argsReq,
                                                                argsRep);
        }

        public Channel getChannel() {
            return this.getCall().getChannel();
        }

        private final String                         serviceName;

        private final Class<?>                       serviceClass;

        private final GrpcProtocolClient.ChannelCall call;

        private MethodRequest                        methodRequest;

        public String getServiceName() {
            return serviceName;
        }

        public Class<?> getServiceClass() {
            return serviceClass;
        }

        public GrpcProtocolClient.ChannelCall getCall() {
            return call;
        }

        public MethodRequest getMethodRequest() {
            return methodRequest;
        }

        public void setMethodRequest(MethodRequest methodRequest) {
            this.methodRequest = methodRequest;
        }

    }

    public static class MethodRequest implements Serializable {

        private static final long serialVersionUID = 5280935790994972153L;

        private final String      methodName;

        private final Class<?>    requestType;

        private final Class<?>    responseType;

        private final Object      arg;

        private final int         callType;

        private final int         callTimeout;

        public MethodRequest(String methodName, Class<?> requestType, Class<?> responseType, Object arg, int callType,
                             int callTimeout){
            super();
            this.methodName = methodName;
            this.requestType = requestType;
            this.responseType = responseType;
            this.arg = arg;
            this.callType = callType;
            this.callTimeout = callTimeout;
        }

        public String getMethodName() {
            return methodName;
        }

        public Class<?> getRequestType() {
            return requestType;
        }

        public Class<?> getResponseType() {
            return responseType;
        }

        public Object getArg() {
            return arg;
        }

        public int getCallType() {
            return callType;
        }

        public int getCallTimeout() {
            return callTimeout;
        }

    }

}
