package com.mjpascual.loglens.repository;

import com.mjpascual.loglens.model.ApiKey;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public class ApiKeyRepository {

    private final JdbcTemplate jdbc;

    public ApiKeyRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public boolean exists(String keyHash) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM api_keys WHERE key_hash = ?", Integer.class, keyHash);
        return count != null && count > 0;
    }

    /**
     * Counts one request against the key in a single statement, so concurrent calls cannot
     * overrun the quota. The counter restarts on the first request of a new month.
     *
     * @return false if the key does not exist or its monthly limit is used up
     */
    public boolean tryConsume(String keyHash, LocalDate today, int freeLimit) {
        String monthStart = today.withDayOfMonth(1).toString();

        return jdbc.update("""
                        UPDATE api_keys
                        SET requests = CASE WHEN last_reset < ? THEN 1 ELSE requests + 1 END,
                            last_reset = ?
                        WHERE key_hash = ? AND (plan <> 'FREE' OR last_reset < ? OR requests < ?)
                        """,
                monthStart, today.toString(), keyHash, monthStart, freeLimit) == 1;
    }

    public void save(String keyHash, ApiKey.Plan plan, LocalDate today) {
        jdbc.update("INSERT INTO api_keys (key_hash, plan, requests, last_reset) VALUES (?, ?, 0, ?)",
                keyHash, plan.name(), today.toString());
    }
}
