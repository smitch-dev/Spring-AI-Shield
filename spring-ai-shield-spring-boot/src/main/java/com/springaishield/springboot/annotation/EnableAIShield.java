package com.springaishield.springboot.annotation;

import com.springaishield.springboot.configuration.AIShieldAutoConfiguration;
import java.lang.annotation.*;
import org.springframework.context.annotation.Import;

/**
 * Active le système de sécurité adaptatif Spring AI Shield.
 * L'annotation @Import déclenche l'auto-configuration Spring nécessaire
 * pour enregistrer les services et filtres d'IA.
 */
@Target(ElementType.TYPE) // Peut être appliquée uniquement à une classe de type (comme @SpringBootApplication)
@Retention(RetentionPolicy.RUNTIME) // L'annotation est conservée et lisible à l'exécution
@Documented // Inclut l'annotation dans la Javadoc
@Import(AIShieldAutoConfiguration.class) // L'action clé : dit à Spring de charger notre classe de configuration

public @interface EnableAIShield {
}