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
 * Spring Boot Auto-Configuration Class for the AI ​​Shield module.
 */
@Configuration

@EntityScan(basePackages = "com.springaishield.springboot.persistence.entity")
@EnableJpaRepositories(basePackages = "com.springaishield.springboot.persistence.jpa")
public class AIShieldAutoConfiguration {

    // On déclare explicitement le RepositoryImpl si on a supprimé le ComponentScan
    @Bean
    @ConditionalOnMissingBean
    public BehaviorRepository behaviorRepository(com.springaishield.springboot.persistence.jpa.JpaBehaviorRepository jpaRepo) {
        return new com.springaishield.springboot.service.BehaviorRepositoryImpl(jpaRepo);
    }

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
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }
}