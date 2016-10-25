package com.quancheng.saluki.core.grpc.client;

import org.apache.commons.lang3.StringUtils;

import com.quancheng.saluki.core.common.SalukiConstants;
import com.quancheng.saluki.core.common.SalukiURL;
import com.quancheng.saluki.core.grpc.client.support.DefaultPolicyClient;
import com.quancheng.saluki.core.grpc.client.support.GenericPolicyClient;
import com.quancheng.saluki.core.grpc.client.support.StubPolicyClient;
import com.quancheng.saluki.core.grpc.exception.RpcFrameworkException;
import com.quancheng.saluki.core.utils.ReflectUtil;

import io.grpc.stub.AbstractStub;

/**
 * <strong>描述：提供策略模式来获取不同的client对象</strong><br>
 * <strong>功能：</strong><br>
 * <strong>使用场景：</strong><br>
 * <strong>注意事项：</strong>
 * <ul>
 * <li></li>
 * </ul>
 * 
 * @author shimingliu 2016年10月18日 下午2:57:02
 * @version $Id: GrpcClientContext.java, v 0.0.1 2016年10月18日 下午2:57:02 shimingliu Exp $
 */
public class GrpcClientContext {

    private final GrpcProtocolClient<Object>     grpcClient;

    private final GrpcProtocolClient.ChannelCall call;

    private final int                            callType;

    private final int                            callTimeout;

    public GrpcClientContext(SalukiURL refUrl, GrpcProtocolClient.ChannelCall call){
        this.call = call;
        this.callType = refUrl.getParameter(SalukiConstants.RPCTYPE_KEY, SalukiConstants.RPCTYPE_ASYNC);
        this.callTimeout = refUrl.getParameter(SalukiConstants.RPCTIMEOUT_KEY, SalukiConstants.DEFAULT_TIMEOUT);
        this.grpcClient = buildProtoClient(refUrl);
    }

    public GrpcProtocolClient<Object> buildProtoClient(SalukiURL refUrl) {
        boolean generic = refUrl.getParameter(SalukiConstants.GENERIC_KEY, SalukiConstants.DEFAULT_GENERIC);
        boolean stub = refUrl.getParameter(SalukiConstants.GRPC_STUB_KEY, Boolean.FALSE);
        if (generic) {
            return new GenericPolicyClient<Object>(new SalukiClassLoader());
        } else {
            if (stub) {
                String stubClassName = refUrl.getParameter(SalukiConstants.INTERFACECLASS_KEY);
                try {
                    @SuppressWarnings({ "rawtypes", "unchecked" })
                    Class<? extends AbstractStub> stubClass = (Class<? extends AbstractStub>) ReflectUtil.name2class(stubClassName);
                    return new StubPolicyClient<Object>(stubClass);
                } catch (ClassNotFoundException e) {
                    throw new IllegalArgumentException("grpc stub client the class must exist in classpath", e);
                }
            } else {
                String[] methodNames = StringUtils.split(refUrl.getParameter(SalukiConstants.METHODS_KEY), ",");
                int retries = refUrl.getParameter((SalukiConstants.METHOD_RETRY_KEY), 1);
                String interfaceName = refUrl.getServiceInterface();
                Class<?> interfaceClass = generateClass(interfaceName, methodNames, retries);
                return new DefaultPolicyClient<Object>(interfaceName, interfaceClass);
            }
        }
    }

    private Class<?> generateClass(String interfaceName, String[] methodNames, int reties) {
        try {
            if (methodNames != null && methodNames.length > 1) {
                return ReflectUtil.addHastrategyAnnotation(interfaceName, methodNames, reties);
            } else {
                return ReflectUtil.addHastrategyAnnotation(interfaceName, reties);
            }
        } catch (Exception e) {
            throw new RpcFrameworkException("Can not add com.quancheng.saluki.core.grpc.client.ha.Hastrategy to "
                                            + interfaceName, e);
        }
    }

    public Object getGrpcClient() {
        return grpcClient.getGrpcClient(call, callType, callTimeout);
    }
}
