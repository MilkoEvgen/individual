package com.milko.individual.service;

import com.milko.individual.dto.AuthRequestDto;
import com.milko.individual.dto.AuthResponseDto;
import com.milko.individual.dto.RefreshTokenRequest;
import com.milko.individual.dto.RegisterDto;
import reactor.core.publisher.Mono;

public interface AuthProviderClient {

    public String createAuthUser(RegisterDto registerDto);

    Mono<AuthResponseDto> authenticate(AuthRequestDto authRequestDto);

    Mono<AuthResponseDto> updateToken(RefreshTokenRequest refreshToken);
}
