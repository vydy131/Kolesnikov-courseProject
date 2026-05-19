package com.investagg.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.investagg.dto.request.TradeOrderRequest;
import com.investagg.dto.response.PageResponse;
import com.investagg.dto.response.TradeOrderResponse;
import com.investagg.entity.enums.OrderDirection;
import com.investagg.entity.enums.OrderStatus;
import com.investagg.security.JwtAuthFilter;
import com.investagg.security.JwtService;
import com.investagg.security.SecurityUtils;
import com.investagg.service.OrderService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private SecurityUtils securityUtils;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private final UUID USER_ID = UUID.randomUUID();
    private final UUID ACCOUNT_ID = UUID.randomUUID();

    @BeforeEach
    void allowFilterChain() throws Exception {
        doAnswer(inv -> {
            ((FilterChain) inv.getArgument(2))
                    .doFilter((ServletRequest) inv.getArgument(0), (ServletResponse) inv.getArgument(1));
            return null;
        }).when(jwtAuthFilter).doFilter(any(), any(), any());
    }

    private TradeOrderResponse buildOrderResponse() {
        return new TradeOrderResponse(
                UUID.randomUUID(), ACCOUNT_ID, "SBER",
                OrderDirection.BUY, new BigDecimal("10"), new BigDecimal("250.00"),
                OrderStatus.PENDING, "broker-123",
                OffsetDateTime.now(), null
        );
    }

    @Test
    @WithMockUser
    void create_validRequest_returns201() throws Exception {
        TradeOrderRequest req = new TradeOrderRequest(
                ACCOUNT_ID, "SBER", OrderDirection.BUY,
                new BigDecimal("10"), new BigDecimal("250.00")
        );
        when(securityUtils.getCurrentUserId(any())).thenReturn(USER_ID);
        when(orderService.createOrder(eq(USER_ID), any())).thenReturn(buildOrderResponse());

        mockMvc.perform(post("/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ticker").value("SBER"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @WithMockUser
    void create_missingTicker_returns400() throws Exception {
        String body = """
                {"accountId":"%s","direction":"BUY","qty":10,"price":250.00}
                """.formatted(ACCOUNT_ID);

        mockMvc.perform(post("/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void create_negativeQty_returns400() throws Exception {
        TradeOrderRequest req = new TradeOrderRequest(
                ACCOUNT_ID, "SBER", OrderDirection.BUY,
                new BigDecimal("-5"), new BigDecimal("250.00")
        );

        mockMvc.perform(post("/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void list_returnsPaginatedOrders() throws Exception {
        PageResponse<TradeOrderResponse> page = new PageResponse<>(
                List.of(buildOrderResponse()), 0, 20, 1L, 1
        );
        when(securityUtils.getCurrentUserId(any())).thenReturn(USER_ID);
        when(orderService.getOrders(eq(USER_ID), any())).thenReturn(page);

        mockMvc.perform(get("/orders").param("page", "0").param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].ticker").value("SBER"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void list_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/orders"))
                .andExpect(status().isUnauthorized());
    }
}
