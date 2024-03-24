package com.milko.individual.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    @Value("${data-manager-url}")
    private String data_manager_url;
    @Value("${keycloak.get-token-url}")
    private String keyCloakUrl;

    @Bean
    @Qualifier("webClientDataManager")
    public WebClient webClientPersonApi() {
        return WebClient.builder()
                .baseUrl(data_manager_url)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Bean
    @Qualifier("webClientKeycloak")
    public WebClient webClientKeycloak() {
        return WebClient.builder()
                .baseUrl(keyCloakUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
