package com.dtacomp.api;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
public class HelloWorld {

    @GetMapping("/hello")
    public String getMethodName() {
        return "Hello World!";
    }

}