package com.barpro.core.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

/**
 * Confiar en X-User-Email/X-User-Role SOLO cuando la peticion trae el token
 * interno que unicamente el gateway (y los servicios entre si) conocen.
 * Asi un atacante que llegue directo al puerto 8082 no puede declararse ADMIN.
 */
@Component
public class GatewayHeaderAuthFilter extends OncePerRequestFilter {

    static final String HEADER_EMAIL = "X-User-Email";
    static final String HEADER_ROLE = "X-User-Role";
    static final String HEADER_INTERNAL = "X-Internal-Token";

    private final String internalToken;

    public GatewayHeaderAuthFilter(@Value("${app.internal.token}") String internalToken) {
        this.internalToken = internalToken;
    }

    static boolean isTrusted(HttpServletRequest request, String expected) {
        String provided = request.getHeader(HEADER_INTERNAL);
        if (provided == null || expected == null || expected.isBlank()) {
            return false;
        }
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                provided.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String email = request.getHeader(HEADER_EMAIL);
        String role = request.getHeader(HEADER_ROLE);
        if (isTrusted(request, internalToken)
                && email != null && !email.isBlank() && role != null && !role.isBlank()
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            email,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + role)));
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);
    }
}
