package com.microsoft.recommendation.service;

import com.microsoft.recommendation.model.Activity;
import com.microsoft.recommendation.model.RecommendationItem;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {
    
    private final VectorStore vectorStore;
    private final VectorService vectorService;
    private final List<Activity> allActivities = new ArrayList<>();
    private static final int VECTOR_DIMENSION = 10;
    
    public RecommendationService(VectorStore vectorStore, VectorService vectorService) {
        this.vectorStore = vectorStore;
        this.vectorService = vectorService;
    }
    
    /**
     * Ingest user activities
     */
    public void ingestActivities(List<Activity> activities) {
        // Clear existing data
        allActivities.clear();
        vectorStore.clear();
        
        // Store all activities
        allActivities.addAll(activities);
        
        // Collect all unique items and generate vectors
        Set<String> allItems = activities.stream()
                .map(Activity::getItemId)
                .collect(Collectors.toSet());
        
        // Generate and store vectors for all items (keeping for vector store compatibility)
        for (String itemId : allItems) {
            double[] vector = generateDeterministicVector(itemId);
            vectorStore.add(itemId, vector);
        }
        
        // Build user vectors for collaborative filtering
        vectorService.buildUserVectors(activities);
        
        // Store user vectors in VectorStore
        Map<String, float[]> userVectors = vectorService.getAllUserVectors();
        for (Map.Entry<String, float[]> entry : userVectors.entrySet()) {
            vectorStore.upsertUserVector(entry.getKey(), entry.getValue());
        }
    }
    
    /**
     * Get recommendations for a user based on:
     * 1. User's own interaction history (items they've interacted with)
     * 2. Action priority: add_to_cart > view
     * 3. Frequency: number of times the user interacted with each item
     */
    public List<RecommendationItem> getRecommendations(String userId, int k) {
        // Get the user's own activities
        List<Activity> userActivities = allActivities.stream()
                .filter(activity -> activity.getUserId().equals(userId))
                .collect(Collectors.toList());
        
        if (userActivities.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Calculate scores for each item based on the user's interactions
        Map<String, ItemScore> itemScores = new HashMap<>();
        
        for (Activity activity : userActivities) {
            String itemId = activity.getItemId();
            
            itemScores.putIfAbsent(itemId, new ItemScore(itemId));
            ItemScore score = itemScores.get(itemId);
            
            if ("add_to_cart".equalsIgnoreCase(activity.getAction())) {
                score.addToCartCount++;
            } else if ("view".equalsIgnoreCase(activity.getAction())) {
                score.viewCount++;
            }
        }
        
        // Sort by priority: add_to_cart count DESC, then view count DESC
        List<RecommendationItem> recommendations = itemScores.values().stream()
                .sorted(Comparator
                        .comparingInt((ItemScore s) -> s.addToCartCount).reversed()
                        .thenComparingInt((ItemScore s) -> s.viewCount).reversed()
                        .thenComparing(s -> s.itemId)) // tie-breaker for deterministic results
                .limit(k)
                .map(score -> new RecommendationItem(
                        score.itemId,
                        calculateScore(score.addToCartCount, score.viewCount)
                ))
                .collect(Collectors.toList());
        
        return recommendations;
    }
    
    /**
     * Calculate a normalized score based on action priorities
     */
    private double calculateScore(int addToCartCount, int viewCount) {
        // Weight: add_to_cart = 10 points, view = 1 point
        double rawScore = (addToCartCount * 10.0) + (viewCount * 1.0);
        // Normalize to 0-1 range (assuming max realistic values)
        double maxPossible = 100.0;
        return Math.min(Math.round((rawScore / maxPossible) * 100.0) / 100.0, 1.0);
    }
    
    /**
     * Inner class to track item scoring metrics
     */
    private static class ItemScore {
        String itemId;
        int addToCartCount = 0;
        int viewCount = 0;
        
        ItemScore(String itemId) {
            this.itemId = itemId;
        }
    }
    
    /**
     * Generate deterministic vector for an item based on its ID
     */
    private double[] generateDeterministicVector(String itemId) {
        Random random = new Random(itemId.hashCode());
        double[] vector = new double[VECTOR_DIMENSION];
        
        for (int i = 0; i < VECTOR_DIMENSION; i++) {
            vector[i] = random.nextDouble();
        }
        
        return vector;
    }
    
    /**
     * Get all stored activities count
     */
    public int getActivityCount() {
        return allActivities.size();
    }
}
