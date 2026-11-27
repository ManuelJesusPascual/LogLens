package com.mjpascual.loglens;

import com.mjpascual.loglens.filters.ApiKeyFilter;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@SecurityScheme(name = "apiKey", type = SecuritySchemeType.HTTP, scheme = "bearer")
public class LoglensApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoglensApplication.class, args);
    }

    /** Only the API needs a key; the admin and Swagger endpoints stay outside the filter. */
    @Bean
    FilterRegistrationBean<ApiKeyFilter> apiKeyFilterRegistration(ApiKeyFilter filter) {
        FilterRegistrationBean<ApiKeyFilter> registration = new FilterRegistrationBean<>(filter);
        registration.addUrlPatterns("/api/*");
        return registration;
    }
}
