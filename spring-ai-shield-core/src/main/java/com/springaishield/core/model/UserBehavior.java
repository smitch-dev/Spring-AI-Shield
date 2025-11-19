package com.springaishield.core.model;

import java.time.Instant;

/**
 * Représente un événement comportemental ou d'accès d'un utilisateur.
 * Utilisé pour entraîner le modèle et calculer le risque contextuel.
 */
public record UserBehavior(
        String id, // Identifiant de l'enregistrement
        String userId, // Identifiant de l'utilisateur (anonyme ou authentifié)
        String ipAddress, // Adresse IP utilisée
        String eventType, // Type d'événement (LOGIN_SUCCESS, ACCESS_DENIED, etc.)
        String requestUrl, // Endpoint accédé
        RiskScore riskScore, // Score de risque associé à cet événement
        Instant timestamp // Date et heure de l'événement
) {
    // Constructeur secondaire pour les événements sans ID immédiat
    public UserBehavior(String userId, String ipAddress, String eventType, String requestUrl, RiskScore riskScore) {
        this(null, userId, ipAddress, eventType, requestUrl, riskScore, Instant.now());
    }
}