package com.investagg.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.investagg.dto.request.ConnectAccountRequest;
import com.investagg.dto.response.AccountResponse;
import com.investagg.entity.enums.AccountStatus;
import com.investagg.exception.ConflictException;
import com.investagg.repository.BrokerRepository;
import com.investagg.security.JwtService;
import com.investagg.security.SecurityUtils;
import com.investagg.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AccountService accountService;

    @MockBean
    private BrokerRepository brokerRepository;

    @MockBean
    private SecurityUtils securityUtils;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    private final UUID USER_ID = UUID.randomUUID();
    private final UUID BROKER_ID = UUID.randomUUID();

    private AccountResponse buildAccountResponse() {
        return new AccountResponse(
                UUID.randomUUID(), BROKER_ID, "Tinkoff",
                "ACC-001", AccountStatus.ACTIVE, null, OffsetDateTime.now()
        );
    }

    @Test
    @WithMockUser
    void connect_validRequest_returns201() throws Exception {
        ConnectAccountRequest req = new ConnectAccountRequest(BROKER_ID, "ACC-001", "raw-token");
        when(securityUtils.getCurrentUserId(any())).thenReturn(USER_ID);
        when(accountService.connectBrokerAccount(eq(USER_ID), any())).thenReturn(buildAccountResponse());

        mockMvc.perform(post("/accounts/connect")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.brokerName").value("Tinkoff"))
                .andExpect(jsonPath("$.accountNumber").value("ACC-001"));
    }

    @Test
    @WithMockUser
    void connect_duplicate_returns409() throws Exception {
        ConnectAccountRequest req = new ConnectAccountRequest(BROKER_ID, "ACC-001", "raw-token");
        when(securityUtils.getCurrentUserId(any())).thenReturn(USER_ID);
        when(accountService.connectBrokerAccount(any(), any()))
                .thenThrow(new ConflictException("Account already connected"));

        mockMvc.perform(post("/accounts/connect")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser
    void connect_missingBrokerId_returns400() throws Exception {
        String body = """
                {"accountNumber":"ACC-001","brokerToken":"token"}
                """;

        mockMvc.perform(post("/accounts/connect")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void list_authenticated_returnsAccounts() throws Exception {
        when(securityUtils.getCurrentUserId(any())).thenReturn(USER_ID);
        when(accountService.getAccounts(USER_ID)).thenReturn(List.of(buildAccountResponse()));

        mockMvc.perform(get("/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].brokerName").value("Tinkoff"));
    }

    @Test
    void list_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/accounts"))
                .andExpect(status().isUnauthorized());
    }
}
