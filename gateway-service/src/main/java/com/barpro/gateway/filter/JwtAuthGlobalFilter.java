package com.barpro.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class JwtAuthGlobalFilter implements GlobalFilter, Ordered {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String HEADER_EMAIL = "X-User-Email";
    private static final String HEADER_ROLE = "X-User-Role";
    private static final List<String> PUBLIC_PATHS = List.of("/api/auth/login", "/error");
    private static final List<String> PUBLIC_PREFIXES = List.of("/api/public/", "/actuator/");

    private final SecretKey key;

    public JwtAuthGlobalFilter(@Value("${app.jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        ServerHttpRequest.Builder sanitized = request.mutate()
                .headers(headers -> {
                    headers.remove(HEADER_EMAIL);
                    headers.remove(HEADER_ROLE);
                });

        if (isPublic(path)) {
            return chain.filter(exchange.mutate().request(sanitized.build()).build());
        }

        String authorization = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            return unauthorized(exchange, "Token faltante");
        }

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(authorization.substring(BEARER_PREFIX.length()))
                    .getPayload();

            ServerHttpRequest authenticated = sanitized
                    .header(HEADER_EMAIL, claims.getSubject())
                    .header(HEADER_ROLE, String.valueOf(claims.get("role")))
                    .build();
            return chain.filter(exchange.mutate().request(authenticated).build());
        } catch (JwtException | IllegalArgumentException ex) {
            return unauthorized(exchange, "Token invalido o expirado");
        }
    }

    private boolean isPublic(String path) {
        if (PUBLIC_PATHS.contains(path)) {
            return true;
        }
        return PUBLIC_PREFIXES.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"" + message + "\"}";
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
