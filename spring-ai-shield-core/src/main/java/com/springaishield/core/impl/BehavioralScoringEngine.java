package com.springaishield.core.impl;

import com.springaishield.core.model.RiskFactor;
import com.springaishield.core.model.RiskScore;
import com.springaishield.core.model.SecurityContext;
import com.springaishield.core.model.UserBehavior;
import com.springaishield.core.repository.BehaviorRepository;
import com.springaishield.core.service.RiskScoringService;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Moteur de scoring de risque Hybride mis à jour pour décodage URL.
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

        if (mlPrediction > 0.5) {
            factors.add(new RiskFactor("ML_PREDICTION", 0.5, "Comportement anormal élevé détecté par ML."));
        } else if (mlPrediction > 0.3) {
            factors.add(new RiskFactor("ML_PREDICTION", 0.2, "Comportement légèrement suspect détecté par ML."));
        }
    }

    private void analyzeContent(SecurityContext context, List<RiskFactor> factors) {
        if (context.requestUrl() == null) return;

        try {
            // 1. Décodage de l'URL (pour transformer %3C en <, %3E en >, etc.)
            // On le fait avant le toLowerCase pour éviter des problèmes de caractères spéciaux
            String decodedContent = URLDecoder.decode(context.requestUrl(), StandardCharsets.UTF_8)
                    .toLowerCase(Locale.ROOT);

            // 2. Heuristiques SQL sur le contenu décodé
            if (decodedContent.contains("select") || decodedContent.contains("union") || decodedContent.contains("--")) {
                factors.add(new RiskFactor("SQL_HEURISTIC", 0.6, "Mot-clé SQL dangereux détecté."));
            }

            // 3. Heuristiques XSS sur le contenu décodé (Maintenant ça va détecter <script>)
            if (decodedContent.contains("<script>") || decodedContent.contains("onerror") || decodedContent.contains("alert(")) {
                factors.add(new RiskFactor("XSS_HEURISTIC", 0.5, "Pattern XSS potentiel détecté."));
            }

            // 4. Test de pattern critique
            if (decodedContent.contains("riskhigh")) {
                factors.add(new RiskFactor("CRITICAL_URL", 0.9, "Pattern critique détecté (test)."));
            }

        } catch (Exception e) {
            // En cas d'erreur de décodage, on analyse la chaîne brute par sécurité
            String rawContent = context.requestUrl().toLowerCase(Locale.ROOT);
            if (rawContent.contains("select") || rawContent.contains("<script>")) {
                factors.add(new RiskFactor("DECODE_ERROR_SUSPICION", 0.5, "Requête suspecte mal formée."));
            }
        }
    }
}