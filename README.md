# 🛡️ Spring AI Shield

**Spring AI Shield** is an adaptive security library for Spring Boot applications, utilizing a hybrid risk assessment engine to analyze every incoming HTTP request and block abnormal or malicious behavior (SQLi, XSS) before it reaches your core business logic.

## 🚀 Key Features

* **Highest Precedence Filter:** The security filter (`AIShieldFilter`) is automatically registered with the highest possible precedence (`Ordered.HIGHEST_PRECEDENCE`), ensuring it intercepts and blocks requests before the Spring Security FilterChainProxy and any application controllers.
* **Hybrid Risk Scoring:** Combines advanced content analysis (heuristics for SQLi/XSS with UTF-8 URL decoding) with Behavioral Analysis (Machine Learning prediction based on persistent user history).
* **Zero-Config Auto-Configuration:** Seamless integration via Spring Boot 3's modern auto-configuration import mechanism (`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`). No manual Java configuration needed.
* **Behavioral Persistence:** Leverages Spring Data JPA to record historical behavior (risk score, URL, IP, event type, etc.) into a database for audit and future risk scoring.

## 💡 Usage Guide

To integrate Spring AI Shield into your Spring Boot project:

### 1. Add the Maven Dependency

Step 1. Add the JitPack repository to your build file
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```
Add the following dependency to your application's `pom.xml`:

Step 2. Add the dependency
```xml
<dependency>
    <groupId>com.github.smitch-dev</groupId>
    <artifactId>spring-ai-shield-spring-boot</artifactId>
    <version>v3.0.0</version>
</dependency>
```

### 2. Auto-Activation
Once the dependency is added, the library is automatically active. The necessary components are registered:

The AIShieldFilter starts monitoring all endpoints (/*).

The BehavioralScoringEngine (the core scoring service) is instantiated.

The JPA Repository is configured to handle behavioral logs.

### 3. Optional Configuration (Database)
The auto-configuration defaults to using an H2 in-memory database. To switch to an external database like PostgreSQL, configure the standard Spring datasource properties in your application.properties

## Example PostgreSQL Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/aishield_db
spring.datasource.username=dbuser
spring.datasource.password=dbpass
# Ensure the DDL-auto is set to update to create the 'ai_user_behavior' table
spring.jpa.hibernate.ddl-auto=update

### 4. How Blocking Works

Interception: The AIShieldFilter executes before all other security layers and decodes URL parameters (e.g., %3Cscript%3E becomes <script>).

Scoring: The RiskScoringService computes a risk score between 0.0 and 1.0.

Blocking Logic: If the Risk Score ≥ 0.5 (current internal threshold):

The request is immediately blocked.

An HTTP 403 (Forbidden) status is returned to the client.

The event is recorded as ACCESS_DENIED in the database.

Allowed Logic: If the score is below 0.5, the request proceeds to the next filter, and the behavior is recorded as ACCESS_GRANTED.

Allowed Logic: If the request is permitted, the behavior and score are recorded in the database for future risk scoring.


## 🛡️ Testing the Shield (Example Attack)

You can test the successful deployment of the shield using simple URL-based attacks:

SQL Injection Test: http://localhost:8080/search?q=select+*+from+users

XSS Attack Test: http://localhost:8080/search?q=<script>alert(1)</script>

Encoded XSS Test: http://localhost:8080/search?q=%3Cscript%3Ealert(1)%3C/script%3E

These requests will be intercepted, scored at 0.5 or higher, and will result in a 403 Forbidden response, protecting your application.






