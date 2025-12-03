package com.microsoft.recommendation.service;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class VectorStore {
    
    private final Map<String, double[]> itemVectors = new HashMap<>();
    private final Map<String, float[]> userVectors = new HashMap<>();
    
    /**
     * Add an item with its vector representation
     */
    public void add(String itemId, double[] vector) {
        itemVectors.put(itemId, vector);
    }
    
    /**
     * Query for top-k similar items to the given vector
     */
    public List<Map.Entry<String, Double>> query(double[] vector, int k) {
        return itemVectors.entrySet().stream()
                .map(entry -> new AbstractMap.SimpleEntry<>(
                        entry.getKey(),
                        cosineSimilarity(vector, entry.getValue())
                ))
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(k)
                .collect(Collectors.toList());
    }
    
    /**
     * Check if an item exists in the vector store
     */
    public boolean contains(String itemId) {
        return itemVectors.containsKey(itemId);
    }
    
    /**
     * Get vector for an item
     */
    public double[] getVector(String itemId) {
        return itemVectors.get(itemId);
    }
    
    /**
     * Get all item IDs
     */
    public Set<String> getAllItemIds() {
        return new HashSet<>(itemVectors.keySet());
    }
    
    /**
     * Calculate cosine similarity between two vectors
     */
    private double cosineSimilarity(double[] v1, double[] v2) {
        if (v1.length != v2.length) {
            throw new IllegalArgumentException("Vectors must have the same length");
        }
        
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        
        for (int i = 0; i < v1.length; i++) {
            dotProduct += v1[i] * v2[i];
            normA += v1[i] * v1[i];
            normB += v2[i] * v2[i];
        }
        
        if (normA == 0.0 || normB == 0.0) {
            return 0.0;
        }
        
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
    
    /**
     * Clear all vectors
     */
    public void clear() {
        itemVectors.clear();
        userVectors.clear();
    }
    
    // ========== User Vector Operations ==========
    
    /**
     * Upsert a user vector
     */
    public void upsertUserVector(String userId, float[] vector) {
        userVectors.put(userId, vector);
    }
    
    /**
     * Get a user vector
     */
    public float[] getUserVector(String userId) {
        return userVectors.get(userId);
    }
    
    /**
     * Find top-K similar users based on cosine similarity (dot product for normalized vectors)
     */
    public List<Map.Entry<String, Float>> topKSimilarUsers(float[] queryVector, int k, String excludeUserId) {
        return userVectors.entrySet().stream()
                .filter(entry -> !entry.getKey().equals(excludeUserId))
                .map(entry -> new AbstractMap.SimpleEntry<>(
                        entry.getKey(),
                        dotProduct(queryVector, entry.getValue())
                ))
                .sorted(Map.Entry.<String, Float>comparingByValue().reversed())
                .limit(k)
                .collect(Collectors.toList());
    }
    
    /**
     * Calculate dot product between two vectors
     */
    private float dotProduct(float[] v1, float[] v2) {
        if (v1.length != v2.length) {
            throw new IllegalArgumentException("Vectors must have the same length");
        }
        
        float result = 0.0f;
        for (int i = 0; i < v1.length; i++) {
            result += v1[i] * v2[i];
        }
        return result;
    }
}
