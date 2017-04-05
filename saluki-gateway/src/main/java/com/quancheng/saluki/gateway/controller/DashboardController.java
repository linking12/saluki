package com.quancheng.saluki.gateway.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class DashboardController {

    @GetMapping
    public String indexPage() {
        return "redirect:/oauth/tokens";
    }

    @GetMapping("/login.html")
    public String loginPage() {
        return "login";
    }

}
