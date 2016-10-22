package com.quancheng.boot.starter.clientproxy;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Maps;
import com.quancheng.boot.saluki.starter.SalukiReference;
import com.quancheng.boot.starter.model.HelloReply;
import com.quancheng.boot.starter.server.GreeterService;
import com.quancheng.saluki.core.common.RpcContext;
import com.quancheng.saluki.core.common.SalukiConstants;
import com.quancheng.saluki.core.common.SalukiURL;
import com.quancheng.saluki.core.grpc.ha.RegistryDirectory;

import io.grpc.Attributes;
import io.grpc.ResolvedServerInfo;

@RestController
@RequestMapping("/greeter")
public class GreeterServiceController {

    @SalukiReference(service = "com.quancheng.boot.starter.server.GreeterService", group = "Default", version = "1.0.0")
    private GreeterService greeterService;

    @RequestMapping
    public HelloReply view() {
        Map<String, String> param = Maps.newHashMap();
        param.put(SalukiConstants.GROUP_KEY, "Default");
        param.put(SalukiConstants.VERSION_KEY, "1.0.0");
        SalukiURL url = new SalukiURL(SalukiConstants.DEFATULT_PROTOCOL, "localhost", 0,
                                      "com.quancheng.boot.starter.server.GreeterService");
        SocketAddress address = new InetSocketAddress("192.168.99.1", 12201);
        ResolvedServerInfo serverinfo = new ResolvedServerInfo(address, Attributes.EMPTY);
        RegistryDirectory.getInstance().remove(url, serverinfo);
        
        com.quancheng.boot.starter.model.HelloRequest request = new com.quancheng.boot.starter.model.HelloRequest();
        request.setName("liushiming");
        RpcContext.getContext().set("123", "helloworld");
        com.quancheng.boot.starter.model.HelloReply reply = greeterService.SayHello(request);
        return reply;
    }
}
