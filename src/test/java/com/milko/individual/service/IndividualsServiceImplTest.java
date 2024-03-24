package com.milko.individual.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.milko.dto.Status;
import com.milko.dto.output.IndividualOutputDto;
import com.milko.dto.output.UserOutputDto;
import com.milko.individual.dto.RegisterDto;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class IndividualsServiceImplTest {
    @Mock
    private AuthProviderClient authProviderClient;

    private IndividualsServiceImpl individualsService;

    public static MockWebServer mockWebServer;
    private RegisterDto registerDto;
    private IndividualOutputDto individualOutputDto;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeAll
    static void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @BeforeEach
    void initialize() {
        String baseUrl = String.format("http://localhost:%s",
                mockWebServer.getPort());
        WebClient webClientDataManager = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        individualsService = new IndividualsServiceImpl(authProviderClient, webClientDataManager);

        registerDto = RegisterDto.builder().build();

        UserOutputDto userOutputDto = UserOutputDto.builder()
                .id(UUID.fromString("b52db198-e5bd-4768-9735-a2e862d6c469"))
                .firstName("firstName")
                .lastName("lastName")
                .status(Status.ACTIVE)
                .build();

        individualOutputDto = IndividualOutputDto.builder()
                .passportNumber("passportNumber")
                .phoneNumber("phoneNumber")
                .email("email")
                .user(userOutputDto)
                .status(Status.ACTIVE)
                .build();
    }

    @Test
    void registerShouldReturnIndividualOutputDto() throws JsonProcessingException {
        when(authProviderClient.createAuthUser(any(RegisterDto.class))).thenReturn(UUID.randomUUID().toString());

        mockWebServer.enqueue(new MockResponse()
                .addHeader("Content-Type", "application/json")
                .setBody(objectMapper.writeValueAsString(individualOutputDto)));

        Mono<IndividualOutputDto> result = individualsService.register(registerDto);

        StepVerifier.create(result)
                .expectNextMatches(resultDto -> {
                    return resultDto.getUser().getId().equals(UUID.fromString("b52db198-e5bd-4768-9735-a2e862d6c469")) &&
                            resultDto.getUser().getFirstName().equals("firstName") &&
                            resultDto.getUser().getLastName().equals("lastName") &&
                            resultDto.getUser().getStatus().equals(Status.ACTIVE) &&
                            resultDto.getPassportNumber().equals("passportNumber") &&
                            resultDto.getPhoneNumber().equals("phoneNumber") &&
                            resultDto.getEmail().equals("email") &&
                            resultDto.getStatus().equals(Status.ACTIVE);
                }).expectComplete()
                .verify();
    }

    @Test
    void registerShouldThrowException() {
        when(authProviderClient.createAuthUser(any(RegisterDto.class))).thenThrow(new RuntimeException("message"));
        Mono<IndividualOutputDto> result = individualsService.register(registerDto);

        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void getUserInfoShouldReturnIndividualOutputDto() throws JsonProcessingException {
        mockWebServer.enqueue(new MockResponse()
                .addHeader("Content-Type", "application/json")
                .setBody(objectMapper.writeValueAsString(individualOutputDto)));

        Mono<IndividualOutputDto> result = individualsService.getUserInfo(UUID.randomUUID());

        StepVerifier.create(result)
                .expectNextMatches(resultDto -> {
                    return resultDto.getUser().getId().equals(UUID.fromString("b52db198-e5bd-4768-9735-a2e862d6c469")) &&
                            resultDto.getUser().getFirstName().equals("firstName") &&
                            resultDto.getUser().getLastName().equals("lastName") &&
                            resultDto.getUser().getStatus().equals(Status.ACTIVE) &&
                            resultDto.getPassportNumber().equals("passportNumber") &&
                            resultDto.getPhoneNumber().equals("phoneNumber") &&
                            resultDto.getEmail().equals("email") &&
                            resultDto.getStatus().equals(Status.ACTIVE);
                }).expectComplete()
                .verify();
    }

    @Test
    void getUserInfoShouldThrowException() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error"));
        Mono<IndividualOutputDto> result = individualsService.getUserInfo(UUID.randomUUID());

        StepVerifier.create(result)
                .expectError(WebClientResponseException.class)
                .verify();
    }


}
