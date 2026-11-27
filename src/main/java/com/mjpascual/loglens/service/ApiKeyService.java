package com.mjpascual.loglens.service;

import com.mjpascual.loglens.exception.TooManyRequestsException;
import com.mjpascual.loglens.exception.UnauthorizedException;
import com.mjpascual.loglens.model.ApiKey;
import com.mjpascual.loglens.repository.ApiKeyRepository;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class ApiKeyService {

    public static final int FREE_LIMIT = 500;

    private final ApiKeyRepository repository;

    public ApiKeyService(ApiKeyRepository repository) {
        this.repository = repository;
    }

    public void validateAndConsume(String rawKey) {
        String keyHash = hash(rawKey);
        if (repository.tryConsume(keyHash, LocalDate.now(), FREE_LIMIT)) {
            return;
        }
        if (repository.exists(keyHash)) {
            throw new TooManyRequestsException("Monthly limit exceeded");
        }
        throw new UnauthorizedException("Invalid API Key");
    }

    public String generateApiKey(ApiKey.Plan plan) {
        String apiKey = UUID.randomUUID().toString();
        repository.save(hash(apiKey), plan, LocalDate.now());
        return apiKey;
    }

    /** Only the hash is stored. A fast hash is enough because keys are random, not user-chosen. */
    static String hash(String key) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(key.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
