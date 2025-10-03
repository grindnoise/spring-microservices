package com.evilcorp.person_service.service;

import com.evilcorp.person.dto.IndividualDto;
import com.evilcorp.person.dto.IndividualPageDto;
import com.evilcorp.person.dto.IndividualWriteDto;
import com.evilcorp.person.dto.IndividualWriteResponseDto;
import com.evilcorp.person_service.entity.Individual;
import com.evilcorp.person_service.exception.PersonException;
import com.evilcorp.person_service.mapper.IndividualMapper;
import com.evilcorp.person_service.repository.IndividualRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.ProtocolException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class IndividualService {

    private final IndividualRepository individualRepository;
    private final IndividualMapper individualMapper;

    @Transactional
    public IndividualWriteResponseDto register(IndividualWriteDto individualWriteDto) {
        final var individual = individualMapper.to(individualWriteDto);
        individualRepository.save(individual);
        log.info("IN - register: individual: [{}] successfully registered", individual.getUser().getEmail());
        return new IndividualWriteResponseDto(individual.getId().toString());
    }

    public IndividualPageDto findByEmails(List<String> emails) {
        final var individuals = individualRepository.findAllByAllByEmails(emails);
        final var dtoList = individualMapper.from(individuals);
        final var individualPageDto = new IndividualPageDto();
        individualPageDto.setItems(dtoList);
        return individualPageDto;
    }

    public IndividualDto findById(UUID id) {
        final var individual = individualRepository.findById(id).orElseThrow(() -> new PersonException("Individual with id [%s] not found", id));
        log.info("IN - findById: individual with id = [{}] successfully found", id);
        return individualMapper.from(individual);
    }

    @Transactional
    public void softDelete(UUID id) {
        log.info("IN - softDelete: individual with id = [{}] successfully deleted", id);
        individualRepository.softDelete(id);
    }

    @Transactional
    public void hardDelete(UUID id) {
        var individual = individualRepository.findById(id)
                .orElseThrow(() -> new PersonException("Individual not found by id=[%s]", id));
        log.info("IN - hardDelete: individual with id = [{}] successfully deleted", id);
        individualRepository.delete(individual);
    }

    @Transactional
    public IndividualWriteResponseDto update(UUID id, IndividualWriteDto writeDto) {
        var individual = individualRepository.findById(id)
                .orElseThrow(() -> new PersonException("Individual not found by id=[%s]", id));
        individualMapper.update(individual, writeDto);
        individualRepository.save(individual);
        return new IndividualWriteResponseDto(individual.getId().toString());
    }
}
