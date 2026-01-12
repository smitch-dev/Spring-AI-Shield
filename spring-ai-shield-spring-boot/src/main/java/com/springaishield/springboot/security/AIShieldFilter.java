package com.springaishield.springboot.security;

import com.springaishield.core.model.RiskScore;
import com.springaishield.core.model.SecurityContext;
import com.springaishield.core.service.RiskScoringService;
import com.springaishield.core.model.UserBehavior;
import com.springaishield.core.repository.BehaviorRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Filtre principal inséré dans la chaîne Spring Security.
 * Intercepte chaque requête, calcule le risque et décide de l'action.
 */
@Order(1)
public class AIShieldFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(AIShieldFilter.class);
    private static final double RISK_THRESHOLD = 0.5; // Seuil unique de blocage

    private final RiskScoringService riskScoringService;
    private final BehaviorRepository behaviorRepository;

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
            requestUrl += "?" + request.getQueryString();
        }

        String userId = request.getRemoteUser() != null ? request.getRemoteUser() : "ANONYMOUS";
        String eventType = "ACCESS_GRANTED";

        // IMPORTANT : Ordre conforme au Record SecurityContext (userId, requestUrl, ipAddress)
        SecurityContext context = new SecurityContext(userId, requestUrl, ipAddress);

        // 2. Calcul du Score de Risque (Appel au module Core)
        RiskScore risk = riskScoringService.calculateRisk(context);

        log.info("AIShield Analysis: URL={} | User={} | Risk Score={} ({})",
                requestUrl, userId, risk.score(), risk.reason());

        // 3. Prise de décision (Logique de blocage)
        boolean isBlocked = risk.score() >= RISK_THRESHOLD;

        if (isBlocked) {
            eventType = "ACCESS_DENIED";
            log.warn("RISK DETECTED! Blocking request from {} for URL {}", ipAddress, requestUrl);

            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write("Accès bloqué par Spring AI Shield : Risque de sécurité détecté.");
        }

        // 4. Sauvegarde dans la base de données (Historique)
        // Vérifie que l'ordre des paramètres correspond à ton constructeur UserBehavior
        UserBehavior behavior = new UserBehavior(userId, ipAddress, eventType, requestUrl, risk);
        behaviorRepository.save(behavior);

        // 5. Continuation de la chaîne (UNIQUEMENT si non bloqué)
        if (!isBlocked) {
            filterChain.doFilter(request, response);
        }
        // si isBlocked est vrai,  rien ne se passe,
        // la réponse 403 a déjà été envoyée.
    }
}