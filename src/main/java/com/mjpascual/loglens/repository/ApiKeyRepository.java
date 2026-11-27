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

    public boolean exists(String key) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM api_keys WHERE key = ?", Integer.class, key);
        return count != null && count > 0;
    }

    /**
     * Counts one request against the key in a single statement, so concurrent calls cannot
     * overrun the quota. The counter restarts on the first request of a new month.
     *
     * @return false if the key does not exist or its monthly limit is used up
     */
    public boolean tryConsume(String key, LocalDate today, int freeLimit) {
        String monthStart = today.withDayOfMonth(1).toString();

        return jdbc.update("""
                        UPDATE api_keys
                        SET requests = CASE WHEN last_reset < ? THEN 1 ELSE requests + 1 END,
                            last_reset = ?
                        WHERE key = ? AND (plan <> 'FREE' OR last_reset < ? OR requests < ?)
                        """,
                monthStart, today.toString(), key, monthStart, freeLimit) == 1;
    }

    public void save(String key, ApiKey.Plan plan, LocalDate today) {
        jdbc.update("INSERT INTO api_keys (key, plan, requests, last_reset) VALUES (?, ?, 0, ?)",
                key, plan.name(), today.toString());
    }
}
