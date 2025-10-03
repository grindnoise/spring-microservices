package com.evilcorp.person_service.mapper;


import lombok.RequiredArgsConstructor;
import lombok.Setter;
import com.evilcorp.person.dto.AddressDto;
import com.evilcorp.person.dto.IndividualWriteDto;
import com.evilcorp.person_service.entity.Address;
import com.evilcorp.person_service.entity.Country;
import com.evilcorp.person_service.entity.User;
import com.evilcorp.person_service.exception.PersonException;
import com.evilcorp.person_service.repository.CountryRepository;
import com.evilcorp.person_service.util.DateTimeUtil;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import static org.mapstruct.InjectionStrategy.CONSTRUCTOR;
import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING, injectionStrategy = CONSTRUCTOR)
@Setter(onMethod_ = @Autowired)
public abstract class AddressMapper {

    protected CountryRepository countryRepository;
    protected DateTimeUtil dateTimeUtil;

    @Named("toAddress")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "created", expression = "java(dateTimeUtil.now())")
    @Mapping(target = "updated", expression = "java(dateTimeUtil.now())")
    @Mapping(target = "city", source = "address.city")
    @Mapping(target = "zipCode", source = "address.zipCode")
    @Mapping(target = "address", source = "address.address")
    @Mapping(target = "country", source = "address.countryCode", qualifiedByName = "toCountry")
    public abstract Address to(IndividualWriteDto dto);

    @Named("fromAddress")
    @Mapping(target = "city", source = "city")
    @Mapping(target = "zipCode", source = "zipCode")
    @Mapping(target = "address", source = "address")
    @Mapping(target = "countryCode", source = "country.code")
    public abstract AddressDto from(Address address);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "updated", expression = "java(dateTimeUtil.now())")
    @Mapping(target = "city", source = "address.city")
    @Mapping(target = "zipCode", source = "address.zipCode")
    @Mapping(target = "address", source = "address.address")
    @Mapping(target = "country", source = "address.countryCode", qualifiedByName = "toCountry")
    public abstract Address update(
            @MappingTarget
            Address address,
            IndividualWriteDto dto
    );

    @Named("toCountry")
    public Country toCountry(String countryCode) {
        return countryRepository.findByCode(countryCode)
                .orElseThrow(() -> new PersonException("Unknow country code: [%s]", countryCode));
    }

    public Address update(User user, IndividualWriteDto dto) {
        return update(user.getAddress(), dto);
    }
}