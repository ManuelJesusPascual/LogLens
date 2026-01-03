package com.mjpascual.loglens.controller;

import com.mjpascual.loglens.service.ApiKeyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final ApiKeyService apiKeyService;
    private final String ADMIN_PASS = "REMOVED";

    public AdminController(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    @GetMapping("/generate-key")
    public ResponseEntity<String> generateKey(@RequestParam String plan, @RequestBody Map<String, String> body) {
        String pass = body.get("pass");
        if (pass == null || !pass.equals(ADMIN_PASS)) {
            return ResponseEntity.status(403).body("Forbidden");
        }

        String key = apiKeyService.generateApiKey(plan);
        return ResponseEntity.ok(key);
    }
}
