package com.evilcorp.person_service.rest;

import com.evilcorp.person.api.PersonApi;
import com.evilcorp.person.dto.IndividualDto;
import com.evilcorp.person.dto.IndividualPageDto;
import com.evilcorp.person.dto.IndividualWriteDto;
import com.evilcorp.person.dto.IndividualWriteResponseDto;
import com.evilcorp.person_service.service.IndividualService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Email;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class InidividualRestControllerV1 implements PersonApi {

    private final IndividualService individualService;

    @Override
    public ResponseEntity<Void> compensateRegistration(UUID id) {
        individualService.hardDelete(id);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> delete(UUID id) {
        individualService.softDelete(id);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<IndividualPageDto> findAllByEmail(List<@Email String> email) {
        final var individuals = individualService.findByEmails(email);
        return ResponseEntity.ok(individuals);
    }

    @Override
    public ResponseEntity<IndividualDto> findById(UUID id) {
        final var individual = individualService.findById(id);
        return ResponseEntity.ok(individual);
    }

    @Override
    public ResponseEntity<IndividualWriteResponseDto> registration(IndividualWriteDto individualWriteDto) {
        final var individual = individualService.register(individualWriteDto);
        return ResponseEntity.ok(individual);
    }

    @Override
    public ResponseEntity<IndividualWriteResponseDto> update(UUID id, IndividualWriteDto individualWriteDto) {
        final var individual = individualService.update(id, individualWriteDto);
        return ResponseEntity.ok(individual);
    }
}
