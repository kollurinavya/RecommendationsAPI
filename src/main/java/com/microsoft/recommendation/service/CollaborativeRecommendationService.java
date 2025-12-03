package com.microsoft.recommendation.service;

import com.microsoft.recommendation.model.RecommendationItem;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CollaborativeRecommendationService {
    
    private final VectorService vectorService;
    private final VectorStore vectorStore;
    private static final int DEFAULT_TOP_N_SIMILAR_USERS = 5;
    
    public CollaborativeRecommendationService(VectorService vectorService, VectorStore vectorStore) {
        this.vectorService = vectorService;
        this.vectorStore = vectorStore;
    }
    
    /**
     * Get collaborative filtering recommendations for a user
     */
    public List<RecommendationItem> getCollaborativeRecommendations(String userId, int k) {
        // Step 1: Get user's normalized vector
        float[] userVector = vectorService.getUserVector(userId);
        if (userVector == null) {
            return new ArrayList<>();
        }
        
        // Step 2: Find top-N similar users
        List<Map.Entry<String, Float>> similarUsers = vectorStore.topKSimilarUsers(
                userVector, 
                DEFAULT_TOP_N_SIMILAR_USERS, 
                userId
        );
        
        if (similarUsers.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Step 3: Get items the target user already has
        Set<String> userItems = vectorService.getUserItems(userId);
        
        // Step 4: Collect and score candidate items from similar users
        Map<String, Float> itemScores = new HashMap<>();
        
        for (Map.Entry<String, Float> similarUser : similarUsers) {
            String neighborId = similarUser.getKey();
            float similarity = similarUser.getValue();
            
            // Get items from this similar user
            Set<String> neighborItems = vectorService.getUserItems(neighborId);
            
            for (String itemId : neighborItems) {
                // Skip if target user already has this item
                if (userItems.contains(itemId)) {
                    continue;
                }
                
                // Get the weight of this item for the neighbor
                float neighborItemWeight = vectorService.getUserItemWeight(neighborId, itemId);
                
                // Accumulate score: similarity * neighborItemWeight
                float contribution = similarity * neighborItemWeight;
                itemScores.merge(itemId, contribution, Float::sum);
            }
        }
        
        // Step 5: Rank and return top-K
        return itemScores.entrySet().stream()
                .sorted(Map.Entry.<String, Float>comparingByValue().reversed())
                .limit(k)
                .map(entry -> new RecommendationItem(
                        entry.getKey(),
                        Math.round(entry.getValue() * 100.0) / 100.0
                ))
                .collect(Collectors.toList());
    }
    
    /**
     * Get similar users for a given user (for debugging/transparency)
     */
    public List<Map.Entry<String, Float>> getSimilarUsers(String userId, int topN) {
        float[] userVector = vectorService.getUserVector(userId);
        if (userVector == null) {
            return new ArrayList<>();
        }
        
        return vectorStore.topKSimilarUsers(userVector, topN, userId);
    }
}
