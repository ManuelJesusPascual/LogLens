package com.mjpascual.loglens.model;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ApiKey {

    private String key;
    private Plan plan;
    private int requests;
    private LocalDate lastReset;

    public enum Plan {
        FREE,
        PAID
    }
}
