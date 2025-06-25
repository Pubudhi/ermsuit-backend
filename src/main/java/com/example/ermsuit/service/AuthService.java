package com.example.ermsuit.service;

import com.example.ermsuit.dto.AuthRequest;
import com.example.ermsuit.dto.AuthResponse;
import com.example.ermsuit.entity.User;
import com.example.ermsuit.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    private final AuditService auditService;

    public AuthResponse authenticate(AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        User user = (User) authentication.getPrincipal();
        
        String jwt = jwtTokenProvider.generateToken(user);
        
        // Update last login time
        userService.updateLastLogin(user.getUsername());
        
        // Log the login event
        auditService.logEvent("USER_LOGIN", "User logged in: " + user.getUsername(), "User", user.getId(), user.getUsername());
        
        return AuthResponse.builder()
                .token(jwt)
                .username(user.getUsername())
                .role(user.getRole().name())
                .build();
    }
}
