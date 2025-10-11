package com.evilcorp.api;

import com.evilcorp.api.config.KeycloakProperties;
import com.evilcorp.keycloak.api.AuthApiClient;
import com.evilcorp.person.api.PersonApiClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(basePackageClasses = {AuthApiClient.class, PersonApiClient.class})
@SpringBootApplication
@EnableConfigurationProperties({KeycloakProperties.class})
public class ApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiApplication.class, args);
    }
}
