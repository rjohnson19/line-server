package com.bjohnson.lineserver.controller;

import com.bjohnson.lineserver.service.LinesService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class LinesControllerTest {

    private static final String LINE_VALUE = "Hello";
    private LinesService mockLinesService;

    @Autowired
    private LinesService linesService;

    @Autowired
    private LinesController linesController;

    @Autowired
    private MockMvc mockMvc;

    @Before
    public void setUp() {
        mockLinesService = mock(LinesService.class);
        when(mockLinesService.getLineAtIndex(0)).thenReturn(Optional.of(LINE_VALUE));
        when(mockLinesService.getLineAtIndex(1)).thenReturn(Optional.empty());

        ReflectionTestUtils.setField(linesController, "linesService", mockLinesService);
    }

    @After
    public void tearDown() {
        ReflectionTestUtils.setField(linesController, "linesService", linesService);
    }

    @Test
    public void testGetLine() throws Exception {
        mockMvc.perform(get("/lines/0"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(LINE_VALUE)));
    }

    @Test
    public void testGetLineAfterEndOfFileReturnsHttp413() throws Exception {
        mockMvc.perform(get("/lines/1"))
                .andExpect(status().is(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE));
    }
}
