package com.example.backend.document.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service to track document views and prevent duplicate counting
 * within a time window (30 minutes default)
 */
@Service
@Slf4j
public class DocumentViewTracker {
    
    private static final long VIEW_COOLDOWN_MINUTES = 30;
    
    // Map of "documentId:identifier" -> lastViewTimestamp
    private final Map<String, Instant> viewTracker = new ConcurrentHashMap<>();
    
    /**
     * Check if view should be counted for this document and identifier
     * @param documentId Document ID
     * @param identifier User identifier (IP address, session ID, or user ID)
     * @return true if view should be counted, false if within cooldown period
     */
    public boolean shouldCountView(Long documentId, String identifier) {
        String key = documentId + ":" + identifier;
        Instant now = Instant.now();
        
        Instant lastView = viewTracker.get(key);
        
        if (lastView == null) {
            // First view - count it
            viewTracker.put(key, now);
            cleanupOldEntries();
            log.debug("First view counted for document {} by {}", documentId, identifier);
            return true;
        }
        
        long minutesSinceLastView = (now.toEpochMilli() - lastView.toEpochMilli()) / (1000 * 60);
        
        if (minutesSinceLastView >= VIEW_COOLDOWN_MINUTES) {
            // Outside cooldown period - count it
            viewTracker.put(key, now);
            log.debug("View counted for document {} by {} ({}m since last view)", 
                     documentId, identifier, minutesSinceLastView);
            return true;
        }
        
        // Within cooldown period - don't count
        log.debug("View NOT counted for document {} by {} (only {}m since last view)", 
                 documentId, identifier, minutesSinceLastView);
        return false;
    }
    
    /**
     * Cleanup entries older than the cooldown period to prevent memory leak
     */
    private void cleanupOldEntries() {
        if (viewTracker.size() > 10000) { // Only cleanup when map gets large
            Instant cutoff = Instant.now().minusSeconds(VIEW_COOLDOWN_MINUTES * 60);
            viewTracker.entrySet().removeIf(entry -> entry.getValue().isBefore(cutoff));
            log.debug("Cleaned up old view tracking entries. Current size: {}", viewTracker.size());
        }
    }
    
    /**
     * Get identifier from request (IP address, session, or user ID)
     */
    public String getIdentifier(String ipAddress, Long userId) {
        if (userId != null) {
            return "user:" + userId;
        }
        return "ip:" + (ipAddress != null ? ipAddress : "unknown");
    }
}
