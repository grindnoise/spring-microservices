package com.evilcorp.api.client;

import com.evilcorp.api.config.KeycloakProperties;
import com.evilcorp.keycloak.dto.TokenRefreshRequest;
import com.evilcorp.keycloak.dto.TokenResponse;
import com.evilcorp.keycloak.dto.UserLoginRequest;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class KeycloakClient {

    private static final String BEARER = "Bearer ";

    private final WebClient webClient;
    private final KeycloakProperties properties;
    private String userRegistrationUrl;
    private String userPasswordResetUrl;
    private String userByIdUrl;

    @PostConstruct
    public void init() {
        this.userRegistrationUrl = properties.serverUrl() + "/admin/realms" + properties.realm() + "/users";
        this.userByIdUrl = userRegistrationUrl + "/{id}";
        this.userPasswordResetUrl = userByIdUrl + "/reset-password";
    }

    public Mono<TokenResponse> login(UserLoginRequest userLoginRequest) {
        return Mono.just(null);
    }

//    public Mono<TokenResponse> register(String adminToken, KeycloakU) {
//        return Mono.just(null);
//    }

    public Mono<TokenResponse> adminLogin(UserLoginRequest userLoginRequest) {
        return Mono.just(null);
    }

    public Mono<TokenResponse> resetUserPassword(String userId, Object dto, String adminToken) {
        return Mono.just(null);
    }

    public Mono<ResponseEntity<Void>> executeOnError(String userId, Object dto, String adminToken, Throwable e) {
        return webClient.delete()
                .uri(userByIdUrl, userId)
                .header(HttpHeaders.AUTHORIZATION, BEARER + adminToken)
                .retrieve()
                .toBodilessEntity()
                .then(Mono.error(e));
    }

    public Mono<TokenResponse> refreshToken(TokenRefreshRequest tokenRefreshRequest) {
        return Mono.just(null);
    }

    private static void addIfNotBlank(LinkedMultiValueMap<String, String> map, String key, String value) {




    }
}
