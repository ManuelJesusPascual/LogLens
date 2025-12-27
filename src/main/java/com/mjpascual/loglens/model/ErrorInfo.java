package com.mjpascual.loglens.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ErrorInfo {
    private final String message;
    private int count = 0;
    private final List<Integer> lines = new ArrayList<>();

    public ErrorInfo(String message) {
        this.message = message;
    }

    public void addLine(int line) {
        count++;
        lines.add(line);
    }
}
