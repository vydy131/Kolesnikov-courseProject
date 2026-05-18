package com.investagg.service;

import com.investagg.dto.request.RegisterRequest;
import com.investagg.dto.response.UserResponse;
import com.investagg.entity.User;
import com.investagg.exception.ConflictException;
import com.investagg.repository.PortfolioRepository;
import com.investagg.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PortfolioRepository portfolioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void createUser_success_returnsUserResponse() {
        RegisterRequest request = new RegisterRequest("test@example.com", "password123");
        when(userRepository.existsByEmailAndDeletedAtIsNull("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed");

        User savedUser = new User();
        savedUser.setId(UUID.randomUUID());
        savedUser.setEmail("test@example.com");
        savedUser.setPassword("hashed");
        savedUser.setCreatedAt(OffsetDateTime.now());
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponse response = userService.createUser(request);

        assertThat(response.email()).isEqualTo("test@example.com");
        verify(portfolioRepository).save(any());
    }

    @Test
    void createUser_encodesPassword() {
        RegisterRequest request = new RegisterRequest("test@example.com", "password123");
        when(userRepository.existsByEmailAndDeletedAtIsNull(anyString())).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("bcrypt_hash");

        User savedUser = new User();
        savedUser.setId(UUID.randomUUID());
        savedUser.setEmail("test@example.com");
        savedUser.setPassword("bcrypt_hash");
        savedUser.setCreatedAt(OffsetDateTime.now());
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        userService.createUser(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getPassword()).isEqualTo("bcrypt_hash");
    }

    @Test
    void createUser_duplicateEmail_throwsConflictException() {
        RegisterRequest request = new RegisterRequest("duplicate@example.com", "password123");
        when(userRepository.existsByEmailAndDeletedAtIsNull("duplicate@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("duplicate@example.com");

        verify(userRepository, never()).save(any());
        verify(portfolioRepository, never()).save(any());
    }
}
