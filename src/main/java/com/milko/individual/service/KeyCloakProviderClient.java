package com.milko.individual.service;

import com.milko.individual.dto.AuthRequestDto;
import com.milko.individual.dto.AuthResponseDto;
import com.milko.individual.dto.RefreshTokenRequest;
import com.milko.individual.dto.RegisterDto;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Service
public class KeyCloakProviderClient implements AuthProviderClient{
    private final String keyCloakGetTokenUrl;
    private final String grantType;
    private final String clientId;
    private final String clientSecret;
    private final String keyCloakRealm;
    private final Keycloak keycloak;
    private final WebClient webClientKeyCloak;

    public KeyCloakProviderClient(@Value("${keycloak.get-token-url}") String keyCloakGetTokenUrl,
                                  @Value("${keycloak.grant-type}") String grantType,
                                  @Value("${keycloak.client-id}") String clientId,
                                  @Value("${keycloak.client-secret}") String clientSecret,
                                  @Value("${keycloak.realm}") String keyCloakRealm,
                                  Keycloak keycloak,
                                  @Qualifier("webClientKeycloak") WebClient webClientKeyCloak) {
        this.keyCloakGetTokenUrl = keyCloakGetTokenUrl;
        this.grantType = grantType;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.keyCloakRealm = keyCloakRealm;
        this.keycloak = keycloak;
        this.webClientKeyCloak = webClientKeyCloak;
    }

    @Override
    public String createAuthUser(RegisterDto registerDto) {
        log.info("IN KeyCloakProviderClient createAuthUser, RegisterDto = {}", registerDto);
        UserRepresentation user = createKeyCloakUser(registerDto);
        Response response = keycloak.realm(keyCloakRealm).users().create(user);
        log.info("User created in keycloak, response = {}", response);
        if (response.getStatus() == Response.Status.CREATED.getStatusCode()) {
            String locationHeader = response.getHeaderString("Location");
            return locationHeader.substring(locationHeader.lastIndexOf('/') + 1);
        } else {
            String errorMessage = response.readEntity(String.class);
            throw new RuntimeException(errorMessage);
        }
    }

    @Override
    public Mono<AuthResponseDto> authenticate(AuthRequestDto authRequestDto) {
        log.info("IN KeyCloakProviderClient authenticate, AuthDto = {}", authRequestDto);
        MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
        requestParams.add("grant_type", grantType);
        requestParams.add("client_id", clientId);
        requestParams.add("client_secret", clientSecret);
        requestParams.add("username", authRequestDto.getLogin());
        requestParams.add("password", authRequestDto.getPassword());

        return webClientKeyCloak.post()
                .uri(keyCloakGetTokenUrl)
                .body(BodyInserters.fromFormData(requestParams))
                .retrieve()
                .bodyToMono(AuthResponseDto.class)
                .onErrorResume(e -> Mono.error(new RuntimeException(e.getMessage())));
    }

    @Override
    public Mono<AuthResponseDto> updateToken(RefreshTokenRequest refreshToken) {
        log.info("IN KeyCloakProviderClient updateToken, RefreshTokenRequest = {}", refreshToken);
        return webClientKeyCloak.post()
                .uri(keyCloakGetTokenUrl)
                .body(BodyInserters.fromFormData("grant_type", "refresh_token")
                        .with("refresh_token", refreshToken.getRefreshToken())
                        .with("client_id", clientId)
                        .with("client_secret", clientSecret)
                )
                .retrieve()
                .bodyToMono(AuthResponseDto.class);
    }

    private UserRepresentation createKeyCloakUser(RegisterDto registerDto) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(registerDto.getLogin());
        user.setEnabled(true);
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(registerDto.getPassword());
        credential.setTemporary(false);
        user.setCredentials(List.of(credential));
        return user;
    }
}
