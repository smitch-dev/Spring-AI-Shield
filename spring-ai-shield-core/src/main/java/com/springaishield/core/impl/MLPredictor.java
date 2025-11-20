package com.springaishield.core.impl;

import com.springaishield.core.model.SecurityContext;
import com.springaishield.core.model.UserBehavior;

import java.util.List;

/**
 * SIMULATION : Représente un modèle de Machine Learning entraîné pour la classification de risque.
 * Simule la prédiction basée sur la nouveauté de l'IP.
 */
public class MLPredictor {

    /**
     * Calcule le risque basé sur le contexte actuel et l'historique.
     * @return Un score entre 0.0 (sûr) et 0.5 (risqué) pour le facteur ML.
     */
    public double predictRisk(SecurityContext context, List<UserBehavior> recentHistory) {

        // --- FEATURE ENGINEERING (Création de caractéristiques pour le modèle) ---

        boolean isKnownIp = recentHistory.stream()
                .anyMatch(b -> b.ipAddress().equals(context.ipAddress()));

        long recentAccessCount = recentHistory.stream()
                .filter(b -> b.ipAddress().equals(context.ipAddress()))
                .count();

        // --- SIMULATION DU MODÈLE LOGISTIQUE ---

        double baseScore = 0.1;

        if (recentHistory.isEmpty()) {
            return 0.3; // Risque initial si aucune donnée historique
        }

        if (!isKnownIp) {
            // L'IP est nouvelle (facteur de risque important)
            baseScore += 0.4;
        }

        if (recentAccessCount > 20) {
            // Très habituel, réduit légèrement le score
            baseScore -= 0.15;
        }

        // Le facteur ML seul ne peut pas dépasser 0.5
        return Math.max(0.0, Math.min(0.5, baseScore));
    }
}