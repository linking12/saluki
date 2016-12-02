package com.quancheng.saluki.example.client.genric;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.quancheng.boot.saluki.starter.SalukiReference;
import com.quancheng.saluki.core.grpc.service.GenericService;
import com.quancheng.test.model.user.UserCreateResponse;

@RestController
@RequestMapping("/genric")
public class GenricServiceController {

    @SalukiReference(service = "com.quancheng.saluki.core.grpc.service.GenericService", group = "monitor", version = "1.0.0")
    private GenericService genricService;

    @RequestMapping("/hello")
    public com.quancheng.test.model.user.UserCreateResponse view() {
        String serviceName = "com.quancheng.test.service.UserService";
        String method = "create";
        String[] parameterTypes = new String[] { "com.quancheng.test.model.user.UserCreateRequest",
                                                 "com.quancheng.test.model.user.UserCreateResponse" };
        com.quancheng.test.model.user.UserCreateRequest request = new com.quancheng.test.model.user.UserCreateRequest();
        request.setName("liushiming");
        Object[] args1 = new Object[] { request };
        UserCreateResponse reply = (UserCreateResponse) genricService.$invoke(serviceName, "Example", "1.0.0", method,
                                                                              parameterTypes, args1);
        return reply;
    }
}
