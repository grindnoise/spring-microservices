package com.evilcorp.api.mapper;

import com.evilcorp.individual.dto.IndividualDto;
import com.evilcorp.individual.dto.IndividualWriteDto;
import com.evilcorp.individual.dto.IndividualWriteResponseDto;
import org.mapstruct.Mapper;

import static org.mapstruct.InjectionStrategy.CONSTRUCTOR;
import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING, injectionStrategy = CONSTRUCTOR)
public interface PersonMapper {

    com.evilcorp.person.dto.IndividualWriteDto from(IndividualWriteDto dto);

    com.evilcorp.person.dto.IndividualDto from(IndividualDto dto);

    IndividualDto from(com.evilcorp.person.dto.IndividualDto dto);

    IndividualWriteResponseDto from(com.evilcorp.person.dto.IndividualWriteResponseDto dto);
}
