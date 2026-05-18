package com.investagg.controller;

import com.investagg.dto.response.NotificationResponse;
import com.investagg.security.SecurityUtils;
import com.investagg.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications")
@SecurityRequirement(name = "BearerAuth")
public class NotificationController {

    private final NotificationService notificationService;
    private final SecurityUtils securityUtils;

    @GetMapping
    @Operation(summary = "List notifications")
    public List<NotificationResponse> list(@AuthenticationPrincipal UserDetails principal) {
        return notificationService.getForUser(securityUtils.getCurrentUserId(principal));
    }

    @PatchMapping("/{notificationId}/read")
    @Operation(summary = "Mark notification as read")
    public NotificationResponse markRead(@PathVariable UUID notificationId,
                                         @AuthenticationPrincipal UserDetails principal) {
        return notificationService.markRead(securityUtils.getCurrentUserId(principal), notificationId);
    }
}
