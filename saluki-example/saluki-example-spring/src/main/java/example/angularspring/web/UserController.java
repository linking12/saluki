package example.angularspring.web;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.quancheng.examples.model.hello.HelloReply;
import com.quancheng.examples.model.hello.HelloRequest;
import com.quancheng.examples.service.HelloService;
import com.quancheng.saluki.core.common.RpcContext;

import example.angularspring.dto.User;
import example.angularspring.service.UserService;

/**
 * Controller for user actions.
 */
@Controller
public class UserController {

    @Inject
    private UserService  userService;

    @Inject
    private HelloService helloService;

    @RequestMapping(value = "/user", method = RequestMethod.GET)
    @ResponseBody
    public User getCurrentUser() {
        HelloRequest request = new HelloRequest();
        request.setName("liushiming");
        RpcContext.getContext().set("123", "helloworld");
        HelloReply reply = helloService.sayHello(request);
        System.out.println(reply);
        return userService.getCurrentUser();
    }

}
