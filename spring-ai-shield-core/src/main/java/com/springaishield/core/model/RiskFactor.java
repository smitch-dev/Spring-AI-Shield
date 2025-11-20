package com.springaishield.core.model;

/**
 * Représente un facteur (règle, modèle ML, heuristique) contribuant au score final.
 */
public record RiskFactor(
        String name, // Nom du facteur (ex: IP_HISTORY, SQL_INJECTION_HEURISTIC)
        double weight, // Poids/impact de ce facteur sur le score final (ex: 0.3)
        String detail // Description du résultat du facteur (ex: 'Nouvelle IP non vue', 'Contient mot-clé SQL')
) {}