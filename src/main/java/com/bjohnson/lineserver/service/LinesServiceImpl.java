package com.bjohnson.lineserver.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class LinesServiceImpl implements LinesService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LinesServiceImpl.class);

    private String filePath;

    /**
     * Constructor for LinesServiceImpl
     * @param filePath Path to file that we should serve. Normally injected by Spring via the
     *                 Value annotation.
     */
    @Autowired
    public LinesServiceImpl(@Value("${lineserver.file.path}") final String filePath) {
        this.filePath = filePath;
    }

    @Override
    public Optional<String> getLineAtIndex(int index) {
        LOGGER.debug("Retrieving index {}", index);
        if (index < 0) {
            return Optional.empty();
        }
        // Lines we are to support should be valid ASCII (e.g. not Unicode).
        // However, a test file I found was encoded in extended-ASCII (ISO-8859) so I use that so
        // we can support both.
        try (Stream<String> lines = Files.lines(Paths.get(filePath), StandardCharsets.ISO_8859_1)) {

            return lines
                    .skip(index) // index is 0 based, so we skip to the line number we are after
                    .findFirst(); // grab the line after the ones we skipped, if there is one.

        } catch (IOException e) {
            LOGGER.error("Failed reading file: " + e.getMessage(), e);
            return Optional.empty();
        }
    }
}
