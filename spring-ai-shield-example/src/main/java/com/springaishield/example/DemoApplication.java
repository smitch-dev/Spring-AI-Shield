package com.springaishield.example;

import com.springaishield.springboot.annotation.EnableAIShield;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// L'Action Clé ! L'application est protégée par notre seule annotation.
@SpringBootApplication
@EnableAIShield
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}