package com.mjpascual.loglens.controller;

import com.mjpascual.loglens.service.AnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for log analysis.
 * Exposes endpoints to receive logs in plain text format and return
 * an analysis of found errors.
 */
@RestController
@RequestMapping("/api/")
public class AnalyzeController {


    @Autowired
    private AnalysisService analysisService;

    /**
     * Endpoint to analyze logs in plain text format.
     * <p>
     * Receives a complete log as String and returns an analysis with:
     * - List of unique errors found
     * - Number of occurrences for each error
     * - Lines where each error appears
     * - Summary with totals
     *
     * @param log Log content in plain text format
     * @return ResponseEntity with the analysis in JSON format if the log is valid,
     * or a 400 Bad Request error if the log is empty
     */
    @PostMapping(value = "/analyze", consumes = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<?> validate(@RequestBody String log) {

        if (log.isBlank()) {
            return ResponseEntity.badRequest().body("Empty log");
        }

        return ResponseEntity.ok(analysisService.analyze(log));
    }
}
