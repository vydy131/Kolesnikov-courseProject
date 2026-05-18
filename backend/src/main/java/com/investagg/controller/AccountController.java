package com.investagg.controller;

import com.investagg.dto.request.ConnectAccountRequest;
import com.investagg.dto.response.AccountResponse;
import com.investagg.dto.response.BrokerResponse;
import com.investagg.repository.BrokerRepository;
import com.investagg.security.SecurityUtils;
import com.investagg.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
@Tag(name = "Investment Accounts")
@SecurityRequirement(name = "BearerAuth")
public class AccountController {

    private final AccountService accountService;
    private final BrokerRepository brokerRepository;
    private final SecurityUtils securityUtils;

    @PostMapping("/connect")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Connect a broker account")
    public AccountResponse connect(@Valid @RequestBody ConnectAccountRequest request,
                                   @AuthenticationPrincipal UserDetails principal) {
        return accountService.connectBrokerAccount(securityUtils.getCurrentUserId(principal), request);
    }

    @GetMapping
    @Operation(summary = "List connected accounts")
    public List<AccountResponse> list(@AuthenticationPrincipal UserDetails principal) {
        return accountService.getAccounts(securityUtils.getCurrentUserId(principal));
    }

    @DeleteMapping("/{accountId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Disconnect a broker account")
    public void disconnect(@PathVariable UUID accountId,
                           @AuthenticationPrincipal UserDetails principal) {
        accountService.disconnectAccount(securityUtils.getCurrentUserId(principal), accountId);
    }

    @GetMapping("/brokers")
    @Operation(summary = "List available brokers")
    public List<BrokerResponse> brokers() {
        return brokerRepository.findByIsActiveTrue().stream()
                .map(b -> new BrokerResponse(b.getId(), b.getName()))
                .toList();
    }
}
