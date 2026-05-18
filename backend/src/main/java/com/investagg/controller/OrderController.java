package com.investagg.controller;

import com.investagg.dto.request.TradeOrderRequest;
import com.investagg.dto.response.PageResponse;
import com.investagg.dto.response.TradeOrderResponse;
import com.investagg.security.SecurityUtils;
import com.investagg.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Tag(name = "Trade Orders")
@SecurityRequirement(name = "BearerAuth")
public class OrderController {

    private final OrderService orderService;
    private final SecurityUtils securityUtils;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Place a trade order")
    public TradeOrderResponse create(@Valid @RequestBody TradeOrderRequest request,
                                     @AuthenticationPrincipal UserDetails principal) {
        return orderService.createOrder(securityUtils.getCurrentUserId(principal), request);
    }

    @GetMapping
    @Operation(summary = "List trade orders (paginated)")
    public PageResponse<TradeOrderResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails principal) {
        return orderService.getOrders(
                securityUtils.getCurrentUserId(principal),
                PageRequest.of(page, size, Sort.by("placedAt").descending())
        );
    }

    @DeleteMapping("/{orderId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Cancel a trade order")
    public void cancel(@PathVariable UUID orderId,
                       @AuthenticationPrincipal UserDetails principal) {
        orderService.cancelOrder(securityUtils.getCurrentUserId(principal), orderId);
    }
}
