package com.quancheng.saluki.example.client;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.quancheng.examples.model.hello.HelloReply;
import com.quancheng.examples.model.hello.HelloRequest;
import com.quancheng.examples.service.HelloService;
import com.quancheng.saluki.boot.SalukiReference;
import com.quancheng.saluki.core.common.RpcContext;
import com.quancheng.saluki.core.grpc.client.validate.RequestArgValidatorGroupHolden;
import com.saluki.example.model.First;
import com.saluki.example.model.Second;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@RestController
@RequestMapping("/proxy")
public class ProxyServiceController {


  @SalukiReference(retries = 3, validatorGroups={First.class, Second.class})
  private HelloService helloService;


  @RequestMapping("/hello")
  public HelloReply hello(@RequestParam(value="name", required=false) String name) {
    return call(name);
  }

  @RequestMapping("/hello_1")
  public HelloReply hello1(@RequestParam(value="name", required=false) String name) {
    RequestArgValidatorGroupHolden.setHoldenGroups(new HashSet<>(Arrays.asList(First.class)));
    return call(name);
  }

  @RequestMapping("/hello_2")
  public HelloReply hello2(@RequestParam(value="name", required=false) String name) {
    RequestArgValidatorGroupHolden.setHoldenGroups(new HashSet<>(Arrays.asList(Second.class)));
    return call(name);
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
