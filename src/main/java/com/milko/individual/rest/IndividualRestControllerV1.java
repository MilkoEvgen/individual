package com.milko.individual.rest;

import com.milko.dto.output.IndividualOutputDto;
import com.milko.individual.service.IndividualsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(("/api/v1"))
public class IndividualRestControllerV1 {
    private final IndividualsService individualsService;

    @GetMapping("/get_info")
    public Mono<IndividualOutputDto> getUserData(@AuthenticationPrincipal Mono<Jwt> jwtMono) {
        return jwtMono.flatMap(jwt -> {
            String userId = jwt.getClaimAsString("sub");
            return individualsService.getUserInfo(UUID.fromString(userId));
        });
    }
}
