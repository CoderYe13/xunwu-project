package com.henu.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class HomeController {
//    @GetMapping("/")
//    public String index(){
//        return "index";
//    }


    @GetMapping("/404")
    public String notFoundPage(){
        return "404";
    }

    @GetMapping("/logout")
    public String logoutPage(){
        return "logout";
    }
}
