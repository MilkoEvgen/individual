package com.milko.individual.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterDto {
    private String login;
    private String password;
    private String firstName;
    private String lastName;
    private UUID addressId;
    private String passportNumber;
    private String phoneNumber;
    private String email;
}
