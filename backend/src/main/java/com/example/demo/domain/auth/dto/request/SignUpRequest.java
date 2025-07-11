package com.example.demo.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SignUpRequest(
        @Email String email,
        @NotBlank String password,
        @NotBlank String name
) {}
