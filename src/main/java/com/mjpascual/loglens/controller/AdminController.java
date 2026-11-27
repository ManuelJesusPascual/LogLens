package com.mjpascual.loglens.controller;

import com.mjpascual.loglens.model.ApiKey;
import com.mjpascual.loglens.service.ApiKeyService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final ApiKeyService apiKeyService;
    private final byte[] adminPassword;

    public AdminController(ApiKeyService apiKeyService,
                           @Value("${loglens.admin.password}") String adminPassword) {
        if (adminPassword.isBlank()) {
            throw new IllegalStateException("ADMIN_PASSWORD must not be blank");
        }
        this.apiKeyService = apiKeyService;
        this.adminPassword = adminPassword.getBytes(StandardCharsets.UTF_8);
    }

    @PostMapping("/generate-key")
    public ResponseEntity<String> generateKey(@RequestHeader(value = "X-Admin-Password", defaultValue = "") String password,
                              @RequestParam(defaultValue = "FREE") ApiKey.Plan plan) {
        // Constant-time comparison, so response timing does not reveal the password
        if (!MessageDigest.isEqual(adminPassword, password.getBytes(StandardCharsets.UTF_8))) {
            return ResponseEntity.status(403).body("Forbidden");
        }
        return ResponseEntity.ok(apiKeyService.generateApiKey(plan));
    }
}
