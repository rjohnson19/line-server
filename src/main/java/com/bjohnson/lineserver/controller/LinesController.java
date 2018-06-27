package com.bjohnson.lineserver.controller;

import com.bjohnson.lineserver.service.LinesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

/**
 * Rest Controller that provides the /lines/{index} HTTP GET endpoint.
 */
@RestController
public class LinesController {

    private LinesService linesService;

    /**
     * Constructor for LinesController.
     *
     * @param linesService Implementation of LinesService to use. Normally injected by Spring.
     */
    @Autowired
    public LinesController(
            @Qualifier("RandomAccessLinesService") final LinesService linesService) {
        this.linesService = linesService;
    }

    @RequestMapping(value = "/lines/{index}", produces = MediaType.TEXT_PLAIN_VALUE)
    public String getLine(@PathVariable("index") final Integer index,
                          final HttpServletResponse httpServletResponse) {
        final Optional<String> optLine = linesService.getLineAtIndex(index);
        if (optLine.isPresent()) {
            return optLine.get();
        } else {
            // Per specification, if the index is beyond the end of file, we return an HTTP 413 status
            httpServletResponse.setStatus(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
            return "";
        }
    }
}
