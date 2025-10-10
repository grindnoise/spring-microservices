package com.evilcorp.api.rest;

import com.evilcorp.api.service.TokenService;
import com.evilcorp.api.service.UserService;
import com.evilcorp.individual.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;

/**
 * AuthRestControllerV1 имеет 4 ендпоинта:
 * - получение данных юзера на основе аутентификации
 * - логин
 * - обновление токена (refresh token)
 * - регистрация
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/v1/auth")
public class AuthRestControllerV1 {

    private final UserService userService;
    private final TokenService tokenService;

    @GetMapping("/me")
    public Mono<ResponseEntity<UserInfoResponse>> getMe() {
        return userService.getUserInfo().map(ResponseEntity::ok);
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<TokenResponse>> login(@Valid @RequestBody Mono<UserLoginRequest> request) {
        return request.flatMap(tokenService::login).map(ResponseEntity::ok);
    }

    @PostMapping("/refresh-token")
    public Mono<ResponseEntity<TokenResponse>> refreshToken(@Valid @RequestBody Mono<TokenRefreshRequest> request) {
        return request.flatMap(tokenService::refreshToken).map(ResponseEntity::ok);
    }

    @PostMapping("/registration")
    public Mono<ResponseEntity<?>> register(@Valid @RequestBody Mono<IndividualWriteDto> request) {
        return request.flatMap(userService::register)
                .map(t -> ResponseEntity.status(HttpStatus.CREATED).body(t));
//                .onErrorResume(error -> {
//                    log.error("Error registering user", error);
//                    return Mono.just(ResponseEntity.badRequest()
//                            .body(Map.of(
//                                    "error", "Registration failed",
//                                    "message", error.getMessage(),
//                                    "timestamp", Instant.now()
//                            )));
//                });
    }
}
