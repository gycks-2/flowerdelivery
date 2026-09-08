package com.yhc.flower_delivery.domain.auth.dto;

import jakarta.validation.constraints.*;
import com.yhc.flower_delivery.domain.account.entity.AccountRole;

public record SignupRequest(
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotNull AccountRole role,
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Pattern(regexp = "[0-9+() -]{8,30}") String phone,
        @Size(max = 255) String vehicleInfo
) {
    @Override
    public String toString() {
        return "SignupRequest[redacted]";
    }
}

