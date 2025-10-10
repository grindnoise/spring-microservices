package com.evilcorp.api.service;

import com.evilcorp.api.client.KeycloakClient;
import com.evilcorp.api.dto.KeycloakCredentialsRepresentation;
import com.evilcorp.api.dto.KeycloakUserRepresentation;
import com.evilcorp.api.exception.ApiException;
import com.evilcorp.api.mapper.TokenResponseMapper;
import com.evilcorp.individual.dto.IndividualWriteDto;
import com.evilcorp.individual.dto.TokenResponse;
import com.evilcorp.individual.dto.UserInfoResponse;
import com.evilcorp.keycloak.dto.UserLoginRequest;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.ZoneOffset;

/**
 * UserService описывает основную логику приложения:
 * - получение данных по пользователю
 * - регистрация пользователя (SAGA - оркестрация)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final PersonService personService;
    private final KeycloakClient keycloakClient;
    private final TokenResponseMapper tokenResponseMapper;

    @WithSpan("userService.getUserInfo")
    public Mono<UserInfoResponse> getUserInfo() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .flatMap(UserService::getUserInfo)
                .switchIfEmpty(Mono.error(() -> new ApiException("User not found")));
    }

    private static Mono<UserInfoResponse> getUserInfo(Authentication authentication) {
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            final var userResponse = new UserInfoResponse();
            userResponse.setId(jwt.getSubject());
            userResponse.setEmail(jwt.getClaimAsString("email"));
            userResponse.setRoles(jwt.getClaimAsStringList("roles"));

            if (jwt.getIssuedAt() != null)
                userResponse.setCreatedAt(jwt.getIssuedAt().atOffset(ZoneOffset.UTC));

            log.info("User [{}] was successfully identified", userResponse.getEmail());

            return Mono.just(userResponse);
        }

        log.error("Failed to get user info");
        return Mono.error(() -> new ApiException("Unable to retrieve user info"));
    }

    @WithSpan("userService.register")
    public Mono<TokenResponse> register(IndividualWriteDto dto) {
        return personService.register(dto)
                .flatMap(responseDto ->
                    keycloakClient.adminLogin()
                            .flatMap(adminTokenResponse -> {
                                final var keycloakUserRepresentation = new KeycloakUserRepresentation(
                                        null,
                                        dto.getEmail(),
                                        dto.getEmail(),
                                        true,
                                        true,
                                        null
                                );

                                // При первичной регистрации пароль временный, поэтому необходимо его переустановить
                                return keycloakClient.register(adminTokenResponse, keycloakUserRepresentation)
                                        .flatMap(userId -> {
                                            final var credentials = new KeycloakCredentialsRepresentation(
                                                    "password",
                                                    dto.getPassword(),
                                                    false
                                            );

                                            // Переустановка пароля
                                            return keycloakClient.resetUserPassword(userId, credentials, adminTokenResponse.getAccessToken())
                                                    .thenReturn(userId);
                                        })
                                        .flatMap(_ -> keycloakClient.login(new UserLoginRequest(dto.getEmail(), dto.getPassword())))
                                        .onErrorResume(error ->
                                                personService.compensateRegistration(responseDto.getId().toString())
                                                        .then(Mono.error(error)))
                                        .map(tokenResponseMapper::toTokenResponse);
                            })
                );
    }
    }
