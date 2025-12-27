package com.mjpascual.loglens.service;

import com.mjpascual.loglens.model.AnalyzeResponse;
import com.mjpascual.loglens.model.ErrorInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnalysisServiceTest {

    private AnalysisService analysisService;

    @BeforeEach
    void setUp() {
        analysisService = new AnalysisService();
    }

    @Test
    void analyze_basicLog_returnsCorrectErrors() {
        String log = """
                2025-12-27 08:00:01 INFO Starting application
                2025-12-27 08:00:02 ERROR NullPointerException at UserService.java:45
                2025-12-27 08:00:03 WARN Retry attempt
                2025-12-27 08:00:04 ERROR Connection timeout
                2025-12-27 08:00:05 INFO Initialization complete
                2025-12-27 08:00:06 ERROR NullPointerException at UserService.java:45
                2025-12-27 08:00:07 ERROR IllegalArgumentException at OrderService.java:78
                2025-12-27 08:00:08 INFO Processing request
                2025-12-27 08:00:09 ERROR Connection timeout
                """;

        AnalyzeResponse response = analysisService.analyze(log);

        assertEquals(9, response.getSummary().getTotalLines());
        assertEquals(3, response.getSummary().getErrorCount());

        List<ErrorInfo> errors = response.getErrors();
        assertEquals(3, errors.size());

        ErrorInfo npe = errors.stream()
                .filter(e -> e.getMessage().contains("NullPointerException"))
                .findFirst().orElseThrow();
        assertEquals(2, npe.getCount());
        assertEquals(List.of(2, 6), npe.getLines());

        ErrorInfo timeout = errors.stream()
                .filter(e -> e.getMessage().contains("Connection timeout"))
                .findFirst().orElseThrow();
        assertEquals(2, timeout.getCount());
        assertEquals(List.of(4, 9), timeout.getLines());

        ErrorInfo iae = errors.stream()
                .filter(e -> e.getMessage().contains("IllegalArgumentException"))
                .findFirst().orElseThrow();
        assertEquals(1, iae.getCount());
        assertEquals(List.of(7), iae.getLines());
    }

    @Test
    void analyze_noErrors_returnsEmptyList() {
        String log = """
                2025-12-27 08:20:00 INFO Startup complete
                2025-12-27 08:20:01 INFO Listening on port 8080
                2025-12-27 08:20:02 DEBUG Health check passed
                """;

        AnalyzeResponse response = analysisService.analyze(log);

        assertEquals(3, response.getSummary().getTotalLines());
        assertEquals(0, response.getSummary().getErrorCount());
        assertTrue(response.getErrors().isEmpty());
    }

    @Test
    void analyze_exceptionAndCausedBy_returnsCorrectErrors() {
        String log = """
                2025-12-27 08:10:00 INFO Job started
                2025-12-27 08:10:01 ERROR IOException at FileService.java:32
                2025-12-27 08:10:02 ERROR SQLException: deadlock detected
                2025-12-27 08:10:03 Caused by: org.postgresql.util.PSQLException
                2025-12-27 08:10:04 INFO Job ended
                """;

        AnalyzeResponse response = analysisService.analyze(log);

        assertEquals(5, response.getSummary().getTotalLines());
        assertEquals(3, response.getSummary().getErrorCount());

        List<ErrorInfo> errors = response.getErrors();
        assertEquals(3, errors.size());

        assertTrue(errors.stream().anyMatch(e -> e.getMessage().contains("IOException")));
        assertTrue(errors.stream().anyMatch(e -> e.getMessage().contains("SQLException")));
        assertTrue(errors.stream().anyMatch(e -> e.getMessage().contains("Caused by")));
    }
}
