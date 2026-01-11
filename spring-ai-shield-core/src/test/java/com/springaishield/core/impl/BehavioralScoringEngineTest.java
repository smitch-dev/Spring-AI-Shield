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
        // Mockito est maintenant disponible via le pom.xml core corrigé
        behaviorRepository = Mockito.mock(BehaviorRepository.class);
        engine = new BehavioralScoringEngine(behaviorRepository);

        // Simulation d'un historique vide pour les tests heuristiques
        when(behaviorRepository.findRecentByUserId(anyString(), anyInt()))
                .thenReturn(Collections.emptyList());
    }

    @Test
    @DisplayName("Détection SQLi")
    void testSqlInjectionDetection() {
        // Assure-toi que "select" est bien passé à l'URL
        // Structure attendue : SecurityContext(userId, requestUrl, ipAddress)
        SecurityContext context = new SecurityContext("user1", "http://localhost/api?query=select", "127.0.0.1");

        RiskScore result = engine.calculateRisk(context);

        // Si ça affiche toujours 0.1, c'est que l'URL reçue par le moteur est nulle ou différente
        assertEquals(0.6, result.score(), "Le moteur devrait retourner 0.6 pour un pattern SQL.");
    }

    @Test
    @DisplayName("Détection XSS")
    void testXssDetection() {
        SecurityContext context = new SecurityContext("user1", "/test?<script>", "127.0.0.1");

        RiskScore result = engine.calculateRisk(context);

        assertEquals(0.5, result.score(), "Le moteur devrait retourner 0.5 pour un pattern XSS.");
    }
    @Test
    @DisplayName("Vérification du Record UserBehavior")
    void testUserBehaviorRecord() {
        // Utilisation du constructeur à 7 arguments défini dans votre code
        UserBehavior behavior = new UserBehavior(
                "ID-1", "user123", "127.0.0.1", "LOGIN", "/home",
                new RiskScore(0.1, "Low"), Instant.now()
        );
        assertEquals("user123", behavior.userId()); // Accès via méthode record, pas de getUserId()
    }
}