package com.springaishield.core.model;

import java.util.Collections;
import java.util.List;

/**
 * Représente le score de risque calculé.
 * Ajout d'une liste de facteurs pour la transparence.
 */
public record RiskScore(
        double score,
        String reason,
        List<RiskFactor> contributingFactors // NOUVEAU : Liste des facteurs d'influence
) {
    // Constructeur d'origine mis à jour pour compatibilité
    public RiskScore(double score, String reason) {
        this(score, reason, Collections.emptyList());
    }

    // Simplification pour un score faible
    public static RiskScore low() {
        return new RiskScore(0.1, "Contexte habituel.", Collections.emptyList());
    }
}