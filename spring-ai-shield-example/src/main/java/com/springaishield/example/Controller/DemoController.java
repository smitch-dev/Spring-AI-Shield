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

    // C'est cette méthode que nous allons utiliser pour tester le bouclier
    @GetMapping("/search")
    public String search(@RequestParam(name = "q", defaultValue = "") String query) {
        return "Résultats de recherche pour : " + query;
    }
}