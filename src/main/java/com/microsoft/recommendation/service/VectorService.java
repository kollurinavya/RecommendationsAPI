package com.microsoft.recommendation.service;

import com.microsoft.recommendation.model.Activity;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class VectorService {
    
    // Action weights
    private static final float VIEW_WEIGHT = 1.0f;
    private static final float ADD_TO_CART_WEIGHT = 3.0f;
    
    // Global item-to-index mapping
    private final Map<String, Integer> itemToIndex = new LinkedHashMap<>();
    private final List<String> indexToItem = new ArrayList<>();
    
    // User data structures
    private final Map<String, float[]> userVectors = new HashMap<>();
    private final Map<String, Map<String, Float>> userItemWeights = new HashMap<>();
    private final Map<String, Set<String>> userToItems = new HashMap<>();
    
    /**
     * Build user vectors from activities
     */
    public void buildUserVectors(List<Activity> activities) {
        // Clear existing data
        itemToIndex.clear();
        indexToItem.clear();
        userVectors.clear();
        userItemWeights.clear();
        userToItems.clear();
        
        // Step 1: Build global item index
        Set<String> allItems = new HashSet<>();
        for (Activity activity : activities) {
            allItems.add(activity.getItemId());
        }
        
        int index = 0;
        for (String itemId : allItems.stream().sorted().toList()) {
            itemToIndex.put(itemId, index);
            indexToItem.add(itemId);
            index++;
        }
        
        int numItems = itemToIndex.size();
        
        // Step 2: Build raw user vectors and track weights
        Map<String, float[]> rawVectors = new HashMap<>();
        
        for (Activity activity : activities) {
            String userId = activity.getUserId();
            String itemId = activity.getItemId();
            float weight = getActionWeight(activity.getAction());
            
            // Initialize user structures if needed
            rawVectors.putIfAbsent(userId, new float[numItems]);
            userItemWeights.putIfAbsent(userId, new HashMap<>());
            userToItems.putIfAbsent(userId, new HashSet<>());
            
            // Add to vector
            int itemIndex = itemToIndex.get(itemId);
            rawVectors.get(userId)[itemIndex] += weight;
            
            // Track weight and items
            userItemWeights.get(userId).merge(itemId, weight, Float::sum);
            userToItems.get(userId).add(itemId);
        }
        
        // Step 3: Normalize vectors
        for (Map.Entry<String, float[]> entry : rawVectors.entrySet()) {
            String userId = entry.getKey();
            float[] rawVector = entry.getValue();
            float[] normalizedVector = normalize(rawVector);
            userVectors.put(userId, normalizedVector);
        }
    }
    
    /**
     * Get action weight
     */
    private float getActionWeight(String action) {
        if ("add_to_cart".equalsIgnoreCase(action)) {
            return ADD_TO_CART_WEIGHT;
        } else if ("view".equalsIgnoreCase(action)) {
            return VIEW_WEIGHT;
        }
        return 1.0f; // default
    }
    
    /**
     * Normalize a vector to unit length
     */
    private float[] normalize(float[] vector) {
        float sumSquares = 0.0f;
        for (float v : vector) {
            sumSquares += v * v;
        }
        
        float norm = (float) Math.sqrt(sumSquares);
        if (norm == 0.0f) {
            return vector;
        }
        
        float[] normalized = new float[vector.length];
        for (int i = 0; i < vector.length; i++) {
            normalized[i] = vector[i] / norm;
        }
        return normalized;
    }
    
    /**
     * Get normalized vector for a user
     */
    public float[] getUserVector(String userId) {
        return userVectors.get(userId);
    }
    
    /**
     * Get all user IDs
     */
    public Set<String> getAllUserIds() {
        return new HashSet<>(userVectors.keySet());
    }
    
    /**
     * Get items a user interacted with
     */
    public Set<String> getUserItems(String userId) {
        return userToItems.getOrDefault(userId, new HashSet<>());
    }
    
    /**
     * Get weight for a specific user-item interaction
     */
    public float getUserItemWeight(String userId, String itemId) {
        return userItemWeights.getOrDefault(userId, new HashMap<>())
                .getOrDefault(itemId, 0.0f);
    }
    
    /**
     * Get all user vectors
     */
    public Map<String, float[]> getAllUserVectors() {
        return new HashMap<>(userVectors);
    }
    
    /**
     * Get number of items in the index
     */
    public int getNumItems() {
        return itemToIndex.size();
    }
}
