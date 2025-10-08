package com.evilcorp.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(value = "application.keycloak")
public record KeycloakProperties(
    String serverUrl,
    String serverRealmUrl,
    String tokenUrl,
    String realm,
    String clientId,
    String clientSecret,
    String adminUsername,
    String adminPassword,
    String adminClientId
) {
}
