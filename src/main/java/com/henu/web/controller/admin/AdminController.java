package com.henu.web.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {
    @GetMapping("/admin/center")
    public String adminCenterPage(){
        return "admin/center";
    }
    @GetMapping("/admin/welcome")
    public String adminWelComePage(){
        return "admin/welcome";
    }

    @GetMapping("/admin/login")
    public String adminLoginPage(){
        return "admin/login";
    }
}
