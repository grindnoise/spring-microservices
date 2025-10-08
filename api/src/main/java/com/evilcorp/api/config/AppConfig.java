package com.evilcorp.api.config;

import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * В AppConfig мы конфигурируем бины для:
 * - RestTemplate (будем использовать в тестах)
 * - WebClient (для обращений к Keycloak)
 * - HttpMessageConverters - для корректной работы клиентов
 */
@Configuration
public class AppConfig {

    @Bean
    public WebClient keycloakWebClient(KeycloakProperties properties) {
        return WebClient.builder()
                .baseUrl(properties.serverUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .build();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public HttpMessageConverters httpMessageConverters() {
        return new HttpMessageConverters();
    }
}
