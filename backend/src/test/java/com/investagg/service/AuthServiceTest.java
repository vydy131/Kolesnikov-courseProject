package com.investagg.service;

import com.investagg.dto.request.LoginRequest;
import com.investagg.dto.response.TokenResponse;
import com.investagg.entity.User;
import com.investagg.exception.AppException;
import com.investagg.exception.EntityNotFoundException;
import com.investagg.repository.UserRepository;
import com.investagg.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "expirationMs", 3_600_000L);
    }

    private User buildUser(String email, String encodedPassword) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(email);
        user.setPassword(encodedPassword);
        user.setCreatedAt(OffsetDateTime.now());
        return user;
    }

    @Test
    void login_validCredentials_returnsToken() {
        User user = buildUser("user@example.com", "hashed");
        when(userRepository.findByEmailAndDeletedAtIsNull("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashed")).thenReturn(true);
        when(jwtService.generateToken(any())).thenReturn("jwt.token.here");

        TokenResponse response = authService.login(new LoginRequest("user@example.com", "password123"));

        assertThat(response.accessToken()).isEqualTo("jwt.token.here");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(3600L);
    }

    @Test
    void login_wrongPassword_throwsUnauthorized() {
        User user = buildUser("user@example.com", "hashed");
        when(userRepository.findByEmailAndDeletedAtIsNull("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("user@example.com", "wrong")))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Invalid credentials");
    }

    @Test
    void login_emailNotFound_throwsEntityNotFound() {
        when(userRepository.findByEmailAndDeletedAtIsNull("nobody@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("nobody@example.com", "any")))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
