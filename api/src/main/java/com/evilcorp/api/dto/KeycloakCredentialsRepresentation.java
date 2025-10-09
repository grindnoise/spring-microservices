package com.evilcorp.api.dto;

public record KeycloakCredentialsRepresentation(
        String type,
        String value,
        Boolean temporary
) {
}
