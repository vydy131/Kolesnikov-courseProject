package com.investagg.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ConnectAccountRequest(
        @NotNull(message = "Broker ID is required")
        UUID brokerId,

        @NotBlank(message = "Account number is required")
        String accountNumber,

        @NotBlank(message = "Broker token is required")
        String brokerToken
) {}
