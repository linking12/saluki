package com.quancheng.saluki.core.grpc.client.support;

import java.lang.reflect.Method;

import org.apache.commons.lang3.StringUtils;

import com.quancheng.saluki.core.common.SalukiConstants;
import com.quancheng.saluki.core.grpc.client.GrpcProtocolClient;
import com.quancheng.saluki.core.utils.ReflectUtil;

import io.grpc.Channel;

/**
 * <strong>描述：获取原生Grpc stub方式client</strong><br>
 * <strong>功能：</strong><br>
 * <strong>使用场景：</strong><br>
 * <strong>注意事项：</strong>
 * <ul>
 * <li></li>
 * </ul>
 * 
 * @author shimingliu 2016年10月18日 下午2:54:31
 * @version $Id: StubPolicyClient.java, v 0.0.1 2016年10月18日 下午2:54:31 shimingliu Exp $
 */
public class StubPolicyClient<AbstractStub> implements GrpcProtocolClient<AbstractStub> {

    private final Class<? extends AbstractStub> stubClass;

    public StubPolicyClient(Class<? extends AbstractStub> stubClass){
        this.stubClass = stubClass;
    }

    public String getStubClassName() {
        return this.stubClass.getName();
    }

    @Override
    public AbstractStub getGrpcClient(ChannelCall channelCall, int callType, int callTimeout) {
        String stubClassName = StubPolicyClient.this.getStubClassName();
        if (StringUtils.contains(stubClassName, "$")) {
            try {
                String parentName = StringUtils.substringBefore(stubClassName, "$");
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
                Channel channel = channelCall.getChannel(null, null, null);
                @SuppressWarnings("unchecked")
                AbstractStub stubInstance = (AbstractStub) method.invoke(null, channel);
                return stubInstance;
            } catch (Exception e) {
                throw new IllegalArgumentException("stub definition not correct，do not edit proto generat file", e);
            }
        } else {
            throw new IllegalArgumentException("stub definition not correct，do not edit proto generat file");
        }
    }

}
