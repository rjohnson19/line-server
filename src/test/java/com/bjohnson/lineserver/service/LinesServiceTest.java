package com.bjohnson.lineserver.service;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import static org.junit.Assert.*;

public class LinesServiceTest {
    private static final String TEST_FILE_CLASSPATH = "/test-file.txt";
    private static final String EXPECTED_FIRST_LINE = "This is the first line.";
    private static final String EXPECTED_THIRD_LINE = "Third line that is longer. Third line that is longer. Third line that is longer. Third line that is longer. Third line that is longer. Third line that is longer.";
    private static final String EXPECTED_LAST_LINE = "Last.";

    private LinesService linesService;
    private File tempFile;

    @Before
    public void setUp() throws IOException {
        // we need to copy the test content to a temp file for the service to read.
        try (InputStream testFileInputStream = getClass().getResourceAsStream(TEST_FILE_CLASSPATH)) {
            tempFile = File.createTempFile("lines", ".tmp");
            FileUtils.copyInputStreamToFile(testFileInputStream, tempFile);
        }

        linesService = new RandomAccessLinesServiceImpl(tempFile.getPath());
        ((RandomAccessLinesServiceImpl) linesService).loadFileLineEndings();
    }

    @After
    public void tearDown() {
        // clean up the temp file we created.
        FileUtils.deleteQuietly(tempFile);
    }

    @Test
    public void testReadFirstLine() {
        assertEquals(EXPECTED_FIRST_LINE, getRequiredLine(0));
    }

    @Test
    public void testReadThirdLine() {
        assertEquals(EXPECTED_THIRD_LINE, getRequiredLine(2));
    }

    @Test
    public void testReadLastLine() {
        assertEquals(EXPECTED_LAST_LINE, getRequiredLine(3));
    }

    @Test
    public void testReadIndexAfterEndOfFileReturnsEmpty() {
        ensureLineIsNotPresent(4);
    }

    @Test
    public void testReadNegativeIndexReturnsEmpty() {
        ensureLineIsNotPresent(-1);
    }

    @Test
    public void testReadIntegerMaxValueIndexReturnsEmpty() {
        ensureLineIsNotPresent(Integer.MAX_VALUE);
    }

    @Test
    public void testReadIntegerMinValueIndexReturnsEmpty() {
        ensureLineIsNotPresent(Integer.MIN_VALUE);
    }

    private void ensureLineIsNotPresent(int index) {
        Optional<String> optLine = linesService.getLineAtIndex(index);
        assertFalse("Line should not be present.", optLine.isPresent());
    }

    private String getRequiredLine(int index) {
        Optional<String> optLine = linesService.getLineAtIndex(index);
        assertTrue("Line should be present.", optLine.isPresent());
        return optLine.get();
    }
}
