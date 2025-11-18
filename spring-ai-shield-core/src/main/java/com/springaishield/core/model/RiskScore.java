package com.springaishield.core.model;

// Exemple de classe dans spring-ai-shield-core
public record RiskScore(double score, String reason) {
    // Le score doit être entre 0.0 (Faible) et 1.0 (Élevé)
    public static RiskScore low() {
        return new RiskScore(0.1, "Contexte habituel.");
    }
}