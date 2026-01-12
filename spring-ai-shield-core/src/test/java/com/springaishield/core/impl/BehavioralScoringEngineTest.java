package com.springaishield.core.impl;

import com.springaishield.core.model.*;
import com.springaishield.core.repository.BehaviorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

class BehavioralScoringEngineTest {

    private BehavioralScoringEngine engine;
    private BehaviorRepository behaviorRepository;

    @BeforeEach
    void setUp() {
        behaviorRepository = Mockito.mock(BehaviorRepository.class);
        engine = new BehavioralScoringEngine(behaviorRepository);

        // Simulation d'un historique vide par défaut
        when(behaviorRepository.findRecentByUserId(anyString(), anyInt()))
                .thenReturn(Collections.emptyList());
    }

    @Test
    @DisplayName("Détection SQLi - Simple")
    void testSqlInjectionDetection() {
        SecurityContext context = new SecurityContext("user1", "/api?query=select", "127.0.0.1");
        RiskScore result = engine.calculateRisk(context);
        assertEquals(0.6, result.score(), "Le moteur devrait retourner 0.6 pour un pattern SQL.");
    }

    @Test
    @DisplayName("Détection SQLi - Insensibilité à la casse")
    void testSqlInjectionCaseInsensitive() {
        // Test avec "SELECT" en majuscules
        SecurityContext context = new SecurityContext("user1", "/api?query=SELECT", "127.0.0.1");
        RiskScore result = engine.calculateRisk(context);
        assertEquals(0.6, result.score(), "Le moteur devrait détecter SQL même en majuscules.");
    }

    @Test
    @DisplayName("Détection XSS - Caractères bruts")
    void testXssDetection() {
        SecurityContext context = new SecurityContext("user1", "/test?<script>", "127.0.0.1");
        RiskScore result = engine.calculateRisk(context);
        assertEquals(0.5, result.score(), "Le moteur devrait retourner 0.5 pour un pattern XSS brut.");
    }

    @Test
    @DisplayName("Détection XSS - URL Encodée (%3Cscript%3E)")
    void testEncodedXssDetection() {
        // Simulation de ce que le navigateur envoie réellement
        SecurityContext context = new SecurityContext("user1", "/search?q=%3Cscript%3Ealert(1)%3C/script%3E", "127.0.0.1");

        RiskScore result = engine.calculateRisk(context);

        assertTrue(result.score() >= 0.5, "Le moteur doit décoder l'URL et détecter le XSS (score >= 0.5)");
        assertEquals("Pattern XSS potentiel détecté.", result.reason());
    }

    @Test
    @DisplayName("Vérification du Record UserBehavior")
    void testUserBehaviorRecord() {
        UserBehavior behavior = new UserBehavior(
                "ID-1", "user123", "127.0.0.1", "LOGIN", "/home",
                new RiskScore(0.1, "Low"), Instant.now()
        );
        assertEquals("user123", behavior.userId());
    }

    @Test
    @DisplayName("Sécurité - Requête saine")
    void testCleanRequest() {
        SecurityContext context = new SecurityContext("user1", "/home?page=1", "127.0.0.1");
        RiskScore result = engine.calculateRisk(context);
        assertTrue(result.score() < 0.2, "Une requête normale ne doit pas être bloquée.");
    }
}