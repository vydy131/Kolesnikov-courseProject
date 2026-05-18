package com.investagg.dto.response;

import java.util.UUID;

public record BrokerResponse(
        UUID id,
        String name
) {}
