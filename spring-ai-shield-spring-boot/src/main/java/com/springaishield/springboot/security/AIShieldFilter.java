package com.springaishield.springboot.security;

import com.springaishield.core.model.RiskScore;
import com.springaishield.core.model.SecurityContext;
import com.springaishield.core.service.RiskScoringService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.springaishield.core.model.UserBehavior; // NOUVEL IMPORT
import com.springaishield.core.repository.BehaviorRepository; // NOUVEL IMPORT

/**
 * Filtre principal inséré dans la chaîne Spring Security.
 * Il intercepte chaque requête, calcule le risque et décide de l'action.
 */

@Order(1)
public class AIShieldFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(AIShieldFilter.class);

    private final RiskScoringService riskScoringService;
    private final BehaviorRepository behaviorRepository;

    // Le filtre a besoin du service de scoring (injecté par Spring)
    public AIShieldFilter(RiskScoringService riskScoringService, BehaviorRepository behaviorRepository) {
        this.riskScoringService = riskScoringService;
        this.behaviorRepository = behaviorRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Collecte du Contexte
        String ipAddress = request.getRemoteAddr();
        String requestUrl = request.getRequestURI();
        if (request.getQueryString() != null) {
            requestUrl += "?" + request.getQueryString(); // Inclut les paramètres
        }
        // userId est simple pour l'instant; il sera plus complexe une fois l'utilisateur authentifié
        String userId = request.getRemoteUser() != null ? request.getRemoteUser() : "ANONYMOUS";

        // Définir le type d'événement initial : par défaut, un accès simple.
        String eventType = "ACCESS_GRANTED";

        SecurityContext context = new SecurityContext(userId, ipAddress, requestUrl);

        // 2. Calcul du Score de Risque (Appel à notre module Core)
        RiskScore risk = riskScoringService.calculateRisk(context);

        log.info("AIShield Analysis: URL={} | User={} | Risk Score={} ({})",
                requestUrl, userId, risk.score(), risk.reason());

        // 3. Prise de Décision (Logique de blocage)
        if (risk.score() > 0.8) {
            // Si bloqué, l'événement est un DENIAL
            eventType = "ACCESS_DENIED";
            log.warn("RISK HIGH! Blocking request from {} for URL {}", ipAddress, requestUrl);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN); // Code HTTP 403
            response.getWriter().write("Accès bloqué par Spring AI Shield : Risque élevé.");
        }

        // 4. SAUVEGARDE DANS LA BASE DE DONNÉES
        UserBehavior behavior = new UserBehavior(userId, ipAddress, eventType, requestUrl, risk);
        behaviorRepository.save(behavior);

        // 5. Continuation de la chaîne (seulement si non bloqué)
        if (risk.score() <= 0.8) {
            filterChain.doFilter(request, response);
        }
    }
}