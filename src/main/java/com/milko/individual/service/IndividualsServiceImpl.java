package com.milko.individual.service;

import com.milko.dto.input.RegisterIndividualInputDto;
import com.milko.dto.output.IndividualOutputDto;
import com.milko.individual.dto.RegisterDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@Slf4j
public class IndividualsServiceImpl implements IndividualsService {
    private static final String URL = "api/v1/individuals";

    private final AuthProviderClient authProviderClient;

    private final WebClient webClientDataManager;

    public IndividualsServiceImpl(AuthProviderClient authProviderClient,
                                  @Qualifier("webClientDataManager") WebClient webClientDataManager) {
        this.authProviderClient = authProviderClient;
        this.webClientDataManager = webClientDataManager;
    }

    @Override
    public Mono<IndividualOutputDto> register(RegisterDto registerDto) {
        log.info("IN IndividualsServiceImpl register, registerDto = {}", registerDto);
        return Mono.fromCallable(() ->
                        authProviderClient.createAuthUser(registerDto))
                .flatMap(authServiceId -> webClientDataManager.post().uri(URL)
                        .bodyValue(createIndividualInputDto(UUID.fromString(authServiceId), registerDto))
                        .retrieve()
                        .bodyToMono(IndividualOutputDto.class))
                .onErrorResume(e -> {
                    log.error("Error in register method: {}", e.getMessage(), e);
                    return Mono.error(new RuntimeException(e.getMessage()));
                });
    }

    @Override
    public Mono<IndividualOutputDto> getUserInfo(UUID uuid) {
        log.info("IN IndividualsServiceImpl getUserInfo, UUID = {}", uuid);
        return webClientDataManager.get()
                .uri(URL + "/" + uuid + "/auth_service_id")
                .retrieve()
                .bodyToMono(IndividualOutputDto.class);
    }


    private RegisterIndividualInputDto createIndividualInputDto(UUID authServiceId, RegisterDto registerDto) {
        return RegisterIndividualInputDto.builder()
                .authServiceId(authServiceId)
                .firstName(registerDto.getFirstName())
                .lastName(registerDto.getLastName())
                .addressId(registerDto.getAddressId())
                .passportNumber(registerDto.getPassportNumber())
                .phoneNumber(registerDto.getPhoneNumber())
                .email(registerDto.getEmail())
                .build();
    }
}
