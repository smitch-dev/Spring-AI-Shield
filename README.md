# 🛡️ Spring AI Shield

**Spring AI Shield** Spring AI Shield is an adaptive security library for Spring Boot applications, utilizing a hybrid Artificial Intelligence engine to assess the real-time risk of every incoming HTTP request and block abnormal or malicious behavior before it reaches your core business logic.

## 🚀 Key Features

* **Highest Precedence Filter:** The security filter (AIShieldFilter) is automatically registered with the highest possible precedence (Ordered.HIGHEST_PRECEDENCE), ensuring it intercepts and blocks requests before the Spring Security FilterChainProxy and any application controllers.
* **Hybrid Risk Scoring :** Combines content analysis (heuristics for SQLi/XSS) with Behavioral Analysis (Machine Learning prediction based on persistent user history).
* **Zero-Config Auto-Configuration:** Seamless integration via Spring Boot's modern auto-configuration import mechanism (META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports). No manual Java configuration needed.
* **Behavioral Persistence :** Leverages Spring Data JPA to record historical behavior (risk score, URL, IP, etc.) into a database (H2 in-memory by default).

## 💡 Usage Guide

To integrate Spring AI Shield into your Spring Boot project:

### 1. Add the Maven Dependency

Add the following dependency to your application's pom.xml:


```xml
<dependency>
    <groupId>com.springaishield</groupId>
    <artifactId>spring-ai-shield-spring-boot</artifactId>
    <version>2.1.0-SNAPSHOT</version> 
</dependency>
```

### 2. Auto-Activation
Once the dependency is added, the library is automatically active. The necessary components are registered:

The AIShieldFilter starts monitoring all endpoints (/*).

The BehavioralScoringEngine (the core AI service) is instantiated.

### 3. Optional Configuration (Database)

The auto-configuration defaults to using H2 in-memory. To switch to an external database like PostgreSQL, configure the standard Spring datasource properties:


## Example PostgreSQL Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/aishield_db
spring.datasource.username=dbuser
spring.datasource.password=dbpass
# Ensure the DDL-auto is set to update to create the 'ai_user_behavior' table
spring.jpa.hibernate.ddl-auto=update

### 4. How Blocking Works

The AIShieldFilter executes before all other security layers.

The RiskScoringService computes a risk score.

Blocking Logic: If the Risk Score > 0.8 (current internal threshold):

    The request is immediately blocked.

    An HTTP 403 (Forbidden) status is returned to the client.

Allowed Logic: If the request is permitted, the behavior and score are recorded in the database for future risk scoring.


## 🛡️ Testing the Shield (Example Attack)

You can test the successful deployment of the shield using a simple URL-based SQL Injection query:

Test Query: http://your-app:8080/search?q=union+select+*

This request will be intercepted, scored above the threshold, and should result in a 403 Forbidden response.






