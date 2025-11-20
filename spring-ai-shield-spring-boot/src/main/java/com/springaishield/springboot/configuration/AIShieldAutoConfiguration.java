package com.springaishield.springboot.configuration;

import com.springaishield.core.impl.BehavioralScoringEngine;
import com.springaishield.core.repository.BehaviorRepository;
import com.springaishield.core.service.RiskScoringService;
import com.springaishield.springboot.security.AIShieldFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Classe d'Auto-Configuration de Spring Boot pour le module AI Shield.
 * Active les composants, la sécurité et la persistance par défaut.
 */
@Configuration
// 1. Détecte tous les services (@Service, etc.) dans ce module
@ComponentScan(basePackages = "com.springaishield.springboot")
// 2. Active la détection des Entités JPA (UserBehaviorEntity)
@EntityScan(basePackages = "com.springaishield.springboot.persistence.entity")
// 3. Active la création des Repositories Spring Data (JpaBehaviorRepository)
@EnableJpaRepositories(basePackages = "com.springaishield.springboot.persistence.jpa")
public class AIShieldAutoConfiguration {

    /**
     * Définit l'implémentation du moteur de scoring IA.
     * On remplace le SimpleRuleEngine par le moteur Comportemental/Hybride (Tâche 10/11).
     * @param behaviorRepository Injecté par Spring grâce à @Service sur BehaviorRepositoryImpl
     * @return Le moteur de scoring Hybride
     */
    @Bean
    @ConditionalOnMissingBean // Permet à l'utilisateur de définir son propre moteur s'il le souhaite
    public RiskScoringService riskScoringService(BehaviorRepository behaviorRepository) {
        // Le moteur IA Hybride a besoin d'accéder à l'historique
        return new BehavioralScoringEngine(behaviorRepository);
    }

    /**
     * Définit le filtre de sécurité personnalisé.
     * Injecte le moteur de scoring et le repository pour la prise de décision et la persistance.
     */
    @Bean
    public AIShieldFilter aiShieldFilter(
            RiskScoringService riskScoringService,
            BehaviorRepository behaviorRepository // Nouveau : pour l'enregistrement des événements
    ) {
        return new AIShieldFilter(riskScoringService, behaviorRepository);
    }

    /**
     * Insère le filtre AI Shield dans la chaîne de sécurité de Spring.
     * Le filtre est placé avant l'authentification/autorisation.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, AIShieldFilter aiShieldFilter) throws Exception {
        http.csrf(csrf -> csrf.disable()) // CSRF désactivé pour la démo
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // Permet l'accès à toutes les URLs pour la démo
                )
                // Insère notre filtre avant les filtres d'authentification standards
                .addFilterBefore(aiShieldFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}