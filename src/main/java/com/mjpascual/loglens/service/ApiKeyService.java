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

    private static final int FREE_LIMIT = 500;

    private final ApiKeyRepository repository;

    public ApiKeyService(ApiKeyRepository repository) {
        this.repository = repository;
    }

    public ApiKey validateAndConsume(String rawKey) {
        ApiKey apiKey = repository.findByKey(rawKey)
                .orElseThrow(() -> new UnauthorizedException("Invalid API Key"));

        resetIfNewMonth(apiKey);

        if (apiKey.getPlan() == ApiKey.Plan.FREE &&
                apiKey.getRequests() >= FREE_LIMIT) {
            throw new TooManyRequestsException("Monthly limit exceeded");
        }

        apiKey.setRequests(apiKey.getRequests() + 1);
        repository.update(apiKey);

        return apiKey;
    }

    private void resetIfNewMonth(ApiKey apiKey) {
        LocalDate now = LocalDate.now();

        if (apiKey.getLastReset().getMonth() != now.getMonth()
                || apiKey.getLastReset().getYear() != now.getYear()) {

            apiKey.setRequests(0);
            apiKey.setLastReset(now);
        }
    }

    public String generateApiKey(String plan) {
        String apiKey = UUID.randomUUID().toString();

        ApiKey key = new ApiKey();
        key.setKey(apiKey);
        key.setPlan(ApiKey.Plan.valueOf(plan));
        key.setRequests(0);
        key.setLastReset(LocalDate.now());

        repository.save(key);
        return apiKey;
    }
}
