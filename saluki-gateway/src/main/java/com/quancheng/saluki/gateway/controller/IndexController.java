package com.quancheng.saluki.gateway.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class IndexController {

    @GetMapping
    public String indexPage() {
        return "redirect:/oauth/tokens.html";
    }

    @GetMapping("/login.html")
    public String loginPage() {
        return "login";
    }

}
