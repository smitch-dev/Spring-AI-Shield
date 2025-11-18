package com.springaishield.core.service;

import com.springaishield.core.model.SecurityContext;
import com.springaishield.core.model.RiskScore;

/**
 * Service central pour calculer le score de risque en fonction du contexte de sécurité.
 * C'est l'interface qui sera implémentée par les modèles ML.
 */
public interface RiskScoringService {

    /**
     * Calcule le score de risque (0.0 à 1.0) pour une requête donnée.
     * * @param context Le contexte de sécurité de la requête (utilisateur, IP, heure, etc.).
     * @return Le score de risque calculé.
     */
    RiskScore calculateRisk(SecurityContext context);
}