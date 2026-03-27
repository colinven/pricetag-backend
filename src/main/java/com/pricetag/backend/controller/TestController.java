package com.pricetag.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/company/test")
public class TestController {

    @GetMapping
    public ResponseEntity<String> test(){
        String body = "Hello from a protected endpoint!";
        return ResponseEntity.ok(body);
    }

}
