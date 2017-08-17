package com.quancheng.saluki.example.client;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.quancheng.examples.model.hello.HelloReply;
import com.quancheng.examples.model.hello.HelloRequest;
import com.quancheng.examples.service.HelloService;
import com.quancheng.saluki.boot.SalukiReference;
import com.quancheng.saluki.core.common.RpcContext;
import com.saluki.example.model.First;
import com.saluki.example.model.Second;

import io.grpc.stub.StreamObserver;

@RestController
@RequestMapping("/proxy")
public class ProxyServiceController {


  @SalukiReference(retries = 3, validatorGroups = {First.class, Second.class})
  private HelloService helloService;


  @RequestMapping("/hello")
  public HelloReply hello(@RequestParam(value = "name", required = false) String name) {
    return call(name);
  }


  @RequestMapping("/hellostream")
  public HelloReply hellostream(@RequestParam(value = "name", required = false) String name) {
    HelloRequest request = new HelloRequest();
    request.setName(name);
    com.quancheng.examples.model.hello.Project project =
        new com.quancheng.examples.model.hello.Project();
    project.setId("123");
    Map<String, com.quancheng.examples.model.hello.Project> projects =
        new HashMap<String, com.quancheng.examples.model.hello.Project>();
    projects.put("test", project);
    request.setProjects(projects);
    helloService.sayHelloStream(request, observer());
    return null;
  }


  private StreamObserver<HelloReply> observer() {
    StreamObserver<HelloReply> responseObserver = new StreamObserver<HelloReply>() {
      @Override
      public void onNext(HelloReply summary) {
        System.out.println(summary);
      }

      @Override
      public void onError(Throwable t) {
        t.printStackTrace();
      }

      @Override
      public void onCompleted() {

      }
    };
    return responseObserver;
  }



  private HelloReply call(final String name) {
    HelloRequest request = new HelloRequest();
    request.setName(name);
    com.quancheng.examples.model.hello.Project project =
        new com.quancheng.examples.model.hello.Project();
    project.setId("123");
    Map<String, com.quancheng.examples.model.hello.Project> projects =
        new HashMap<String, com.quancheng.examples.model.hello.Project>();
    projects.put("test", project);
    request.setProjects(projects);
    RpcContext.getContext().set("123", "helloworld");
    HelloReply reply = helloService.sayHello(request);
    return reply;
  }


}
