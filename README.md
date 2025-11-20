# 🛡️ Spring AI Shield

**Spring AI Shield** est une bibliothèque de sécurité adaptative pour les applications Spring Boot, utilisant un moteur d'intelligence artificielle hybride pour évaluer le risque en temps réel de chaque requête HTTP et bloquer les comportements anormaux ou malveillants avant qu'ils n'atteignent votre logique métier.

## 🚀 Fonctionnalités Clés

* **Scoring Hybride :** Combine l'analyse de contenu (heuristiques SQLi/XSS) et l'analyse comportementale (prédiction Machine Learning basée sur l'historique utilisateur).
* **Intégration Transparente :** Auto-configuration Spring Boot pour une installation facile. Le moteur s'insère dans la `SecurityFilterChain`.
* **Persistance :** Utilise Spring Data JPA pour enregistrer l'historique des comportements (score de risque, URL, IP, etc.) dans une base de données (H2 par défaut).

## 💡 Utilisation

Pour utiliser `Spring AI Shield` dans votre projet Spring Boot :

### 1. Ajoutez les dépendances Maven

Ajoutez la dépendance vers le module Spring Boot de la librairie dans le `pom.xml` de votre application :


```xml
<dependency>
    <groupId>com.springaishield</groupId>
    <artifactId>spring-ai-shield-spring-boot</artifactId>
    <version>2.1.0-SNAPSHOT</version> 
</dependency>
```

### 2.Configurez l'application (Optionnel)
L'auto-configuration utilise H2 en mémoire par défaut. Si vous souhaitez utiliser une base de données externe, configurez-la dans application.properties :

## Exemple de configuration PostgreSQL :
spring.datasource.url=jdbc:postgresql://localhost:5432/aishield_db
spring.datasource.username=dbuser
spring.datasource.password=dbpass
spring.jpa.hibernate.ddl-auto=update

### 3. Fonctionnement

Une fois la dépendance ajoutée, le filtre AIShieldFilter est automatiquement inséré dans la chaîne de sécurité.

Si Risk Score > 0.8 : La requête est bloquée avec un statut HTTP 403 (Forbidden).

Sinon : La requête est autorisée et le comportement est enregistré.









