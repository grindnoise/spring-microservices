package com.evilcorp.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange -> exchange
                        // Public
                        .pathMatchers(
                                "/actuator/health",
                                "/actuator/prometheus",
                                "/actuator/info",
                                "/v1/auth/login",
                                "/v1/auth/registration",
                                "/v1/auth/refresh-token",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                // /v3/api-docs is the endpoint for SpringDoc OpenAPI documentation, which automatically generates API documentation for your Spring Boot application.
                                "/v3/api-docs/**"
                        ).permitAll()
                        // USER
                        .pathMatchers("/v1/auth/me").hasRole("individual.user")
                        .anyExchange().authenticated())
                .oauth2ResourceServer(configurer ->
                        configurer.jwt(jwtConfigurer -> jwtConfigurer.jwtAuthenticationConverter(keycloakAuthenticationConverter())))

                .build();
    }

    private Converter<Jwt, ? extends Mono<AbstractAuthenticationToken>> keycloakAuthenticationConverter() {
        ReactiveJwtAuthenticationConverter converter = new ReactiveJwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakJwtConverter());
        return converter;
    }
}
