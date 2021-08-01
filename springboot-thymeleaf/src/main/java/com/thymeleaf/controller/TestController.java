package com.thymeleaf.controller;

import com.thymeleaf.domain.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;

/**
 * Thymeleaf模板引擎
 */
@Controller
@RequestMapping(value = "/test")
public class TestController {

    @GetMapping(value = "/hello")
    public String hello(Model model){
        model.addAttribute("message", "hello thymeleaf!");

        //创建一个List<User>，并将其存入Model中，到页面使用thymeleaf标签显示
        List<User> users = new ArrayList<User>();
        users.add(new User(1, "张三", "广州"));
        users.add(new User(2, "李四", "深圳"));
        users.add(new User(3, "王五", "上海"));
        model.addAttribute("users", users);

        Map<String, Object> dataMap = new HashMap<String, Object>();
        dataMap.put("NO", "123");
        dataMap.put("address", "深圳");
        model.addAttribute("dataMap", dataMap);

        model.addAttribute("now", new Date());
        model.addAttribute("age", 10);
        return "demo1";
    }
}
