package com.bjohnson.lineserver.service;

import com.bjohnson.lineserver.configuration.CacheConfiguration;
import org.springframework.cache.annotation.Cacheable;

import java.util.Optional;

/**
 * Service that encapsulates functionality for reading lines from the configured file.
 */
public interface LinesService {
    /**
     * Retrieve the line from the file at the specified index.
     *
     * @param index the index of the line to return from the file.
     * @return An optional String value, which will not be present if the file we are serving does
     * not contain a line at the specified index.
     */
    @Cacheable(CacheConfiguration.LINES_CACHE)
    Optional<String> getLineAtIndex(final int index);
}
