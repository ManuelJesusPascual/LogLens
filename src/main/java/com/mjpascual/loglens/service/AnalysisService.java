package com.mjpascual.loglens.service;

import com.mjpascual.loglens.model.AnalyzeResponse;
import com.mjpascual.loglens.model.ErrorInfo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service responsible for analyzing log files and extracting error information.
 * Processes plain text logs to identify, categorize, and count error occurrences.
 */
@Service
public class AnalysisService {

    /**
     * Analyzes a log string to identify and categorize errors.
     * <p>
     * This method:
     * 1. Splits the log into individual lines
     * 2. Checks each line for error indicators (ERROR, Exception, Caused by)
     * 3. Groups identical errors together with their line numbers
     * 4. Returns a summary with error details and statistics
     *
     * @param log The complete log content as a single string
     * @return AnalyzeResponse containing the list of unique errors and a summary
     */
    public AnalyzeResponse analyze(String log) {
        Map<String, ErrorInfo> errors = new HashMap<>();

        String[] lines = log.split("\\R");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            int lineNumber = i + 1;

            if (checkIfIsError(line)) {
                String message = extractErrorMessage(line);

                errors.computeIfAbsent(message, ErrorInfo::new).addLine(lineNumber);
            }
        }

        List<ErrorInfo> errorList = new ArrayList<>(errors.values());

        AnalyzeResponse.Summary summary = new AnalyzeResponse.Summary(lines.length, errorList.size());
        return new AnalyzeResponse(errorList, summary);


    }

    /**
     * Checks if a log line contains error indicators.
     *
     * @param line The log line to check
     * @return true if the line contains "ERROR", "Exception", or "Caused by"
     */
    private boolean checkIfIsError(String line) {
        return line.contains("ERROR") || line.contains("Exception") || line.contains("Caused by");

    }

    /**
     * Extracts the relevant error message from a log line.
     * <p>
     * The extraction strategy prioritizes error indicators in this order:
     * 1. "Caused by" - typically indicates the root cause
     * 2. "ERROR" - standard error log level
     * 3. "Exception" - Java exception messages
     * <p>
     * The message is extracted from the indicator onwards to capture
     * the complete error description while removing timestamp and other prefixes.
     *
     * @param line The log line containing an error
     * @return The extracted error message, trimmed of leading/trailing whitespace
     */
    private String extractErrorMessage(String line) {

        if (line.contains("Caused by")) {
            return line.substring(line.indexOf("Caused by")).trim();
        } else if (line.contains("ERROR")) {
            return line.substring(line.indexOf("ERROR")).trim();
        } else if (line.contains("Exception")) {
            return line.substring(line.indexOf("Exception")).trim();
        } else {
            return line.trim();
        }
    }
}
