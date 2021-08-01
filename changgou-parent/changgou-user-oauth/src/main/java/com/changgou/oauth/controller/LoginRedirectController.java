package com.changgou.oauth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/oauth")
public class LoginRedirectController {

    @RequestMapping("/login")
    public String login(String From, Model model){
        model.addAttribute("from", From);
        return "/login";
    }
}
