package com.springaishield.core.impl;

import com.springaishield.core.model.RiskFactor;
import com.springaishield.core.model.RiskScore;
import com.springaishield.core.model.SecurityContext;
import com.springaishield.core.model.UserBehavior;
import com.springaishield.core.repository.BehaviorRepository;
import com.springaishield.core.service.RiskScoringService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Moteur de scoring de risque Hybride utilisant une combinaison de :
 * 1. Prédiction Machine Learning (comportemental)
 * 2. Analyse de Contenu (heuristiques)
 */
public class BehavioralScoringEngine implements RiskScoringService {

    private final BehaviorRepository behaviorRepository;
    private final MLPredictor mlPredictor;
    private static final double MAX_RISK_SCORE = 1.0;

    public BehavioralScoringEngine(BehaviorRepository behaviorRepository) {
        this.behaviorRepository = behaviorRepository;
        // Initialisation du modèle ML simulé
        this.mlPredictor = new MLPredictor();
    }

    @Override
    public RiskScore calculateRisk(SecurityContext context) {

        List<RiskFactor> factors = new ArrayList<>();

        // --- 1. Analyse ML / Prédiction Comportementale ---
        analyzeMachineLearning(context, factors);

        // --- 2. Analyse de Contenu (Heuristiques Simples) ---
        analyzeContent(context, factors);

        // --- 3. Calcul du Score Final (Hybride) ---

        double totalScore = factors.stream().mapToDouble(RiskFactor::weight).sum();

        if (factors.isEmpty()) {
            return RiskScore.low();
        }

        // Limiter le score au maximum défini
        if (totalScore > MAX_RISK_SCORE) {
            totalScore = MAX_RISK_SCORE;
        }

        // Déterminer la raison principale (détail du facteur le plus lourd)
        String primaryReason = factors.stream()
                .max((f1, f2) -> Double.compare(f1.weight(), f2.weight()))
                .map(RiskFactor::detail)
                .orElse("Facteurs divers.");

        return new RiskScore(totalScore, primaryReason, factors);
    }

    /**
     * Déléguée au MLPredictor pour analyser l'historique et le contexte.
     */
    private void analyzeMachineLearning(SecurityContext context, List<RiskFactor> factors) {
        // Récupérer l'historique nécessaire pour le Feature Engineering du modèle ML
        List<UserBehavior> recentHistory = behaviorRepository.findRecentByUserId(context.userId(), 50);

        // La logique est déléguée au prédicteur ML
        double mlPrediction = mlPredictor.predictRisk(context, recentHistory);

        // Interprétation du résultat du modèle ML
        if (mlPrediction > 0.5) {
            factors.add(new RiskFactor("ML_PREDICTION", 0.5, "Le modèle ML prédit un comportement anormal élevé. Score: " + String.format("%.2f", mlPrediction)));
        } else if (mlPrediction > 0.2) {
            factors.add(new RiskFactor("ML_PREDICTION", 0.2, "Le modèle ML prédit un comportement légèrement suspect. Score: " + String.format("%.2f", mlPrediction)));
        }
    }

    /**
     * Analyse les données de la requête (URL, corps) pour des motifs d'attaque connus (heuristiques).
     */
    private void analyzeContent(SecurityContext context, List<RiskFactor> factors) {
        String requestContent = context.requestUrl().toLowerCase();

        // Heuristique pour Injection SQL (SQLi)
        if (requestContent.contains("select") || requestContent.contains("union") || requestContent.contains("--")) {
            factors.add(new RiskFactor("SQL_HEURISTIC", 0.6, "Mot-clé SQL dangereux (SELECT/UNION) détecté."));
        }

        // Heuristique pour Cross-Site Scripting (XSS)
        if (requestContent.contains("<script>") || requestContent.contains("onerror") || requestContent.contains("alert(")) {
            factors.add(new RiskFactor("XSS_HEURISTIC", 0.5, "Mot-clé XSS potentiel (<script>) détecté."));
        }

        // Règle de blocage héritée du MVP pour le test
        if (requestContent.contains("riskhigh")) {
            factors.add(new RiskFactor("CRITICAL_URL", 0.9, "Détection de pattern critique par URL (test)."));
        }
    }
}