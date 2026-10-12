package com.barpro.notification.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Map;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final GatewayHeaderAuthFilter gatewayHeaderAuthFilter;
    private final String internalToken;

    public SecurityConfig(GatewayHeaderAuthFilter gatewayHeaderAuthFilter,
                          @Value("${app.internal.token}") String internalToken) {
        this.gatewayHeaderAuthFilter = gatewayHeaderAuthFilter;
        this.internalToken = internalToken;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/**",
                                "/error",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html").permitAll()
                        // Solo core-service (llamada servicio-servicio, sin identidad de
                        // usuario) puede crear notificaciones. Vía gateway siempre hay
                        // X-User-Email, luego queda bloqueado para clientes externos.
                        .requestMatchers(HttpMethod.POST, "/api/notifications")
                                .access((authz, ctx) -> {
                                    HttpServletRequest req = ctx.getRequest();
                                    boolean interno = GatewayHeaderAuthFilter.isTrusted(req, internalToken);
                                    boolean sinIdentidad = req.getHeader(GatewayHeaderAuthFilter.HEADER_EMAIL) == null;
                                    return new AuthorizationDecision(interno && sinIdentidad);
                                })
                        .requestMatchers(HttpMethod.GET, "/api/notifications").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/notifications/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .exceptionHandling(handler -> handler.authenticationEntryPoint(this::write401))
                .addFilterBefore(gatewayHeaderAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    private void write401(jakarta.servlet.http.HttpServletRequest request,
                          jakarta.servlet.http.HttpServletResponse response,
                          org.springframework.security.core.AuthenticationException authException)
            throws java.io.IOException {
        response.setStatus(response.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        Map<String, Object> body = Map.of(
                "status", 401,
                "error", "Unauthorized",
                "message", "Token faltante o invalido");
        new ObjectMapper().writeValue(response.getOutputStream(), body);
    }
}
