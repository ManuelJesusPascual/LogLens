package com.mjpascual.loglens.service;

import com.mjpascual.loglens.exception.TooManyRequestsException;
import com.mjpascual.loglens.exception.UnauthorizedException;
import com.mjpascual.loglens.model.ApiKey;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:sqlite:target/quota-test.db",
        "loglens.admin.password=test-password"
})
class ApiKeyServiceTest {

    @Autowired
    private ApiKeyService service;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void concurrentRequestsCannotOverrunTheMonthlyLimit() throws Exception {
        String key = service.generateApiKey(ApiKey.Plan.FREE);
        jdbc.update("UPDATE api_keys SET requests = ? WHERE key = ?", ApiKeyService.FREE_LIMIT - 1, key);

        Callable<Boolean> call = () -> {
            try {
                service.validateAndConsume(key);
                return true;
            } catch (TooManyRequestsException e) {
                return false;
            }
        };
        try (var pool = Executors.newFixedThreadPool(50)) {
            long accepted = 0;
            for (var result : pool.invokeAll(IntStream.range(0, 200).mapToObj(i -> call).toList())) {
                if (result.get()) accepted++;
            }
            assertEquals(1, accepted);
        }
        assertEquals(List.of(ApiKeyService.FREE_LIMIT), requestsOf(key));
    }

    @Test
    void counterRestartsInANewMonth() {
        String key = service.generateApiKey(ApiKey.Plan.FREE);
        jdbc.update("UPDATE api_keys SET requests = ?, last_reset = ? WHERE key = ?",
                ApiKeyService.FREE_LIMIT, LocalDate.now().minusMonths(1).toString(), key);

        assertDoesNotThrow(() -> service.validateAndConsume(key));
        assertEquals(List.of(1), requestsOf(key));
    }

    @Test
    void unknownKeyIsRejected() {
        assertThrows(UnauthorizedException.class, () -> service.validateAndConsume("nope"));
    }

    private List<Integer> requestsOf(String key) {
        return jdbc.queryForList("SELECT requests FROM api_keys WHERE key = ?", Integer.class, key);
    }
}
