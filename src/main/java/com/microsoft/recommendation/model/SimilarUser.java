package com.microsoft.recommendation.model;

public class SimilarUser {
    
    private String userId;
    private double similarity;
    
    public SimilarUser() {}
    
    public SimilarUser(String userId, double similarity) {
        this.userId = userId;
        this.similarity = similarity;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public double getSimilarity() {
        return similarity;
    }
    
    public void setSimilarity(double similarity) {
        this.similarity = similarity;
    }
}
