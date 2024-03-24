package com.milko.individual.testcontainers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.milko.dto.Status;
import com.milko.dto.output.IndividualOutputDto;
import com.milko.dto.output.UserOutputDto;
import com.milko.individual.dto.AuthRequestDto;
import com.milko.individual.dto.AuthResponseDto;
import com.milko.individual.dto.RegisterDto;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class IndividualRestControllerV1Test {
    @Container
    public static KeycloakContainer keycloak;

    static {
        keycloak = new KeycloakContainer().withRealmImportFile("realm.json");
        keycloak.start();
    }

    @Autowired
    private WebTestClient webTestClient;
    private static MockWebServer mockWebServer;
    private RegisterDto registerDto;
    private AuthRequestDto authRequestDto;
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

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("keycloak.auth-server-url", () -> "http://localhost:" + keycloak.getFirstMappedPort());
        registry.add("keycloak.get-token-url", () -> "http://localhost:" + keycloak.getFirstMappedPort() + "/realms/individuals_test_realm/protocol/openid-connect/token");
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", () -> "http://localhost:" + keycloak.getFirstMappedPort() + "/realms/individuals_test_realm");
        registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri", () -> "http://localhost:" + keycloak.getFirstMappedPort() + "/realms/individuals_test_realm/protocol/openid-connect/certs");
        registry.add("data-manager-url", () -> "http://localhost:" + mockWebServer.getPort());
        registry.add("keycloak.realm", () -> "individuals_test_realm");
        registry.add("keycloak.grant-type", () -> "password");
        registry.add("keycloak.client-id", () -> "test-client");
        registry.add("keycloak.client-secret", () -> "1DXKZHfCMcDoEgwbnP0MccgHlbXhLpFL");
    }

    @BeforeEach
    void initialize() {
        registerDto = RegisterDto.builder()
                .login("login")
                .password("password")
                .build();

        authRequestDto = AuthRequestDto.builder()
                .login("login")
                .password("password")
                .build();

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
    @Order(1)
    public void registerShouldReturnIndividualOutputDto() throws JsonProcessingException {
        mockWebServer.enqueue(new MockResponse()
                .addHeader("Content-Type", "application/json")
                .setBody(objectMapper.writeValueAsString(individualOutputDto)));
        mockWebServer.enqueue(new MockResponse()
                .addHeader("Content-Type", "application/json")
                .setBody(objectMapper.writeValueAsString(individualOutputDto)));

        webTestClient.post().uri("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(registerDto)
                .exchange()
                .expectStatus().isOk()
                .expectBody();

        EntityExchangeResult<AuthResponseDto> exchangeResult = webTestClient.post().uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(authRequestDto)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AuthResponseDto.class)
                .returnResult();

        String accessToken = Objects.requireNonNull(exchangeResult.getResponseBody()).getAccessToken();

        webTestClient.get().uri("/api/v1/get_info")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.user.id").isEqualTo("b52db198-e5bd-4768-9735-a2e862d6c469")
                .jsonPath("$.user.firstName").isEqualTo("firstName")
                .jsonPath("$.user.lastName").isEqualTo("lastName")
                .jsonPath("$.user.status").isEqualTo("ACTIVE")
                .jsonPath("$.passportNumber").isEqualTo("passportNumber")
                .jsonPath("$.phoneNumber").isEqualTo("phoneNumber")
                .jsonPath("$.email").isEqualTo("email")
                .jsonPath("$.status").isEqualTo("ACTIVE");
    }

    @Test
    @Order(1)
    public void registerShouldReturn401IfTokenNotValid() throws JsonProcessingException {
        EntityExchangeResult<AuthResponseDto> exchangeResult = webTestClient.post().uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(authRequestDto)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AuthResponseDto.class)
                .returnResult();

        String accessToken = Objects.requireNonNull(exchangeResult.getResponseBody()).getRefreshToken();

        webTestClient.get().uri("/api/v1/get_info")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNAUTHORIZED);
    }

}




