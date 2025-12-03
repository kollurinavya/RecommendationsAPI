package com.microsoft.recommendation.model;

import java.time.ZonedDateTime;
import java.util.List;

public class CollaborativeRecommendationResponse {
    
    private String userId;
    private List<RecommendationItem> recommendations;
    private List<SimilarUser> similarUsers;
    private String generatedAt;
    
    public CollaborativeRecommendationResponse() {}
    
    public CollaborativeRecommendationResponse(String userId, 
                                               List<RecommendationItem> recommendations,
                                               List<SimilarUser> similarUsers) {
        this.userId = userId;
        this.recommendations = recommendations;
        this.similarUsers = similarUsers;
        this.generatedAt = ZonedDateTime.now().toString();
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
    
    public List<SimilarUser> getSimilarUsers() {
        return similarUsers;
    }
    
    public void setSimilarUsers(List<SimilarUser> similarUsers) {
        this.similarUsers = similarUsers;
    }
    
    public String getGeneratedAt() {
        return generatedAt;
    }
    
    public void setGeneratedAt(String generatedAt) {
        this.generatedAt = generatedAt;
    }
}
