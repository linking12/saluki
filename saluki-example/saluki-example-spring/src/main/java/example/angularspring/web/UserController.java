package example.angularspring.web;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.quancheng.examples.model.hello.HelloReply;
import com.quancheng.examples.model.hello.HelloRequest;
import com.quancheng.examples.service.HelloService;

import example.angularspring.dto.User;
import example.angularspring.service.UserService;

/**
 * Controller for user actions.
 */
@Controller
public class UserController {

    @Inject
    private UserService  userService;

    @Autowired
    private HelloService helloService;

    @PostConstruct
    public void init() {
        HelloRequest request = new HelloRequest();
        request.setName("liushiming");
        HelloReply reply = helloService.sayHello(request);
        System.out.println(reply);
    }

    @RequestMapping(value = "/user", method = RequestMethod.GET)
    @ResponseBody
    public User getCurrentUser() {
        return userService.getCurrentUser();
    }

}
