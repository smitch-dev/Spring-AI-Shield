package com.springaishield.springboot.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "ai_user_behavior")
public class UserBehaviorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;
    private String ipAddress;
    private String eventType;
    private String requestUrl;


    private double riskScore;

    private Instant timestamp;

    public UserBehaviorEntity() {}

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