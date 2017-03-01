/*
 * Copyright (c) 2017, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.springsupport;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.quancheng.examples.model.hello.HelloReply;
import com.quancheng.examples.model.hello.HelloRequest;
import com.quancheng.examples.service.HelloService;

/**
 * @author shimingliu 2017年3月1日 上午11:18:20
 * @version TestMain.java, v 0.0.1 2017年3月1日 上午11:18:20 shimingliu
 */
public class TestMain {

    public static void main(String[] args) {
        ClassPathXmlApplicationContext ac = new ClassPathXmlApplicationContext("test.xml");
        HelloService helloService = (HelloService) ac.getBean("remoteService");
        HelloRequest request = new HelloRequest();
        request.setName("liushiming");
        HelloReply reply = helloService.sayHello(request);
        System.out.println(reply);

    }

}
