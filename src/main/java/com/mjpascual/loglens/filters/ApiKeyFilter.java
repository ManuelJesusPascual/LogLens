package com.mjpascual.loglens.filters;

import com.mjpascual.loglens.exception.UnauthorizedException;
import com.mjpascual.loglens.service.ApiKeyService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Component
public class ApiKeyFilter extends OncePerRequestFilter {

    private final ApiKeyService apiKeyService;
    private final HandlerExceptionResolver exceptionResolver;

    public ApiKeyFilter(ApiKeyService apiKeyService,
                        @Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver) {
        this.apiKeyService = apiKeyService;
        this.exceptionResolver = exceptionResolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String auth = request.getHeader(HttpHeaders.AUTHORIZATION);

        try {
            if (auth == null || !auth.startsWith("Bearer ")) {
                throw new UnauthorizedException("Missing API Key");
            }
            apiKeyService.validateAndConsume(auth.substring(7));
        } catch (RuntimeException ex) {
            // Same error format as the controllers (GlobalExceptionHandler)
            exceptionResolver.resolveException(request, response, null, ex);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
