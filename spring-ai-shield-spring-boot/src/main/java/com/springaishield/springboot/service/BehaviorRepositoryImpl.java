package com.springaishield.springboot.service;

import com.springaishield.core.model.RiskScore;
import com.springaishield.core.model.UserBehavior;
import com.springaishield.core.repository.BehaviorRepository;
import com.springaishield.springboot.persistence.entity.UserBehaviorEntity;
import com.springaishield.springboot.persistence.jpa.JpaBehaviorRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Concrete implementation of the COEUR interface, using Spring Data JPA.
 */
@Service
public class BehaviorRepositoryImpl implements BehaviorRepository {

    private final JpaBehaviorRepository jpaRepository;

    public BehaviorRepositoryImpl(JpaBehaviorRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public UserBehavior save(UserBehavior behavior) {


        String factorSummary = behavior.riskScore().contributingFactors().stream()
                .map(f -> f.name() + ":" + f.weight())
                .collect(Collectors.joining("; "));


        // 1. Conversion of the Core model to the JPA entity
        UserBehaviorEntity entity = new UserBehaviorEntity(
                behavior.userId(),
                behavior.ipAddress(),
                factorSummary.isEmpty() ? behavior.eventType() : factorSummary, // Utilise les facteurs ou l'eventType de base
                behavior.requestUrl(),
                behavior.riskScore().score()
        );

        // 2. Saved to the database
        UserBehaviorEntity savedEntity = jpaRepository.save(entity);

        // 3. Reconversion vers le modèle Core à retourner
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
        RiskScore score = new RiskScore(entity.getRiskScore(), "Raison inconnue (Récupéré de BDD)");

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