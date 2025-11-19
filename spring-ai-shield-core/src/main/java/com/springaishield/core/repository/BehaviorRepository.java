package com.springaishield.core.repository;

import com.springaishield.core.model.UserBehavior;
import java.util.List;

/**
 * Interface pour la persistance des événements comportementaux.
 * Ce contrat est indépendant de la technologie de base de données (JPA, Mongo, etc.).
 */
public interface BehaviorRepository {

    /**
     * Enregistre un événement comportemental.
     * @param behavior L'enregistrement d'événement à sauvegarder.
     * @return L'enregistrement sauvegardé (avec un ID rempli).
     */
    UserBehavior save(UserBehavior behavior);

    /**
     * Récupère l'historique récent des comportements d'un utilisateur.
     * @param userId L'ID de l'utilisateur.
     * @param limit Le nombre maximal d'enregistrements à retourner.
     * @return Une liste triée des comportements récents.
     */
    List<UserBehavior> findRecentByUserId(String userId, int limit);
}