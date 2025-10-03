package com.evilcorp.person_service.mapper;


import com.evilcorp.person_service.entity.Individual;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import com.evilcorp.person_service.entity.Individual;
import com.evilcorp.person_service.entity.User;
import com.evilcorp.person_service.util.DateTimeUtil;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import org.springframework.beans.factory.annotation.Autowired;


import com.evilcorp.person.dto.IndividualWriteDto;

import static org.mapstruct.InjectionStrategy.CONSTRUCTOR;
import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(
        componentModel = SPRING,
        injectionStrategy = CONSTRUCTOR,
        uses = {
                AddressMapper.class
        }
)
//@Setter(onMethod_ = @Autowired)
//@RequiredArgsConstructor
public abstract class UserMapper {

    @Autowired
    // Можно воспользоваться @Setter(onMethod_ = @Autowired),
    // чтобы убрать аннотации @Autowired внутри класса
    protected DateTimeUtil dateTimeUtil;

    @Named("toUser")
    @Mapping(target = "id", ignore = true)

    @Mapping(target = "active", constant = "true")
    @Mapping(target = "created", expression = "java(dateTimeUtil.now())")
    @Mapping(target = "updated", expression = "java(dateTimeUtil.now())")
    @Mapping(target = "address", source = ".", qualifiedByName = "toAddress")
    public abstract User to(IndividualWriteDto dto);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "updated", expression = "java(dateTimeUtil.now())")
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "address", expression = "java(addressMapper.update(user, dto))")
    public abstract User update(
            @MappingTarget
            User user,
            IndividualWriteDto dto
    );

    public User update(Individual individual, IndividualWriteDto dto) {
        return update(individual.getUser(), dto);
    }
}