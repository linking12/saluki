package com.quancheng.saluki.example.client.stub;

import java.util.concurrent.ExecutionException;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.quancheng.examples.model.hello.HelloReply;
import com.quancheng.examples.service.HelloServiceGrpc;
import com.quancheng.saluki.boot.SalukiReference;
import com.quancheng.saluki.core.grpc.util.SerializerUtils;
import com.quancheng.saluki.serializer.exception.ProtobufException;

@RestController
@RequestMapping("/stub")
public class StubServiceController {

    @SalukiReference(service = "com.quancheng.examples.service.HelloService", group = "monitor", version = "1.0.0")
    private HelloServiceGrpc.HelloServiceFutureStub helloServiceStub;

    @RequestMapping("/hello")
    public HelloReply view() throws ProtobufException, InterruptedException, ExecutionException {
        com.quancheng.examples.model.Hello.HelloRequest request = com.quancheng.examples.model.Hello.HelloRequest.newBuilder().setName("liushiming").build();
        com.quancheng.examples.model.Hello.HelloReply reply = helloServiceStub.sayHello(request).get();
        return (HelloReply) SerializerUtils.Protobuf2Pojo(reply, com.quancheng.examples.model.hello.HelloReply.class);
    }
}
