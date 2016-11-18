package com.quancheng.saluki.core.grpc.server.support;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.protobuf.Message;
import com.quancheng.saluki.core.common.RpcContext;
import com.quancheng.saluki.core.common.SalukiConstants;
import com.quancheng.saluki.core.common.SalukiURL;
import com.quancheng.saluki.core.grpc.exception.RpcFrameworkException;
import com.quancheng.saluki.core.grpc.exception.RpcServiceException;
import com.quancheng.saluki.core.grpc.monitor.MonitorService;
import com.quancheng.saluki.core.grpc.utils.PojoProtobufUtils;
import com.quancheng.saluki.core.utils.ClassHelper;
import com.quancheng.saluki.core.utils.NetUtils;
import com.quancheng.saluki.core.utils.ReflectUtil;
import com.quancheng.saluki.serializer.exception.ProtobufException;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.ServerCalls.UnaryMethod;
import io.grpc.stub.StreamObserver;

public class ServerInvocation implements UnaryMethod<Message, Message> {

    private static final Logger                        log         = LoggerFactory.getLogger(ServerInvocation.class);
    private final List<MonitorService>                 monitors;
    private final Object                               serviceToInvoke;
    private final Method                               method;
    private final SalukiURL                            providerUrl;
    private final ConcurrentMap<String, AtomicInteger> concurrents = new ConcurrentHashMap<String, AtomicInteger>();

    public ServerInvocation(Object serviceToInvoke, Method method, SalukiURL providerUrl){
        this.serviceToInvoke = serviceToInvoke;
        this.method = method;
        this.monitors = this.findMonitor();
        this.providerUrl = providerUrl;
    }

    @Override
    public void invoke(Message request, StreamObserver<Message> responseObserver) {
        Message reqProtoBufer = request;
        Message respProtoBufer = null;
        long start = System.currentTimeMillis();
        getConcurrent().incrementAndGet();
        try {
            String remoteAddress = RpcContext.getContext().getAttachment(SalukiConstants.REMOTE_ADDRESS);
            log.debug(String.format("receiver %s request from %s", new Gson().toJson(reqProtoBufer), remoteAddress));
            Class<?> requestType = ReflectUtil.getTypedReq(method);
            Object reqPojo = PojoProtobufUtils.Protobuf2Pojo(reqProtoBufer, requestType);
            Object[] requestParams = new Object[] { reqPojo };
            Object respPojo = method.invoke(serviceToInvoke, requestParams);
            respProtoBufer = PojoProtobufUtils.Pojo2Protobuf(respPojo);
            collect(reqProtoBufer, respProtoBufer, start, false);
            responseObserver.onNext(respProtoBufer);
            responseObserver.onCompleted();
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            collect(reqProtoBufer, respProtoBufer, start, true);
            // 由于反射调用method，获得的异常都是经过反射异常包装过的，所以我们需要取target error
            Throwable target = e.getCause();
            if (log.isInfoEnabled()) {
                log.info(target.getMessage(), target);
            }
            RpcServiceException rpcBizError = new RpcServiceException(target);
            StatusRuntimeException statusException = Status.INTERNAL.withDescription(rpcBizError.getMessage())//
                                                                    .withCause(rpcBizError).asRuntimeException();
            responseObserver.onError(statusException);
        } catch (ProtobufException e) {
            RpcFrameworkException rpcFramworkError = new RpcFrameworkException(e);
            StatusRuntimeException statusException = Status.INTERNAL.withDescription(rpcFramworkError.getMessage())//
                                                                    .withCause(rpcFramworkError).asRuntimeException();
            responseObserver.onError(statusException);
        } finally {
            getConcurrent().decrementAndGet();
        }
    }

    // 信息采集

    private void collect(Message request, Message response, long start, boolean error) {
        try {
            if (monitors == null || monitors.isEmpty()) {
                return;
            }
            long elapsed = System.currentTimeMillis() - start; // 计算调用耗时
            int concurrent = getConcurrent().get(); // 当前并发数
            String service = providerUrl.getServiceInterface(); // 获取服务名称
            String method = this.method.getName(); // 获取方法名
            String consumer = RpcContext.getContext().getAttachment(SalukiConstants.REMOTE_ADDRESS);// 远程服务器地址
            String serverInfo = System.getProperty(SalukiConstants.REGISTRY_SERVER_PARAM);
            @SuppressWarnings("unchecked")
            Map<String, String> clientParam = new Gson().fromJson(serverInfo, Map.class);
            String serverhost = clientParam.get("serverHost");
            String host = serverhost != null ? serverhost : NetUtils.getLocalHost();
            String registryRealPort = Integer.valueOf(providerUrl.getPort()).toString();
            String registryPort = System.getProperty(SalukiConstants.REGISTRY_SERVER_PORT, registryRealPort);
            for (MonitorService monitor : monitors) {
                monitor.collect(new SalukiURL(SalukiConstants.MONITOR_PROTOCOL, host, //
                                              Integer.valueOf(registryPort), //
                                              service + "/" + method, //
                                              MonitorService.TIMESTAMP, String.valueOf(start), //
                                              MonitorService.APPLICATION, providerUrl.getGroup(), //
                                              MonitorService.INTERFACE, service, //
                                              MonitorService.METHOD, method, //
                                              MonitorService.CONSUMER, consumer, //
                                              error ? MonitorService.FAILURE : MonitorService.SUCCESS, "1", //
                                              MonitorService.ELAPSED, String.valueOf(elapsed), //
                                              MonitorService.CONCURRENT, String.valueOf(concurrent), //
                                              MonitorService.INPUT, String.valueOf(request.getSerializedSize()), //
                                              MonitorService.OUTPUT, String.valueOf(response.getSerializedSize())));
            }
        } catch (Throwable t) {
            log.error("Failed to monitor count service " + this.serviceToInvoke.getClass() + ", cause: "
                      + t.getMessage(), t);
        }
    }

    // 获取并发计数器
    private AtomicInteger getConcurrent() {
        String key = serviceToInvoke.getClass().getName() + "." + method.getName();
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
