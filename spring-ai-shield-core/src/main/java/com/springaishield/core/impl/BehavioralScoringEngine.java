package com.springaishield.core.impl;

import com.springaishield.core.model.RiskFactor;
import com.springaishield.core.model.RiskScore;
import com.springaishield.core.model.SecurityContext;
import com.springaishield.core.model.UserBehavior;
import com.springaishield.core.repository.BehaviorRepository;
import com.springaishield.core.service.RiskScoringService;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Moteur de scoring de risque Hybride.
 */
public class BehavioralScoringEngine implements RiskScoringService {

    private final BehaviorRepository behaviorRepository;
    private final MLPredictor mlPredictor;
    private static final double MAX_RISK_SCORE = 1.0;

    public BehavioralScoringEngine(BehaviorRepository behaviorRepository) {
        this.behaviorRepository = behaviorRepository;
        this.mlPredictor = new MLPredictor();
    }

    @Override
    public RiskScore calculateRisk(SecurityContext context) {
        List<RiskFactor> factors = new ArrayList<>();

        // 1. Analyse comportementale (ML)
        analyzeMachineLearning(context, factors);

        // 2. Analyse de contenu (Heuristiques)
        analyzeContent(context, factors);

        // 3. Calcul du score final
        double totalScore = factors.stream().mapToDouble(RiskFactor::weight).sum();

        if (factors.isEmpty()) {
            return RiskScore.low();
        }

        if (totalScore > MAX_RISK_SCORE) {
            totalScore = MAX_RISK_SCORE;
        }

        String primaryReason = factors.stream()
                .max((f1, f2) -> Double.compare(f1.weight(), f2.weight()))
                .map(RiskFactor::detail)
                .orElse("Facteurs divers.");

        return new RiskScore(totalScore, primaryReason, factors);
    }

    private void analyzeMachineLearning(SecurityContext context, List<RiskFactor> factors) {
        List<UserBehavior> recentHistory = behaviorRepository.findRecentByUserId(context.userId(), 50);
        double mlPrediction = mlPredictor.predictRisk(context, recentHistory);

        // On n'ajoute un facteur que si la prédiction dépasse un seuil significatif
        if (mlPrediction > 0.5) {
            factors.add(new RiskFactor("ML_PREDICTION", 0.5, "Comportement anormal élevé détecté par ML."));
        } else if (mlPrediction > 0.3) { // Seuil augmenté pour éviter les faux positifs à 0.2
            factors.add(new RiskFactor("ML_PREDICTION", 0.2, "Comportement légèrement suspect détecté par ML."));
        }
    }

    private void analyzeContent(SecurityContext context, List<RiskFactor> factors) {
        if (context.requestUrl() == null) return;

        // Conversion en minuscules pour l'insensibilité à la casse
        String requestContent = context.requestUrl().toLowerCase(Locale.ROOT);

        // Heuristiques SQL
        if (requestContent.contains("select") || requestContent.contains("union") || requestContent.contains("--")) {
            factors.add(new RiskFactor("SQL_HEURISTIC", 0.6, "Mot-clé SQL dangereux détecté."));
        }

        // Heuristiques XSS
        if (requestContent.contains("<script>") || requestContent.contains("onerror") || requestContent.contains("alert(")) {
            factors.add(new RiskFactor("XSS_HEURISTIC", 0.5, "Pattern XSS potentiel détecté."));
        }

        // Test de pattern critique
        if (requestContent.contains("riskhigh")) {
            factors.add(new RiskFactor("CRITICAL_URL", 0.9, "Pattern critique détecté (test)."));
        }
    }
}