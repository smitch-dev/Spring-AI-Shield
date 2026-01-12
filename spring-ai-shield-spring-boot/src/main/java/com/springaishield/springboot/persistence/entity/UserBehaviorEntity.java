package com.springaishield.springboot.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "ai_user_behavior")
public class UserBehaviorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;
    private String ipAddress;
    private String eventType; // Contiendra "ACCESS_GRANTED", "ACCESS_DENIED" ou le résumé des facteurs

    @Column(length = 1024) // Les URLs avec paramètres peuvent être longues
    private String requestUrl;

    private double riskScore;

    private Instant timestamp;

    // Constructeur par défaut requis par JPA
    public UserBehaviorEntity() {}

    // Constructeur complet
    public UserBehaviorEntity(String userId, String ipAddress, String eventType, String requestUrl, double riskScore) {
        this.userId = userId;
        this.ipAddress = ipAddress;
        this.eventType = eventType;
        this.requestUrl = requestUrl;
        this.riskScore = riskScore;
        this.timestamp = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getEventType() {
        return eventType;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public double getRiskScore() {
        return riskScore;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public void setRiskScore(double riskScore) {
        this.riskScore = riskScore;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }



}