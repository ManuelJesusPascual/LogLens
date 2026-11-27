package com.mjpascual.loglens.service;

import com.mjpascual.loglens.exception.TooManyRequestsException;
import com.mjpascual.loglens.exception.UnauthorizedException;
import com.mjpascual.loglens.model.ApiKey;
import com.mjpascual.loglens.repository.ApiKeyRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class ApiKeyService {

    public static final int FREE_LIMIT = 500;

    private final ApiKeyRepository repository;

    public ApiKeyService(ApiKeyRepository repository) {
        this.repository = repository;
    }

    public void validateAndConsume(String rawKey) {
        if (repository.tryConsume(rawKey, LocalDate.now(), FREE_LIMIT)) {
            return;
        }
        if (repository.exists(rawKey)) {
            throw new TooManyRequestsException("Monthly limit exceeded");
        }
        throw new UnauthorizedException("Invalid API Key");
    }

    public String generateApiKey(ApiKey.Plan plan) {
        String apiKey = UUID.randomUUID().toString();
        repository.save(apiKey, plan, LocalDate.now());
        return apiKey;
    }
}
