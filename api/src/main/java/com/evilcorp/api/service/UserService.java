package com.evilcorp.api.service;

import com.evilcorp.api.client.KeycloakClient;
import com.evilcorp.api.dto.KeycloakCredentialsRepresentation;
import com.evilcorp.api.dto.KeycloakUserRepresentation;
import com.evilcorp.api.exception.ApiException;
import com.evilcorp.api.mapper.TokenResponseMapper;
import com.evilcorp.individual.dto.IndividualWriteDto;
import com.evilcorp.individual.dto.TokenResponse;
import com.evilcorp.individual.dto.UserInfoResponse;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
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

//    @WithSpan("userService.register")
//    public Mono<TokenResponse> register(IndividualWriteDto dto) {
//        return personService.register(dto)
//                .flatMap(responseDto -> {
//                    keycloakClient.adminLogin()
//                            .flatMap(tokenResponse -> {
//                                final var keycloakUserRepresentation = new KeycloakUserRepresentation(
//                                        null,
//                                        dto.getEmail(),
//                                        dto.getEmail(),
//                                        true,
//                                        true,
//                                        null
//                                );
//
//                                return keycloakClient.register(tokenResponse.getAccessToken(), keycloakUserRepresentation)
//                                        .flatMap(tokenResponse -> {
//                                            tokenResponse
//                                        });
//                            })
//                })
//    }
}
