package com.milko.individual.rest;

import com.milko.dto.output.IndividualOutputDto;
import com.milko.individual.dto.AuthRequestDto;
import com.milko.individual.dto.AuthResponseDto;
import com.milko.individual.dto.RefreshTokenRequest;
import com.milko.individual.dto.RegisterDto;
import com.milko.individual.service.AuthProviderClient;
import com.milko.individual.service.IndividualsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(("/api/v1/auth"))
public class AuthRestControllerV1 {
    private final AuthProviderClient authProviderClient;
    private final IndividualsService individualsService;

    @PostMapping("/register")
    public Mono<IndividualOutputDto> register(@RequestBody RegisterDto registerDto) {
        log.info("IN AuthRestController register(), RegisterDto = {}", registerDto);
        return individualsService.register(registerDto);
    }

    @PostMapping("/login")
    public Mono<AuthResponseDto> login(@RequestBody AuthRequestDto authRequestDto) {
        log.info("IN AuthRestController login(), AuthDto = {}", authRequestDto);
        return authProviderClient.authenticate(authRequestDto);
    }

    @PostMapping("/refresh_token")
    public Mono<AuthResponseDto> refreshToken(@RequestBody RefreshTokenRequest refreshToken) {
        log.info("IN AuthRestController refreshToken(), RefreshTokenRequest = {}", refreshToken);
        return authProviderClient.updateToken(refreshToken);
    }
}
