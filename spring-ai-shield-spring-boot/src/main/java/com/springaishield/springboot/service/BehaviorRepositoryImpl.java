package com.springaishield.springboot.service;

import com.springaishield.core.model.RiskScore;
import com.springaishield.core.model.UserBehavior;
import com.springaishield.core.repository.BehaviorRepository;
import com.springaishield.springboot.persistence.entity.UserBehaviorEntity;
import com.springaishield.springboot.persistence.jpa.JpaBehaviorRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Collections;

/**
 * Implémentation concrète de l'interface du Core, utilisant Spring Data JPA.
 */
@Service
public class BehaviorRepositoryImpl implements BehaviorRepository {

    private final JpaBehaviorRepository jpaRepository;

    public BehaviorRepositoryImpl(JpaBehaviorRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public UserBehavior save(UserBehavior behavior) {
        // Préparation du résumé des facteurs de risque pour le stockage
        String factorSummary = behavior.riskScore().contributingFactors().stream()
                .map(f -> f.name() + ":" + f.weight())
                .collect(Collectors.joining("; "));

        // On utilise la raison du risque s'il n'y a pas de facteurs détaillés
        String eventDetails = factorSummary.isEmpty() ? behavior.riskScore().reason() : factorSummary;

        // 1. Conversion du modèle Core vers l'entité JPA
        // Ordre constructeur Entity: userId, ipAddress, eventType, requestUrl, riskScore
        UserBehaviorEntity entity = new UserBehaviorEntity(
                behavior.userId(),
                behavior.ipAddress(),
                eventDetails,
                behavior.requestUrl(),
                behavior.riskScore().score()
        );

        // 2. Sauvegarde en BDD
        UserBehaviorEntity savedEntity = jpaRepository.save(entity);

        // 3. Reconversion vers le modèle Core
        return mapEntityToCore(savedEntity);
    }

    @Override
    public List<UserBehavior> findRecentByUserId(String userId, int limit) {
        List<UserBehaviorEntity> entities = jpaRepository.findRecentByUserIdNative(userId, limit);
        return entities.stream()
                .map(this::mapEntityToCore)
                .collect(Collectors.toList());
    }

    private UserBehavior mapEntityToCore(UserBehaviorEntity entity) {
        // création du RiskScore (les facteurs détaillés sont perdus à la lecture simple,
        // mais le score et la raison globale sont préservés)
        RiskScore score = new RiskScore(entity.getRiskScore(), entity.getEventType(), Collections.emptyList());


        // Record est (id, userId, requestUrl, ipAddress, eventType, riskScore, timestamp) :
        return new UserBehavior(
                String.valueOf(entity.getId()),
                entity.getUserId(),
                entity.getIpAddress(),
                entity.getEventType(),
                entity.getRequestUrl(),
                score,
                entity.getTimestamp()
        );
    }
}