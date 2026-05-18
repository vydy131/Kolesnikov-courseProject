package com.investagg.security;

import com.investagg.exception.EntityNotFoundException;
import com.investagg.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final UserRepository userRepository;

    public UUID getCurrentUserId(UserDetails principal) {
        return userRepository.findByEmailAndDeletedAtIsNull(principal.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("Authenticated user not found"))
                .getId();
    }
}
