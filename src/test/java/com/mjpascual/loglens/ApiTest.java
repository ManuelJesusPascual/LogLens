package com.mjpascual.loglens;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.url=jdbc:sqlite:target/api-test.db",
        "loglens.admin.password=test-password"
})
class ApiTest {

    @LocalServerPort
    private int port;

    private final HttpClient client = HttpClient.newHttpClient();

    @Test
    void adminCreatesAKeyThatCanAnalyzeLogs() throws Exception {
        HttpResponse<String> created = generateKey("test-password");
        assertEquals(200, created.statusCode());

        assertEquals(200, analyze(created.body(), "ERROR boom").statusCode());
    }

    @Test
    void adminRejectsAWrongPassword() throws Exception {
        assertEquals(403, generateKey("wrong").statusCode());
    }

    @Test
    void apiRequiresAValidKey() throws Exception {
        assertEquals(401, send(request("/api/analyze")
                .POST(HttpRequest.BodyPublishers.ofString("ERROR boom"))).statusCode());
        assertEquals(401, analyze("unknown", "ERROR boom").statusCode());
    }

    @Test
    void apiDocsArePublic() throws Exception {
        assertEquals(200, send(request("/v3/api-docs")).statusCode());
        assertEquals(200, send(request("/swagger-ui/index.html")).statusCode());
    }

    @Test
    void errorsUseTheRightStatusWithoutLeakingInternals() throws Exception {
        String key = generateKey("test-password").body();

        assertEquals(400, analyze(key, "   ").statusCode());
        assertEquals(400, analyze(key, "").statusCode());
        assertEquals(413, analyze(key, "x".repeat(1024 * 1024 + 1)).statusCode());
        assertEquals(415, send(request("/api/analyze")
                .header("Authorization", "Bearer " + key)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{}"))).statusCode());
        assertEquals(404, send(request("/nope")).statusCode());
        assertEquals(405, send(request("/admin/generate-key")).statusCode());
    }

    @Test
    void disguisedApiPathsStillNeedAKey() throws Exception {
        for (String path : new String[]{"/%61pi/analyze", "//api/analyze", "/api;x/analyze", "/admin/../api/analyze"}) {
            HttpResponse<String> response = send(request(path)
                    .header("Content-Type", "text/plain")
                    .POST(HttpRequest.BodyPublishers.ofString("ERROR boom")));
            assertNotEquals(200, response.statusCode(), path);
        }
    }

    private HttpResponse<String> generateKey(String password) throws IOException, InterruptedException {
        return send(request("/admin/generate-key")
                .header("X-Admin-Password", password)
                .POST(HttpRequest.BodyPublishers.noBody()));
    }

    private HttpResponse<String> analyze(String key, String log) throws IOException, InterruptedException {
        return send(request("/api/analyze")
                .header("Authorization", "Bearer " + key)
                .header("Content-Type", "text/plain")
                .POST(HttpRequest.BodyPublishers.ofString(log)));
    }

    private HttpRequest.Builder request(String path) {
        return HttpRequest.newBuilder(URI.create("http://localhost:" + port + path));
    }

    private HttpResponse<String> send(HttpRequest.Builder request) throws IOException, InterruptedException {
        return client.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }
}
