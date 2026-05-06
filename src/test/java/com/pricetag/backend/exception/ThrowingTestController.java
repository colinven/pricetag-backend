package com.pricetag.backend.exception;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ThrowingTestController {

    @GetMapping("/test/runtime-exception")
    String throwRuntime() {
        throw new RuntimeException("test runtime exception");
    }

    @GetMapping("/test/null-pointer")
    String throwNpe() {
        throw new NullPointerException("test null pointer exception");
    }
}
