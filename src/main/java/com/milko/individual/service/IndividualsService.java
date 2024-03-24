package com.milko.individual.service;

import com.milko.dto.output.IndividualOutputDto;
import com.milko.individual.dto.RegisterDto;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface IndividualsService {

    Mono<IndividualOutputDto> register(RegisterDto registerDto);

    Mono<IndividualOutputDto> getUserInfo(UUID uuid);
}
