package com.mjpascual.loglens.controller;

import com.mjpascual.loglens.service.ApiKeyService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final ApiKeyService apiKeyService;
    private final String adminPassword;

    public AdminController(ApiKeyService apiKeyService,
                           @Value("${loglens.admin.password}") String adminPassword) {
        if (adminPassword.isBlank()) {
            throw new IllegalStateException("ADMIN_PASSWORD must not be blank");
        }
        this.apiKeyService = apiKeyService;
        this.adminPassword = adminPassword;
    }

    @GetMapping("/generate-key")
    public ResponseEntity<String> generateKey(@RequestParam String plan, @RequestBody Map<String, String> body) {
        String pass = body.get("pass");
        if (pass == null || !pass.equals(adminPassword)) {
            return ResponseEntity.status(403).body("Forbidden");
        }

        String key = apiKeyService.generateApiKey(plan);
        return ResponseEntity.ok(key);
    }
}
