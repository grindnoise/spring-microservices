package com.evilcorp.api.service;

import com.evilcorp.api.client.KeycloakClient;
import com.evilcorp.api.mapper.KeycloakMapper;
import com.evilcorp.api.mapper.PersonMapper;
import com.evilcorp.api.mapper.TokenResponseMapper;
import com.evilcorp.individual.dto.TokenRefreshRequest;
import com.evilcorp.individual.dto.TokenResponse;
import com.evilcorp.individual.dto.UserLoginRequest;
import com.evilcorp.person.api.PersonApiClient;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * TokenService "оборачивает" логику работы с токенами:
 * - получение access_token по кредам
 * - получение access_token no refresh_token
 * - получение "админского"
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

    private final KeycloakClient keycloakClient;
    private final KeycloakMapper keycloakMapper;
    private final TokenResponseMapper tokenResponseMapper;

    @WithSpan("tokenService.login")
    public Mono<TokenResponse> login(UserLoginRequest request) {
        return keycloakClient.login(keycloakMapper.toKeycloakUserLoginRequest(request))
                .doOnNext(_ -> log.info("User [{}] successfully logged in", request.getEmail()))
                .doOnError(e -> log.error("Login attempt for user [{}] failed with error: [{}]", request.getEmail(), e.toString()))
                .map(tokenResponseMapper::toTokenResponse);
    }

    @WithSpan("tokenService.refreshToken")
    public Mono<TokenResponse> refreshToken(TokenRefreshRequest request) {
        return keycloakClient.refreshToken(keycloakMapper.toKeycloakTokenRefreshRequest(request))
                .doOnNext(_ -> log.info("Token successfully refreshed"))
                .doOnError(e -> log.error("Failed to refresh token"))
                .map(tokenResponseMapper::toTokenResponse);
    }

    @WithSpan("tokenService.getAdminServiceToken")
    public Mono<TokenResponse> getAdminServiceToken() {
        return keycloakClient.adminLogin()
                .doOnNext(_ -> log.info("Admin token successfully loaded"))
                .doOnError(_ -> log.error("Failed to get admin service token"))
                .map(tokenResponseMapper::toTokenResponse);
    }


}
