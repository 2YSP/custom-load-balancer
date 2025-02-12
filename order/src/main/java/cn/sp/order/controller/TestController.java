package cn.sp.order.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: Ship
 * @Description:
 * @Date: Created in 2025/2/12
 */
@RequestMapping("test")
@RestController
public class TestController {


    @GetMapping("")
    public String sayHello(){
        return "hello";
    }
}
