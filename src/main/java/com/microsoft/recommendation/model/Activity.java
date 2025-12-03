package com.microsoft.recommendation.model;

import jakarta.validation.constraints.NotBlank;

public class Activity {
    
    @NotBlank(message = "userId is required")
    private String userId;
    
    @NotBlank(message = "itemId is required")
    private String itemId;
    
    @NotBlank(message = "action is required")
    private String action;
    
    public Activity() {}
    
    public Activity(String userId, String itemId, String action) {
        this.userId = userId;
        this.itemId = itemId;
        this.action = action;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getItemId() {
        return itemId;
    }
    
    public void setItemId(String itemId) {
        this.itemId = itemId;
    }
    
    public String getAction() {
        return action;
    }
    
    public void setAction(String action) {
        this.action = action;
    }
}
