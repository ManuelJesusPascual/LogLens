package com.mjpascual.loglens.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class AnalyzeResponse {
    private List<ErrorInfo> errors;
    private Summary summary;

    @Data
    @AllArgsConstructor
    public static class Summary {
        private int totalLines;
        private int errorCount;
    }
}
