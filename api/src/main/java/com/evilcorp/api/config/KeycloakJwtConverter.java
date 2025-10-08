package com.evilcorp.api.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Flux;

public class KeycloakJwtConverter implements Converter<Jwt, Flux<GrantedAuthority>> {

    @Override
    public Flux<GrantedAuthority> convert(Jwt source) {
        var roles = source.getClaimAsStringList("roles");
        if(roles == null || roles.isEmpty())
            return Flux.empty();

        return Flux.fromIterable(roles.stream()
                .map(SimpleGrantedAuthority::new)
                .toList());

    }
}
