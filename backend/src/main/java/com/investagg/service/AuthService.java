package com.investagg.service;

import com.investagg.dto.request.LoginRequest;
import com.investagg.dto.response.TokenResponse;
import com.investagg.entity.User;
import com.investagg.exception.AppException;
import com.investagg.exception.EntityNotFoundException;
import com.investagg.repository.UserRepository;
import com.investagg.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(request.email())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new AppException("Invalid credentials", HttpStatus.UNAUTHORIZED, "UNAUTHORIZED");
        }

        org.springframework.security.core.userdetails.User principal =
                new org.springframework.security.core.userdetails.User(
                        user.getEmail(), user.getPassword(), java.util.List.of()
                );

        String token = jwtService.generateToken(principal);
        return new TokenResponse(token, expirationMs / 1000);
    }
}
