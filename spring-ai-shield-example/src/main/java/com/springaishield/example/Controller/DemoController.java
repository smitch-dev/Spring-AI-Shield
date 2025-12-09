package com.springaishield.example.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {

    @GetMapping("/hello")
    public String publicAccess() {
        return "Hello World! Accès autorisé.";
    }

    @GetMapping("/search")
    public String search(@RequestParam(name = "q") String query) {
        return "Search results for: " + query;
    }
}