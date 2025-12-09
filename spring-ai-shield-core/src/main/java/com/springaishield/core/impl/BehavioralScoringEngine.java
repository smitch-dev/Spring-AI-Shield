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

        // --- 1. Machine Learning Analysis / Behavioral Prediction ---
        analyzeMachineLearning(context, factors);

        // --- 2. Content Analysis (Simple Heuristics) ---
        analyzeContent(context, factors);

        // --- 3. Calculation of the Final Score (Hybrid) ---
        double totalScore = factors.stream().mapToDouble(RiskFactor::weight).sum();

        if (factors.isEmpty()) {
            return RiskScore.low();
        }

        // Limit the score to the defined maximum
        if (totalScore > MAX_RISK_SCORE) {
            totalScore = MAX_RISK_SCORE;
        }

        // Determine the main reason (detail of the most significant factor)
        String primaryReason = factors.stream()
                .max((f1, f2) -> Double.compare(f1.weight(), f2.weight()))
                .map(RiskFactor::detail)
                .orElse("Facteurs divers.");

        return new RiskScore(totalScore, primaryReason, factors);
    }

    /**
     * Assigned to MLPredictor to analyze history and context.
     */
    private void analyzeMachineLearning(SecurityContext context, List<RiskFactor> factors) {
        // Retrieving the history required for Feature Engineering of the ML model
        List<UserBehavior> recentHistory = behaviorRepository.findRecentByUserId(context.userId(), 50);

        // The logic is delegated to the ML predictor.
        double mlPrediction = mlPredictor.predictRisk(context, recentHistory);

        // Interpretation of the ML model result
        if (mlPrediction > 0.5) {
            factors.add(new RiskFactor("ML_PREDICTION", 0.5, "Le modèle ML prédit un comportement anormal élevé. Score: " + String.format("%.2f", mlPrediction)));
        } else if (mlPrediction > 0.2) {
            factors.add(new RiskFactor("ML_PREDICTION", 0.2, "Le modèle ML prédit un comportement légèrement suspect. Score: " + String.format("%.2f", mlPrediction)));
        }
    }

    /**
     * Analyzes query data (URL, body) for known attack patterns (heuristics).
     */
    private void analyzeContent(SecurityContext context, List<RiskFactor> factors) {
        String requestContent = context.requestUrl().toLowerCase();

        // Heuristics for SQL Injection (SQLi)
        if (requestContent.contains("select") || requestContent.contains("union") || requestContent.contains("--")) {
            factors.add(new RiskFactor("SQL_HEURISTIC", 0.6, "Mot-clé SQL dangereux (SELECT/UNION) détecté."));
        }

        // Heuristics for Cross-Site Scripting (XSS)
        if (requestContent.contains("<script>") || requestContent.contains("onerror") || requestContent.contains("alert(")) {
            factors.add(new RiskFactor("XSS_HEURISTIC", 0.5, "Mot-clé XSS potentiel (<script>) détecté."));
        }

        // Blocking rule inherited from the MVP for testing
        if (requestContent.contains("riskhigh")) {
            factors.add(new RiskFactor("CRITICAL_URL", 0.9, "Détection de pattern critique par URL (test)."));
        }
    }
}