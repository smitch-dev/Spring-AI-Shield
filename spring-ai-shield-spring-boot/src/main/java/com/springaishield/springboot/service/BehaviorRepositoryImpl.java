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
 * Implémentation concrète de l'interface du COEUR, utilisant Spring Data JPA.
 */
@Service // Rend cette classe injectable par Spring
public class BehaviorRepositoryImpl implements BehaviorRepository {

    private final JpaBehaviorRepository jpaRepository;

    public BehaviorRepositoryImpl(JpaBehaviorRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    // --- Mapper les entités JPA vers les modèles du Cœur (et vice-versa) ---

    @Override
    public UserBehavior save(UserBehavior behavior) {
        // 1. Conversion du modèle Core vers l'entité JPA
        UserBehaviorEntity entity = new UserBehaviorEntity(
                behavior.userId(),
                behavior.ipAddress(),
                behavior.eventType(),
                behavior.requestUrl(),
                behavior.riskScore().score() // Stocke seulement le double du score
        );

        // 2. Sauvegarde dans la base de données
        UserBehaviorEntity savedEntity = jpaRepository.save(entity);

        // 3. Reconversion vers le modèle Core à retourner
        return mapEntityToCore(savedEntity);
    }

    @Override
    public List<UserBehavior> findRecentByUserId(String userId, int limit) {
        List<UserBehaviorEntity> entities = jpaRepository.findRecentByUserIdNative(userId, limit);

        // Conversion des entités JPA en modèles Core
        return entities.stream()
                .map(this::mapEntityToCore)
                .collect(Collectors.toList());
    }

    // Méthode utilitaire pour convertir l'entité JPA en modèle Core
    private UserBehavior mapEntityToCore(UserBehaviorEntity entity) {
        // Attention : Le Reason (raison) est perdu lors du stockage JPA simple,
        // nous le reconstruisons avec une valeur par défaut pour l'instant.
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