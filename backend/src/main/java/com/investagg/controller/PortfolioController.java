package com.investagg.controller;

import com.investagg.dto.response.PortfolioAnalyticsResponse;
import com.investagg.security.SecurityUtils;
import com.investagg.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/portfolio")
@RequiredArgsConstructor
@Tag(name = "Portfolio")
@SecurityRequirement(name = "BearerAuth")
public class PortfolioController {

    private final AnalyticsService analyticsService;
    private final SecurityUtils securityUtils;

    @GetMapping("/analytics")
    @Operation(summary = "Get portfolio analytics with live prices")
    public PortfolioAnalyticsResponse getAnalytics(@AuthenticationPrincipal UserDetails principal) {
        return analyticsService.buildAnalytics(securityUtils.getCurrentUserId(principal));
    }
}
