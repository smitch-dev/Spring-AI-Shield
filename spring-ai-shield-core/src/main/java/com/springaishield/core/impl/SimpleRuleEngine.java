package com.springaishield.core.impl;

import com.springaishield.core.model.RiskScore;
import com.springaishield.core.model.SecurityContext;
import com.springaishield.core.service.RiskScoringService;

// Implémentation initiale, utilisée comme MVP avant l'IA réelle.
public class SimpleRuleEngine implements RiskScoringService {

    @Override
    public RiskScore calculateRisk(SecurityContext context) {
        // Règle 1: Simuler un risque élevé si l'IP est 127.0.0.1 (pour le test)
        if ("127.0.0.1".equals(context.ipAddress())) {
            // Score de risque moyen fixe pour l'instant
            return new RiskScore(0.5, "Accès à partir de l'IP de développement : 127.0.0.1. Risque Moyen.");
        }

        if (context.requestUrl().contains("riskhigh")) {
            return new RiskScore(0.9, "Simulation de tentative d'injection SQL détectée.");
        }


        // Règle de base : par défaut, faible risque
        return RiskScore.low();
    }
}