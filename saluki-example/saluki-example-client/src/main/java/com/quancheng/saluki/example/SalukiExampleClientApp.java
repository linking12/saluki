package com.quancheng.saluki.example;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.quancheng.examples.model.hello.HelloRequest;
import com.quancheng.examples.service.HelloService;
import com.quancheng.saluki.boot.SalukiReference;
import com.saluki.example.model.First;
import com.saluki.example.model.Second;

@SpringBootApplication
public class SalukiExampleClientApp implements CommandLineRunner {

  @SalukiReference(retries = 3, validatorGroups = {First.class, Second.class})
  private HelloService helloService;

  private int threads = 50;
  private int rounds = 10000;

  public static void main(String[] args) {
    SpringApplication.run(SalukiExampleClientApp.class, args);
  }

  @Override
  public void run(String... arg0) throws Exception {

    long start = System.currentTimeMillis();
    Thread[] ts = new Thread[threads];
    for (int i = 0; i < threads; i++) {
      ts[i] = new Thread(new Runnable() {

        @Override
        public void run() {
          HelloRequest request = new HelloRequest();
          request.setName("liushiming");
          com.quancheng.examples.model.hello.Project project =
              new com.quancheng.examples.model.hello.Project();
          project.setId("123");
          Map<String, com.quancheng.examples.model.hello.Project> projects =
              new HashMap<String, com.quancheng.examples.model.hello.Project>();
          projects.put("test", project);
          request.setProjects(projects);
          helloService.sayHello(request);
        }

      });
    }
    for (int i = 0; i < threads; i++) {
      ts[i].start();
    }
    for (int i = 0; i < threads; i++) {
      ts[i].join();
    }
    System.out.println(
        " qps=" + ((long) threads * rounds * 1000 / (System.currentTimeMillis() - start + 1)));
    double ms = (System.currentTimeMillis() - start + 1) / ((double) threads * rounds);
    System.out.println("ms=" + ms);
  }


}
