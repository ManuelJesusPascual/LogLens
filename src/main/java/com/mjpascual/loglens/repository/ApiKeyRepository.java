package com.mjpascual.loglens.repository;

import com.mjpascual.loglens.model.ApiKey;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Optional;

@Repository
public class ApiKeyRepository {

    private final JdbcTemplate jdbc;

    public ApiKeyRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<ApiKey> findByKey(String key) {
        return jdbc.query(
                "SELECT key, plan, requests, last_reset FROM api_keys WHERE key = ?",
                rs -> {
                    if (!rs.next()) return Optional.empty();

                    ApiKey apiKey = new ApiKey();
                    apiKey.setKey(rs.getString("key"));
                    apiKey.setPlan(ApiKey.Plan.valueOf(rs.getString("plan")));
                    apiKey.setRequests(rs.getInt("requests"));
                    apiKey.setLastReset(LocalDate.parse(rs.getString("last_reset")));

                    return Optional.of(apiKey);
                },
                key
        );
    }

    public void update(ApiKey apiKey) {
        jdbc.update(
                "UPDATE api_keys SET requests = ?, last_reset = ? WHERE key = ?",
                apiKey.getRequests(),
                apiKey.getLastReset().toString(),
                apiKey.getKey()
        );
    }

    public void save(ApiKey apiKey) {
        String sql = "INSERT INTO api_keys (requests, plan, last_reset, key) VALUES (?, ?, ?, ?)";

        jdbc.update(
                sql,
                apiKey.getRequests(),
                apiKey.getPlan(),
                apiKey.getLastReset().toString(),
                apiKey.getKey()
        );
    }
}
