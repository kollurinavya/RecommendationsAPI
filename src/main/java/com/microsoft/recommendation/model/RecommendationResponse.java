package com.microsoft.recommendation.model;

import java.time.ZonedDateTime;
import java.util.List;

public class RecommendationResponse {
    
    private String userId;
    private List<RecommendationItem> recommendations;
    private String generatedAt;
    
    public RecommendationResponse() {}
    
    public RecommendationResponse(String userId, List<RecommendationItem> recommendations) {
        this.userId = userId;
        this.recommendations = recommendations;
        this.generatedAt = ZonedDateTime.now().toString();
    }
    
    public RecommendationResponse(String userId, List<RecommendationItem> recommendations, String generatedAt) {
        this.userId = userId;
        this.recommendations = recommendations;
        this.generatedAt = generatedAt;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public List<RecommendationItem> getRecommendations() {
        return recommendations;
    }
    
    public void setRecommendations(List<RecommendationItem> recommendations) {
        this.recommendations = recommendations;
    }
    
    public String getGeneratedAt() {
        return generatedAt;
    }
    
    public void setGeneratedAt(String generatedAt) {
        this.generatedAt = generatedAt;
    }
}
