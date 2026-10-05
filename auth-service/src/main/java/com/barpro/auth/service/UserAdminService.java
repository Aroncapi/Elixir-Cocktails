package com.barpro.auth.service;

import com.barpro.auth.dto.CreateUserRequest;
import com.barpro.auth.dto.UpdateUserRequest;
import com.barpro.auth.dto.UserResponse;
import com.barpro.auth.entity.Role;
import com.barpro.auth.entity.User;
import com.barpro.auth.error.EmailAlreadyExistsException;
import com.barpro.auth.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserAdminService {

    private final UserRepository users;
    private final PasswordEncoder encoder;

    public UserAdminService(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    public List<UserResponse> list() {
        return users.findAll().stream().map(UserResponse::from).toList();
    }

    @Transactional
    public UserResponse create(CreateUserRequest request) {
        if (users.existsByEmailIgnoreCase(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }
        User user = new User();
        user.setEmail(request.email().trim().toLowerCase());
        user.setPassword(encoder.encode(request.password()));
        user.setFullName(request.fullName().trim());
        user.setRole(parseRole(request.role()));
        user.setActive(true);
        return UserResponse.from(users.save(user));
    }

    @Transactional
    public UserResponse update(Long id, UpdateUserRequest request) {
        User user = users.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + id));

        String currentEmail = currentEmail();
        boolean demoting = request.role() != null && !parseRole(request.role()).equals(user.getRole());
        boolean deactivating = Boolean.FALSE.equals(request.active());
        if (user.getEmail().equalsIgnoreCase(currentEmail) && (demoting || deactivating)) {
            throw new IllegalArgumentException("No puedes degradar ni desactivar tu propia cuenta");
        }

        if (request.fullName() != null && !request.fullName().isBlank()) {
            user.setFullName(request.fullName().trim());
        }
        if (request.role() != null) {
            user.setRole(parseRole(request.role()));
        }
        if (request.active() != null) {
            user.setActive(request.active());
        }
        if (request.password() != null && !request.password().isBlank()) {
            user.setPassword(encoder.encode(request.password()));
        }
        return UserResponse.from(users.save(user));
    }

    @Transactional
    public UserResponse deactivate(Long id) {
        User user = users.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + id));
        if (user.getEmail().equalsIgnoreCase(currentEmail())) {
            throw new IllegalArgumentException("No puedes desactivar tu propia cuenta");
        }
        user.setActive(false);
        return UserResponse.from(users.save(user));
    }

    private String currentEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null ? "" : authentication.getName();
    }

    private Role parseRole(String raw) {
        try {
            return Role.valueOf(raw.trim().toUpperCase());
        } catch (Exception ex) {
            throw new IllegalArgumentException("Rol invalido: " + raw + " (use ADMIN o STAFF)");
        }
    }
}
