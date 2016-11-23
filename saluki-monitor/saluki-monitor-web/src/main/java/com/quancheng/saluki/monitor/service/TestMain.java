package com.quancheng.saluki.monitor.service;

public class TestMain {

    public static void main(String[] args) {
        GenericRpcCallService service = new GenericRpcCallService();
        service.init();
        String model = service.getService("com.quancheng.examples.service.HelloService");
        System.out.println(model);

    }

}
