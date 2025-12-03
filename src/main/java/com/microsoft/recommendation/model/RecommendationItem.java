package com.microsoft.recommendation.model;

public class RecommendationItem {
    
    private String itemId;
    private double score;
    
    public RecommendationItem() {}
    
    public RecommendationItem(String itemId, double score) {
        this.itemId = itemId;
        this.score = score;
    }
    
    public String getItemId() {
        return itemId;
    }
    
    public void setItemId(String itemId) {
        this.itemId = itemId;
    }
    
    public double getScore() {
        return score;
    }
    
    public void setScore(double score) {
        this.score = score;
    }
}
