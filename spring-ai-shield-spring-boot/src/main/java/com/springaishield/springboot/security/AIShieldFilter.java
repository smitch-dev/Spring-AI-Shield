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
 * Main filter inserted into the Spring Security chain.
 * * It intercepts each request, calculates the risk, and decides on the action.
 */

@Order(1)
public class AIShieldFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(AIShieldFilter.class);

    private final RiskScoringService riskScoringService;
    private final BehaviorRepository behaviorRepository;

    // The filter of the scoring service injected by Spring
    public AIShieldFilter(RiskScoringService riskScoringService, BehaviorRepository behaviorRepository) {
        this.riskScoringService = riskScoringService;
        this.behaviorRepository = behaviorRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Context Collection
        String ipAddress = request.getRemoteAddr();
        String requestUrl = request.getRequestURI();
        if (request.getQueryString() != null) {
            requestUrl += "?" + request.getQueryString(); // Inclut les paramètres
        }

        String userId = request.getRemoteUser() != null ? request.getRemoteUser() : "ANONYMOUS";

        String eventType = "ACCESS_GRANTED";

        SecurityContext context = new SecurityContext(userId, ipAddress, requestUrl);

        // 2. Risk Score Calculation (Call to our Core module)
        RiskScore risk = riskScoringService.calculateRisk(context);

        log.info("AIShield Analysis: URL={} | User={} | Risk Score={} ({})",
                requestUrl, userId, risk.score(), risk.reason());

        // 3. Decision-Making (Blocking Logic)
        if (risk.score() > 0.8) {
            // Si bloqué, l'événement est un DENIAL
            eventType = "ACCESS_DENIED";
            log.warn("RISK HIGH! Blocking request from {} for URL {}", ipAddress, requestUrl);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN); // Code HTTP 403
            response.getWriter().write("Accès bloqué par Spring AI Shield : Risque élevé.");
        }

        // 4. BACK UP IN DATABASE
        UserBehavior behavior = new UserBehavior(userId, ipAddress, eventType, requestUrl, risk);
        behaviorRepository.save(behavior);

        // 5. Chain continuation (only if not blocked)
        if (risk.score() <= 0.8) {
            filterChain.doFilter(request, response);
        }
    }
}