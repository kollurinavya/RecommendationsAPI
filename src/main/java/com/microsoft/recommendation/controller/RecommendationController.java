package com.microsoft.recommendation.controller;

import com.microsoft.recommendation.model.Activity;
import com.microsoft.recommendation.model.CollaborativeRecommendationResponse;
import com.microsoft.recommendation.model.HealthResponse;
import com.microsoft.recommendation.model.RecommendationResponse;
import com.microsoft.recommendation.model.SimilarUser;
import com.microsoft.recommendation.service.CollaborativeRecommendationService;
import com.microsoft.recommendation.service.RecommendationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping
public class RecommendationController {
    
    private final RecommendationService recommendationService;
    private final CollaborativeRecommendationService collaborativeRecommendationService;
    
    public RecommendationController(RecommendationService recommendationService,
                                   CollaborativeRecommendationService collaborativeRecommendationService) {
        this.recommendationService = recommendationService;
        this.collaborativeRecommendationService = collaborativeRecommendationService;
    }
    
    /**
     * GET / - Root endpoint to avoid Whitelabel error on '/'
     */
    @GetMapping(path = {"", "/"})
    public ResponseEntity<Map<String, Object>> root() {
        return ResponseEntity.ok(Map.of(
                "service", "recommendation-service",
                "status", "ok",
                "endpoints", List.of("/health", "/ingest", "/recommend", "/recommendCollaborative")
        ));
    }

    /**
     * POST /ingest - Ingest user activities
     */
    @PostMapping("/ingest")
    public ResponseEntity<Map<String, Object>> ingestActivities(@Valid @RequestBody List<Activity> activities) {
        recommendationService.ingestActivities(activities);
        
        return ResponseEntity.ok(Map.of(
                "message", "Activities ingested successfully",
                "count", activities.size(),
                "status", "success"
        ));
    }
    
    /**
     * GET /recommend - Get recommendations for a user
     */
    @GetMapping("/recommend")
    public ResponseEntity<RecommendationResponse> getRecommendations(
            @RequestParam String userId,
            @RequestParam(defaultValue = "5") int k) {
        
        var recommendations = recommendationService.getRecommendations(userId, k);
        var response = new RecommendationResponse(userId, recommendations);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * GET /health - Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<HealthResponse> health() {
        return ResponseEntity.ok(new HealthResponse("ok"));
    }
    
    /**
     * GET /recommendCollaborative - Get collaborative filtering recommendations
     */
    @GetMapping("/recommendCollaborative")
    public ResponseEntity<CollaborativeRecommendationResponse> getCollaborativeRecommendations(
            @RequestParam String userId,
            @RequestParam(defaultValue = "5") int k) {
        
        var recommendations = collaborativeRecommendationService.getCollaborativeRecommendations(userId, k);
        var similarUsers = collaborativeRecommendationService.getSimilarUsers(userId, 5)
                .stream()
                .map(entry -> new SimilarUser(entry.getKey(), Math.round(entry.getValue() * 100.0) / 100.0))
                .toList();
        
        var response = new CollaborativeRecommendationResponse(userId, recommendations, similarUsers);
        return ResponseEntity.ok(response);
    }
}
