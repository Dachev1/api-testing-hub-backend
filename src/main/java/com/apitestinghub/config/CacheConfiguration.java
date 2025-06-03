package com.apitestinghub.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Cache configuration for AI-powered services.
 * Uses Caffeine for high-performance in-memory caching.
 */
@Configuration
@EnableCaching
public class CacheConfiguration {

    /**
     * Configure Caffeine cache manager with optimal settings for AI responses.
     * AI responses can be large and expensive to generate, so we cache them aggressively.
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        // Enable async cache mode for reactive support
        cacheManager.setAsyncCacheMode(true);

        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(Duration.ofHours(6))
                .expireAfterAccess(Duration.ofHours(2))
                .recordStats()
                .weakKeys());

        cacheManager.setCacheNames(java.util.Arrays.asList(
                "ai-documentation",    // Full API documentation generation
                "ai-descriptions",     // API endpoint descriptions
                "ai-analysis"          // API response analysis
        ));

        return cacheManager;
    }
}