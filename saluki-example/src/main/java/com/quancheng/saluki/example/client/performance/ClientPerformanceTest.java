package com.quancheng.saluki.example.client.performance;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.Assert;

import com.quancheng.boot.saluki.starter.SalukiReference;
import com.quancheng.examples.model.hello.HelloRequest;
import com.quancheng.examples.service.HelloService;
import com.quancheng.saluki.example.client.ClientApp;

@SpringBootApplication
public class ClientPerformanceTest implements CommandLineRunner {

    @SalukiReference(service = "com.quancheng.examples.service.HelloService", group = "Default", version = "1.0.0")
    private HelloService     helloService;
    /**
     * 单次循环
     */
    private static final int CIRCLE = Integer.valueOf(System.getProperty("circle", "10000"));

    private static String build(int length) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < length * 1024; i++) {
            buffer.append("-");
        }
        return buffer.toString();
    }

    public static void main(String[] args) {

        SpringApplication.run(ClientApp.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Input the params: (request size(KB)) (thread number) (circle times) (warming) >> ");
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        String[] params = input.readLine().split(" ");
        int request = Integer.valueOf(params[0]);
        int threads = Integer.valueOf(params[1]);
        int circles = Integer.valueOf(params[2]);
        Assert.isTrue(circles >= ClientPerformanceTest.CIRCLE * 2);
        Assert.isTrue(circles % ClientPerformanceTest.CIRCLE == 0);
        String buffer = ClientPerformanceTest.build(request);
        // 预热
        for (int index = 0; index < circles / ClientPerformanceTest.CIRCLE; index++) {
            HelloRequest helloRequest = new HelloRequest();
            helloRequest.setName(buffer);
            helloService.sayHello(helloRequest);
        }
        System.gc();
        Thread.sleep(5000);
        System.out.println("---------------------[START]---------------------");
        AtomicLong counter = new AtomicLong(circles);
        InvokerRunnable invoker = new InvokerRunnable(helloService, counter, buffer);
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        // 计时
        long start = System.nanoTime();
        for (int index = 0; index < circles / ClientPerformanceTest.CIRCLE; index++) {
            executor.execute(invoker);
        }
        synchronized (counter) {
            while (counter.get() != 0) {
                counter.wait();
            }
        }
        long using = System.nanoTime() - start;
        // 完成数量 / 耗时
        long qps = (long) (circles / Double.valueOf(TimeUnit.MILLISECONDS.convert(using, TimeUnit.NANOSECONDS)) * 1000);
        // 耗时 / 完成数量
        double per = (Double.valueOf(using) / circles) / 1000 / 1000;
        System.out.println("Using: " + using);
        System.out.println("Qps: " + qps);
        System.out.println("Per: " + per);
    }

    private static class InvokerRunnable implements Runnable {

        private final HelloService helloService;

        private final AtomicLong   counter;

        private final String       buffer;

        public InvokerRunnable(HelloService helloService, AtomicLong counter, String buffer){
            super();
            this.helloService = helloService;
            this.counter = counter;
            this.buffer = buffer;
        }

        public void run() {
            for (int index = 0; index < ClientPerformanceTest.CIRCLE; index++) {
                HelloRequest helloRequest = new HelloRequest();
                helloRequest.setName(buffer);
                this.helloService.sayHello(helloRequest);
            }
            this.counter.addAndGet(-ClientPerformanceTest.CIRCLE);
            if (this.counter.get() == 0) {
                synchronized (this.counter) {
                    this.counter.notifyAll();
                }
            }
        }
    }
}
