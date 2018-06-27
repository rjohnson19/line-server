package com.bjohnson.lineserver.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service("RandomAccessLinesService")
public class RandomAccessLinesServiceImpl implements LinesService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RandomAccessLinesServiceImpl.class);

    private String filePath;
    private List<Long> lineEndingsList;

    /**
     * Constructor for RandomAccessLinesServiceImpl
     * @param filePath Path to file that we should serve. Normally injected by Spring via the
     *                 Value annotation.
     */
    @Autowired
    public RandomAccessLinesServiceImpl(@Value("${lineserver.file.path}") final String filePath) {
        this.filePath = filePath;
        lineEndingsList = new ArrayList<>();
    }

    @PostConstruct
    public void loadFileLineEndings() {
        LOGGER.debug("Beginning preload of file...");
        // read through the bytes of a file.
        // whenever we encounter a line ending, just \n, mark down the byte position
        // of the line ending in lineEndingsList.
        // this will allow us to use RandomAccessFile to quickly read from the previous lines ending to our lines ending.
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(filePath), StandardCharsets.ISO_8859_1)) {
            long bytePosition = 0;
            int nextChar = reader.read();
            while (nextChar != -1) {
                char thisChar = (char) nextChar;
                if ('\n' == thisChar) {
                    lineEndingsList.add(bytePosition);
                }
                nextChar = reader.read();
                bytePosition++;
            }
        } catch (IOException e) {
            LOGGER.error("Failed to preload file {}", filePath, e);
        }
        LOGGER.info("Preloading of file {} has been completed.", filePath);
    }

    @Override
    public Optional<String> getLineAtIndex(int index) {
        if (index < 0 || index > lineEndingsList.size()) {
            return Optional.empty();
        }

        long startPos = 0;
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(filePath, "r")) {
            if (index > 0) {
                // get the index of the line ending immediately preceding the line we are after.
                startPos = lineEndingsList.get(index - 1);
                // seek to the first char of the next line (our desired line)
                randomAccessFile.seek(startPos + 1);
            }
            // read and return the rest of the line, if there is one.
            String line = randomAccessFile.readLine();
            if (Objects.nonNull(line)) {
                return Optional.of(line);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to read from file: {}", e.getMessage(), e);
        }

        return Optional.empty();
    }
}
