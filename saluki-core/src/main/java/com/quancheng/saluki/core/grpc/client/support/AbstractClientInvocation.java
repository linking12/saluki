package com.quancheng.saluki.core.grpc.client.support;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Ticker;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.protobuf.Message;
import com.quancheng.saluki.core.common.SalukiConstants;
import com.quancheng.saluki.core.common.SalukiURL;
import com.quancheng.saluki.core.grpc.client.GrpcRequest;
import com.quancheng.saluki.core.grpc.client.GrpcResponse;
import com.quancheng.saluki.core.grpc.client.calls.HaClientCalls;
import com.quancheng.saluki.core.grpc.client.calls.RetryOptions;
import com.quancheng.saluki.core.grpc.exception.RpcFrameworkException;
import com.quancheng.saluki.core.grpc.exception.RpcServiceException;
import com.quancheng.saluki.core.grpc.monitor.MonitorService;
import com.quancheng.saluki.core.utils.ClassHelper;
import com.quancheng.saluki.core.utils.NetUtils;
import com.quancheng.saluki.core.utils.ReflectUtil;
import com.quancheng.saluki.serializer.exception.ProtobufException;

import io.grpc.Channel;
import io.grpc.MethodDescriptor;

/**
 * <strong>描述：</strong>TODO 描述 <br>
 * <strong>功能：</strong><br>
 * <strong>使用场景：</strong><br>
 * <strong>注意事项：</strong>
 * <ul>
 * <li></li>
 * </ul>
 * 
 * @author shimingliu 2016年10月18日 下午11:20:15
 * @version $Id: AbstractClientInvocation.java, v 0.0.1 2016年10月18日 下午11:20:15 shimingliu Exp $
 */
public abstract class AbstractClientInvocation implements InvocationHandler {

    private static final Logger                        log         = LoggerFactory.getLogger(AbstractClientInvocation.class);

    private final List<MonitorService>                 monitors;

    private final Cache<String, Channel>               channelCache;

    private final Map<String, Integer>                 methodRetries;

    private final SalukiURL                            refUrl;

    private final ConcurrentMap<String, AtomicInteger> concurrents = new ConcurrentHashMap<String, AtomicInteger>();

    public AbstractClientInvocation(Map<String, Integer> methodRetries, SalukiURL refUrl){
        this.monitors = this.findMonitor();
        this.channelCache = CacheBuilder.newBuilder()//
                                        .maximumSize(1000L)//
                                        .softValues()//
                                        .ticker(Ticker.systemTicker())//
                                        .build();
        this.methodRetries = methodRetries;
        this.refUrl = refUrl;
    }

