package com.investagg.controller;

import com.investagg.dto.request.LoginRequest;
import com.investagg.dto.request.RegisterRequest;
import com.investagg.dto.response.TokenResponse;
import com.investagg.dto.response.UserResponse;
import com.investagg.service.AuthService;
import com.investagg.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication")
public class AuthController {

    private final UserService userService;
    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new user")
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        return userService.createUser(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate and receive JWT")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
