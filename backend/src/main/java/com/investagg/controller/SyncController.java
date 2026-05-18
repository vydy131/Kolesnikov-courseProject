package com.investagg.controller;

import com.investagg.dto.response.SyncStatusResponse;
import com.investagg.security.SecurityUtils;
import com.investagg.service.AccountService;
import com.investagg.service.SyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/sync")
@RequiredArgsConstructor
@Tag(name = "Broker Sync")
@SecurityRequirement(name = "BearerAuth")
public class SyncController {

    private final SyncService syncService;
    private final AccountService accountService;
    private final SecurityUtils securityUtils;

    @PostMapping("/accounts/{accountId}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Trigger manual sync for a broker account")
    public SyncStatusResponse syncAccount(@PathVariable UUID accountId,
                                          @AuthenticationPrincipal UserDetails principal) {
        UUID userId = securityUtils.getCurrentUserId(principal);
        accountService.validateOwnership(userId, accountId);
        syncService.syncBrokerAccount(accountId);
        return new SyncStatusResponse(accountId, "SYNCED", OffsetDateTime.now());
    }
}