    private Channel getChannel(GrpcRequest request) {
        try {
            return channelCache.get(request.getServiceName(), new Callable<Channel>() {

                @Override
                public Channel call() throws Exception {
                    return request.getChannel();
                }
            });
        } catch (ExecutionException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    protected abstract GrpcRequest buildGrpcRequest(Method method, Object[] args);

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (ReflectUtil.isToStringMethod(method)) {
            return AbstractClientInvocation.this.toString();
        }
        GrpcRequest request = buildGrpcRequest(method, args);
        // 准备Grpc参数begin
        String serviceName = request.getServiceName();
        String methodName = request.getMethodRequest().getMethodName();
        Message reqProtoBufer = null;
        Message respProtoBufer = null;
        MethodDescriptor<Message, Message> methodDesc = request.getMethodDescriptor();
        int timeOut = request.getMethodRequest().getCallTimeout();
        // 准备Grpc调用参数end
        Channel channel = getChannel(request);
        RetryOptions retryConfig = createRetryOption(methodName);
        HaClientCalls grpcClient = new HaClientCalls.Default(channel, retryConfig);
        long start = System.currentTimeMillis();
        getConcurrent(serviceName, methodName).incrementAndGet();
        try {
            reqProtoBufer = request.getRequestArg();
            switch (request.getMethodRequest().getCallType()) {
                case SalukiConstants.RPCTYPE_ASYNC:
                    respProtoBufer = grpcClient.unaryFuture(reqProtoBufer, methodDesc).get(timeOut, TimeUnit.SECONDS);
                    break;
                case SalukiConstants.RPCTYPE_BLOCKING:
                    respProtoBufer = grpcClient.blockingUnaryResult(reqProtoBufer, methodDesc);
                    break;
                default:
                    respProtoBufer = grpcClient.unaryFuture(reqProtoBufer, methodDesc).get(timeOut, TimeUnit.SECONDS);
                    break;
            }
            Class<?> respPojoType = request.getMethodRequest().getResponseType();
            GrpcResponse response = new GrpcResponse.Default(respProtoBufer, respPojoType);
            Object respPojo = response.getResponseArg();
            // 收集监控信息
            collect(serviceName, methodName, reqProtoBufer, respProtoBufer, grpcClient.getRemoteAddress(), start,
                    false);
            return respPojo;
        } catch (ProtobufException | InterruptedException | ExecutionException | TimeoutException e) {
            collect(serviceName, methodName, reqProtoBufer, respProtoBufer, grpcClient.getRemoteAddress(), start, true);
            if (e instanceof ProtobufException) {
                RpcFrameworkException rpcFramwork = new RpcFrameworkException(e);
                throw rpcFramwork;
            } else {
                RpcServiceException rpcService = new RpcServiceException(e);
                throw rpcService;
            }
        } finally {
            getConcurrent(serviceName, methodName).decrementAndGet();
        }
    }

    private RetryOptions createRetryOption(String methodName) {
        if (methodRetries.size() == 1 && methodRetries.containsKey("*")) {
            Integer retries = methodRetries.get("*");
            return new RetryOptions(retries, true);
        } else {
            Integer retries = methodRetries.get(methodName);
            if (retries != null) {
                return new RetryOptions(retries, true);
            } else {
                return new RetryOptions(0, false);
            }
        }
    }

    // 信息采集
    private void collect(String serviceName, String methodName, Message request, Message response,
                         SocketAddress remoteAddress, long start, boolean error) {
        try {
            if (monitors == null || monitors.isEmpty()) {
                return;
            }
            // ---- 服务信息获取 ----
            long elapsed = System.currentTimeMillis() - start; // 计算调用耗时
            int concurrent = getConcurrent(serviceName, methodName).get(); // 当前并发数
            String service = serviceName; // 获取服务名称
            String method = methodName; // 获取方法名
            String provider = ((InetSocketAddress) remoteAddress).getHostName();// 服务端主机
            String req = new Gson().toJson(request);// 入参
            String rep = new Gson().toJson(response);// 出参
            String consumerHost = System.getProperty(SalukiConstants.REGISTRY_CLIENT_HOST, NetUtils.getLocalHost());
            for (MonitorService monitor : monitors) {
                monitor.collect(new SalukiURL(SalukiConstants.MONITOR_PROTOCOL, consumerHost, 0, //
                                              service + "/" + method, //
                                              MonitorService.TIMESTAMP, String.valueOf(start), //
                                              MonitorService.APPLICATION, //
                                              refUrl.getGroup(), //
                                              MonitorService.INTERFACE, service, //
                                              MonitorService.METHOD, method, //
                                              MonitorService.PROVIDER, provider, //
                                              error ? MonitorService.FAILURE : MonitorService.SUCCESS, "1", //
                                              MonitorService.ELAPSED, String.valueOf(elapsed), //
                                              MonitorService.CONCURRENT, String.valueOf(concurrent), //
                                              MonitorService.INPUT, req, //
                                              MonitorService.OUTPUT, rep));
            }
        } catch (Throwable t) {
            log.error("Failed to monitor count service " + serviceName + ", cause: " + t.getMessage(), t);
        }
    }

    private AtomicInteger getConcurrent(String servcieName, String methodName) {
        String key = servcieName + "." + methodName;
        AtomicInteger concurrent = concurrents.get(key);
        if (concurrent == null) {
            concurrents.putIfAbsent(key, new AtomicInteger());
            concurrent = concurrents.get(key);
        }
        return concurrent;
    }

    private List<MonitorService> findMonitor() {
        Iterable<MonitorService> candidates = ServiceLoader.load(MonitorService.class, ClassHelper.getClassLoader());
        List<MonitorService> list = Lists.newArrayList();
        for (MonitorService current : candidates) {
            list.add(current);
        }
        return list;
    }

}
