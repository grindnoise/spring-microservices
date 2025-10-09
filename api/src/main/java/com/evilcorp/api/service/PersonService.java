package com.evilcorp.api.service;

import com.evilcorp.api.mapper.PersonMapper;
import com.evilcorp.individual.dto.IndividualWriteDto;
import com.evilcorp.individual.dto.IndividualWriteResponseDto;
import com.evilcorp.person.api.PersonApiClient;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.UUID;

/**
 * Класс PersonService "оборачивает" обращения к person-service через фейн клиент - PersonApiClient
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PersonService {

    private final PersonApiClient personApiClient;
    private final PersonMapper personMapper;

    @WithSpan("personService.register")
    public Mono<IndividualWriteResponseDto> register(IndividualWriteDto request) {
        return Mono.fromCallable(() -> personApiClient.registration(personMapper.from(request)))
                .mapNotNull(HttpEntity::getBody)
                .map(personMapper::from)
                .subscribeOn(Schedulers.boundedElastic())
                .doOnNext(t -> log.info("Successfully registered person with id [{}]", t.getId()));
    }

    @WithSpan("personService.compensateRegistration")
    public Mono<Void> compensateRegistration(String id) {
        return Mono.fromRunnable(() -> personApiClient.compensateRegistration(UUID.fromString(id)))
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }
}
