package com.springaishield.springboot.configuration;

// ... imports existants

import com.springaishield.core.impl.SimpleRuleEngine;
import com.springaishield.core.service.RiskScoringService;
import com.springaishield.springboot.security.AIShieldFilter; // NOUVEL IMPORT
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.SecurityFilterChain; // NOUVEL IMPORT

// Les imports Spring Security à ajouter/corriger :
import org.springframework.security.config.annotation.web.builders.HttpSecurity; // Pour la classe HttpSecurity
import org.springframework.security.web.SecurityFilterChain; // Pour l'objet retourné
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter; // Pour placer le filtre avant
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer; // Pour disable CSRF

@Configuration
public class AIShieldAutoConfiguration {

    // ... (riskScoringService Bean existant) ...
    @Bean
    @ConditionalOnMissingBean
    public RiskScoringService riskScoringService() {
        return new SimpleRuleEngine();
    }
    // ...

    // NOUVEAU BEAN : Enregistrement du filtre de sécurité
    // Nous le rendons disponible pour injection.
    @Bean
    public AIShieldFilter aiShieldFilter(RiskScoringService riskScoringService) {
        return new AIShieldFilter(riskScoringService);
    }

    // NOUVEAU BEAN : Configuration de Spring Security
    // Insère notre filtre avant les filtres d'authentification de base.
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, AIShieldFilter aiShieldFilter) throws Exception {

        // 1. Ajouter notre filtre AI Shield en premier (ou juste avant l'authentification standard)
        http.addFilterBefore(aiShieldFilter, UsernamePasswordAuthenticationFilter.class);

        // 2. Simplifier la configuration de sécurité pour l'exemple
        // Désactiver la protection CSRF pour simplifier les tests (bonne pratique pour une API)
        http.csrf(AbstractHttpConfigurer::disable);

        // 3. Permettre l'accès à tous les requêtes (pour l'application de démo)
        http.authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll() // Permet l'accès public à tout
        );
        return http.build();
    }
}