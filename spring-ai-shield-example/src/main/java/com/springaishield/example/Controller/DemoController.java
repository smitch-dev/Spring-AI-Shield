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
    // DÉFINISSEZ EXPLICITEMENT LE NOM DU PARAMÈTRE : name="q"
    public String search(@RequestParam(name = "q") String query) {
        // Renommer la variable interne en 'query' est plus clair.
        // Spring utilise 'name="q"' pour trouver le paramètre dans l'URL.
        return "Search results for: " + query;
    }
}