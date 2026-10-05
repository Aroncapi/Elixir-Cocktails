package com.barpro.auth.service;

import com.barpro.auth.dto.LoginRequest;
import com.barpro.auth.dto.LoginResponse;
import com.barpro.auth.dto.UserResponse;
import com.barpro.auth.entity.User;
import com.barpro.auth.repository.UserRepository;
import com.barpro.auth.security.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;

    public AuthService(UserRepository users, PasswordEncoder encoder, JwtService jwtService) {
        this.users = users;
        this.encoder = encoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        User user = users.findByEmailIgnoreCase(request.email())
                .filter(User::isActive)
                .filter(existing -> encoder.matches(request.password(), existing.getPassword()))
                .orElseThrow(() -> new BadCredentialsException("Credenciales invalidas"));

        String token = jwtService.issue(user.getEmail(), user.getRole().name());
        return new LoginResponse(token, "Bearer", UserResponse.from(user));
    }

    public UserResponse currentUser(String email) {
        User user = users.findByEmailIgnoreCase(email)
                .filter(User::isActive)
                .orElseThrow(() -> new BadCredentialsException("Credenciales invalidas"));
        return UserResponse.from(user);
    }
}
