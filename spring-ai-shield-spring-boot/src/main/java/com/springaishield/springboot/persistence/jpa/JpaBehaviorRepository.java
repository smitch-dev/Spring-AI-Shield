package com.springaishield.springboot.persistence.jpa;

import com.springaishield.springboot.persistence.entity.UserBehaviorEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Interface Spring Data JPA. Spring génère automatiquement les méthodes CRUD.
 */
public interface JpaBehaviorRepository extends JpaRepository<UserBehaviorEntity, Long> {

    /**
     * Implémente la méthode findRecentByUserId nécessaire pour l'IA.
     * @param userId L'ID de l'utilisateur.
     * @param limit Le nombre maximal d'enregistrements.
     */
    @Query(value = "SELECT * FROM ai_user_behavior WHERE user_id = :userId ORDER BY timestamp DESC LIMIT :limit", nativeQuery = true)
    List<UserBehaviorEntity> findRecentByUserIdNative(@Param("userId") String userId, @Param("limit") int limit);
}