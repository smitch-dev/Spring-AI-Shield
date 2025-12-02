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

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.core.Ordered;

/**
 * Classe d'Auto-Configuration de Spring Boot pour le module AI Shield.
 */
@Configuration
@ComponentScan(basePackages = "com.springaishield.springboot")
@EntityScan(basePackages = "com.springaishield.springboot.persistence.entity")
@EnableJpaRepositories(basePackages = "com.springaishield.springboot.persistence.jpa")
public class AIShieldAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RiskScoringService riskScoringService(BehaviorRepository behaviorRepository) {
        return new BehavioralScoringEngine(behaviorRepository);
    }

    @Bean
    @ConditionalOnMissingBean
    public AIShieldFilter aiShieldFilter(
            RiskScoringService riskScoringService,
            BehaviorRepository behaviorRepository
    ) {
        return new AIShieldFilter(riskScoringService, behaviorRepository);
    }

    @Bean
    public FilterRegistrationBean<AIShieldFilter> aiShieldFilterRegistration(AIShieldFilter aiShieldFilter) {
        FilterRegistrationBean<AIShieldFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(aiShieldFilter);
        registration.addUrlPatterns("/*");

        // Priorité la plus élevée pour s'assurer que le filtre est exécuté avant Spring Security
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);

        return registration;
    }
}