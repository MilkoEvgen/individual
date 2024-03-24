package com.milko.individual.keycloak;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeyCloakProvider {
    private final String keyCloakAuthServerUrl;

    public KeyCloakProvider(@Value("${keycloak.auth-server-url}") String keyCloakAuthServerUrl) {
        this.keyCloakAuthServerUrl = keyCloakAuthServerUrl;
    }

    @Bean
    public Keycloak getKeyCloak(){
        return KeycloakBuilder.builder()
                .serverUrl(keyCloakAuthServerUrl)
                .realm("master")
                .username("admin")
                .password("admin")
                .clientId("admin-cli")
                .build();
    }
}
