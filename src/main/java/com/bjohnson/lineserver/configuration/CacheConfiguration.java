package com.bjohnson.lineserver.configuration;

import com.google.common.cache.CacheBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.guava.GuavaCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

/**
 * Configuration class that sets up the Spring Cache for caching lines previously read.
 */
@Configuration
public class CacheConfiguration {
    public static final String LINES_CACHE = "linesCache";
    private int cacheMaxSize;

    /**
     * Constructor for this configuration class.
     *
     * @param cacheMaxSize The maximum amount of items we should hold in the cache of lines.
     *                     Normally injected by Spring via the Value annotation.
     */
    @Autowired
    public CacheConfiguration(@Value("${lineserver.cache.lines.size}") int cacheMaxSize) {
        this.cacheMaxSize = cacheMaxSize;
    }

    @Bean
    public CacheManager cacheManager() {
        GuavaCacheManager cacheManager = new GuavaCacheManager();
        cacheManager.setCacheBuilder(CacheBuilder.newBuilder().maximumSize(cacheMaxSize));
        cacheManager.setCacheNames(Collections.singletonList(LINES_CACHE));

        return cacheManager;

    }
}
