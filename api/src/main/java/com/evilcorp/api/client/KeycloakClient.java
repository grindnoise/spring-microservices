package com.evilcorp.api.client;

import com.evilcorp.api.config.KeycloakProperties;
import com.evilcorp.api.dto.KeycloakCredentialsRepresentation;
import com.evilcorp.api.dto.KeycloakUserRepresentation;
import com.evilcorp.api.exception.ApiException;
import com.evilcorp.api.util.UserIdExtractor;
import com.evilcorp.keycloak.dto.TokenRefreshRequest;
import com.evilcorp.keycloak.dto.TokenResponse;
import com.evilcorp.keycloak.dto.UserLoginRequest;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.ClientResponse;
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
        this.userRegistrationUrl = properties.serverUrl() + "/admin/realms/" + properties.realm() + "/users";
        this.userByIdUrl = userRegistrationUrl + "/{id}";
        this.userPasswordResetUrl = userByIdUrl + "/reset-password";
    }

    /**
     * Так как Keycloak требует по контракту
     * APPLICATION_FORM_URLENCODED, мы формируем
     * тело запроса через LinkedMultiValueMap.
     */
    @WithSpan("keycloakClient.login")
    public Mono<TokenResponse> login(UserLoginRequest userLoginRequest) {
        var form = new LinkedMultiValueMap<String, String>();
        form.add("grant_type", "password");
        form.add("client_id", properties.clientId());
        form.add("username", userLoginRequest.getEmail());
        form.add("password", userLoginRequest.getPassword());
        addIfNotBlank(form, "client_secret", properties.clientSecret());

        return webClient.post()
                .uri(properties.tokenUrl())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(form)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::toApiException)
                .bodyToMono(TokenResponse.class);
    }

    @WithSpan("keycloakClient.register")
    public Mono<String> register(TokenResponse adminToken, KeycloakUserRepresentation userRepresentation) {
        return webClient.post()
                .uri(userRegistrationUrl)
                .header(HttpHeaders.AUTHORIZATION, BEARER + adminToken.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(userRepresentation)
                .exchangeToMono(this::extractUserId);
    }

    @WithSpan("keycloakClient.adminLogin")
    public Mono<TokenResponse> adminLogin() {
        var form = new LinkedMultiValueMap<String, String>();
        form.add("grant_type", "password");
        form.add("client_id", properties.clientId());
        form.add("username", properties.adminUsername());
        form.add("password", properties.adminPassword());
        addIfNotBlank(form, "client_secret", properties.clientSecret());

        return webClient.post()
                .uri(properties.tokenUrl())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(form)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::toApiException)
                .bodyToMono(TokenResponse.class);
    }

    @WithSpan("keycloakClient.resetUserPassword")
    public Mono<Void> resetUserPassword(String userId, KeycloakCredentialsRepresentation dto, String adminToken) {
        return webClient.put()
                .uri(userPasswordResetUrl, userId)
                .header(HttpHeaders.AUTHORIZATION, BEARER + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp ->
                        resp.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .flatMap(s -> Mono.error(new ApiException("User reset failed: " + s))))
                .toBodilessEntity()
                .then();
    }

    @WithSpan("keycloakClient.resetUserPassword.executeOnError")
    public Mono<ResponseEntity<Void>> executeOnError(String userId, Object dto, String adminToken, Throwable e) {
        return webClient.delete()
                .uri(userByIdUrl, userId)
                .header(HttpHeaders.AUTHORIZATION, BEARER + adminToken)
                .retrieve()
                .toBodilessEntity()
                .then(Mono.error(e));
    }

    @WithSpan("keycloakClient.refreshToken")
    public Mono<TokenResponse> refreshToken(TokenRefreshRequest tokenRefreshRequest) {
        var form = new LinkedMultiValueMap<String, String>();
        form.add("grant_type", "refresh_token");
        form.add("client_id", properties.clientId());
        form.add("refresh_token", tokenRefreshRequest.getRefreshToken());
        addIfNotBlank(form, "client_secret", properties.clientSecret());

        return webClient.post()
                .uri(properties.tokenUrl())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(form)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::toApiException)
                .bodyToMono(TokenResponse.class);
    }

    private static void addIfNotBlank(LinkedMultiValueMap<String, String> map, String key, String value) {
        if (StringUtils.hasLength(value)) {
            map.add(key, value);
        }
    }

    private Mono<? extends Throwable> toApiException(ClientResponse clientResponse) {
        return clientResponse.bodyToMono(String.class)
                .defaultIfEmpty(clientResponse.statusCode().toString())
                .map(body -> new ApiException("Keycloak error " + clientResponse.statusCode() + ": " + body));
    }

    private Mono<String> extractUserId(ClientResponse clientResponse) {
        if (clientResponse.statusCode().equals(HttpStatus.CREATED)) {
            final var location = clientResponse.headers().asHttpHeaders().getLocation();
            if (location == null)
                return Mono.error(new ApiException("Location header not found"));
            return Mono.just(UserIdExtractor.extractUserId(location.getPath()));
        }
        return clientResponse.bodyToMono(String.class)
                .flatMap(body -> Mono.error(new ApiException("Error retrieving user id: " + body)));
    }
}
