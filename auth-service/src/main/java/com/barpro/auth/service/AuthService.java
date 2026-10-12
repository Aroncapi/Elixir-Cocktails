package com.barpro.auth.service;

import com.barpro.auth.dto.LoginRequest;
import com.barpro.auth.dto.LoginResponse;
import com.barpro.auth.dto.UserResponse;
import com.barpro.auth.entity.User;
import com.barpro.auth.error.TooManyAttemptsException;
import com.barpro.auth.repository.UserRepository;
import com.barpro.auth.security.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;
    private final LoginRateLimiter rateLimiter;
    /** Se compara contra esto cuando el usuario no existe, para no revelar si existe (timing). */
    private final String hashFantasma;

    public AuthService(UserRepository users, PasswordEncoder encoder, JwtService jwtService,
                       LoginRateLimiter rateLimiter) {
        this.users = users;
        this.encoder = encoder;
        this.jwtService = jwtService;
        this.rateLimiter = rateLimiter;
        this.hashFantasma = encoder.encode(UUID.randomUUID().toString());
    }

    public LoginResponse login(LoginRequest request) {
        String email = request.email() == null ? "" : request.email().trim();
        if (!rateLimiter.permitido(email)) {
            throw new TooManyAttemptsException(
                    "Demasiados intentos fallidos. Espera unos minutos antes de volver a intentar.");
        }

        User user = users.findByEmailIgnoreCase(email)
                .filter(User::isActive)
                .orElse(null);
        boolean passwordOk = encoder.matches(request.password(), user == null ? hashFantasma : user.getPassword());

        if (user == null || !passwordOk) {
            rateLimiter.registrarFallo(email);
            throw new BadCredentialsException("Credenciales invalidas");
        }

        rateLimiter.limpiar(email);
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
