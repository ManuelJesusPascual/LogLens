package com.mjpascual.loglens.service;

import com.mjpascual.loglens.exception.TooManyRequestsException;
import com.mjpascual.loglens.exception.UnauthorizedException;
import com.mjpascual.loglens.repository.ApiKeyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class ApiKeyService {

    public static final int MONTHLY_LIMIT = 500;

    private static final Logger log = LoggerFactory.getLogger(ApiKeyService.class);

    private final ApiKeyRepository repository;

    public ApiKeyService(ApiKeyRepository repository) {
        this.repository = repository;
    }

    public void validateAndConsume(String rawKey) {
        String keyHash = hash(rawKey);
        if (repository.tryConsume(keyHash, LocalDate.now(), MONTHLY_LIMIT)) {
            return;
        }
        if (repository.exists(keyHash)) {
            throw new TooManyRequestsException("Monthly limit exceeded");
        }
        throw new UnauthorizedException("Invalid API Key");
    }

    public String generateApiKey() {
        String apiKey = UUID.randomUUID().toString();
        repository.save(hash(apiKey), LocalDate.now());
        log.info("Created an API key");
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
