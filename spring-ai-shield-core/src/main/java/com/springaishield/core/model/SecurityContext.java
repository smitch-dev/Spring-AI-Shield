package com.springaishield.core.model;


public record SecurityContext(String userId, String requestUrl, String ipAddress) {
}