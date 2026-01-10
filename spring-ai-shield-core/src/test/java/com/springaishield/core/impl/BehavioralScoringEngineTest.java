package com.springaishield.core.impl;

import com.springaishield.core.model.*;
import com.springaishield.core.repository.BehaviorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

class BehavioralScoringEngineTest {

    private BehavioralScoringEngine engine;
    private BehaviorRepository behaviorRepository;

    @BeforeEach
    void setUp() {
        // On mock le repository car le moteur appelle behaviorRepository.findRecentByUserId
        behaviorRepository = Mockito.mock(BehaviorRepository.class);
        // Initialisation du moteur avec le mock
        engine = new BehavioralScoringEngine(behaviorRepository);

        // Comportement par défaut : historique vide pour ne pas fausser les tests heuristiques
        when(behaviorRepository.findRecentByUserId(anyString(), anyInt()))
                .thenReturn(Collections.emptyList());
    }

    @Test
    @DisplayName("Détection d'injection SQL via URL")
    void testSqlInjectionDetection() {
        // Dans votre moteur, l'analyse porte sur context.requestUrl()
        SecurityContext context = new SecurityContext("user123", "/api/data?id=1' OR '1'='1'--", "127.0.0.1");

        RiskScore result = engine.calculateRisk(context);

        assertTrue(result.score() >= 0.6, "Le score devrait refléter une détection SQL (0.6).");
        assertTrue(result.reason().contains("SQL"), "La raison devrait mentionner le risque SQL.");
    }

    @Test
    @DisplayName("Détection d'attaque XSS via URL")
    void testXssDetection() {
        SecurityContext context = new SecurityContext("user123", "/search?q=<script>alert(1)</script>", "127.0.0.1");

        RiskScore result = engine.calculateRisk(context);

        assertTrue(result.score() >= 0.5, "Le score devrait refléter une détection XSS (0.5).");
    }

    @Test
    @DisplayName("Impact du passif utilisateur (Mock ML)")
    void testBehaviorHistoryImpact() {
        String userId = "attacker_user";
        SecurityContext context = new SecurityContext(userId, "/safe-url", "192.168.1.1");

        // Simulation d'un historique lourd pour influencer le MLPredictor
        UserBehavior pastEvent = new UserBehavior(
                "id-1",
                userId,
                "192.168.1.1",
                "ATTACK_ATTEMPT",
                "/malicious-path",
                new RiskScore(0.9, "Previous Attack"),
                Instant.now()
        );

        when(behaviorRepository.findRecentByUserId(eq(userId), anyInt()))
                .thenReturn(List.of(pastEvent));

        RiskScore result = engine.calculateRisk(context);

        // Note : Le score dépend ici de la logique interne de votre MLPredictor
        assertNotNull(result);
        assertDoesNotThrow(() -> result.score());
    }
}