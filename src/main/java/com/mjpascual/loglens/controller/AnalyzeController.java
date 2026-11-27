package com.mjpascual.loglens.controller;

import com.mjpascual.loglens.model.AnalyzeResponse;
import com.mjpascual.loglens.service.AnalysisService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * REST controller for log analysis.
 * Exposes endpoints to receive logs in plain text format and return
 * an analysis of found errors.
 */
@RestController
@RequestMapping("/api/")
@SecurityRequirement(name = "apiKey")
public class AnalyzeController {

    private static final int MAX_LOG_BYTES = 1024 * 1024;

    private final AnalysisService analysisService;

    public AnalyzeController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    /**
     * Endpoint to analyze logs in plain text format.
     * <p>
     * Receives a complete log and returns an analysis with:
     * - List of unique errors found
     * - Number of occurrences for each error
     * - Lines where each error appears
     * - Summary with totals
     *
     * @param body        Log content in plain text, UTF-8 unless the request says otherwise
     * @param contentType Content type of the request, used for its charset
     * @return the analysis; 400 if the log is empty, 413 if it is larger than 1 MB
     */
    @PostMapping(value = "/analyze", consumes = MediaType.TEXT_PLAIN_VALUE)
    @RequestBody(required = true, content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE,
            schema = @Schema(type = "string")))
    public AnalyzeResponse analyze(InputStream body,
                                   @Parameter(hidden = true) @RequestHeader("Content-Type") MediaType contentType)
            throws IOException {
        // Reading one byte over the limit detects oversized bodies, even without Content-Length
        byte[] bytes = body.readNBytes(MAX_LOG_BYTES + 1);
        if (bytes.length > MAX_LOG_BYTES) {
            throw new ResponseStatusException(HttpStatus.CONTENT_TOO_LARGE, "Log larger than 1 MB");
        }

        Charset charset = contentType.getCharset() != null ? contentType.getCharset() : StandardCharsets.UTF_8;
        String log = new String(bytes, charset);
        if (log.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Empty log");
        }

        return analysisService.analyze(log);
    }
}
