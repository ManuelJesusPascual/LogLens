package com.mjpascual.loglens.filters;

import com.mjpascual.loglens.exception.TooManyRequestsException;
import com.mjpascual.loglens.exception.UnauthorizedException;
import com.mjpascual.loglens.service.ApiKeyService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class ApiKeyFilter extends OncePerRequestFilter {

    private final ApiKeyService apiKeyService;

    public ApiKeyFilter(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String auth = request.getHeader(HttpHeaders.AUTHORIZATION);

        try {
            if (auth == null || !auth.startsWith("Bearer ")) {
                throw new UnauthorizedException("Missing API Key");
            }

            String key = auth.substring(7);
            apiKeyService.validateAndConsume(key);

            filterChain.doFilter(request, response);

        } catch (UnauthorizedException ex) {
            writeError(response, HttpStatus.UNAUTHORIZED, ex.getMessage());
        } catch (TooManyRequestsException ex) {
            writeError(response, HttpStatus.TOO_MANY_REQUESTS, ex.getMessage());
        } catch (Exception ex) {
            writeError(response, HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error: " + ex.getMessage());
        }
    }

    private void writeError(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");
        String json = "{\"error\":\"" + message + "\"}";
        response.getWriter().write(json);
    }

}
