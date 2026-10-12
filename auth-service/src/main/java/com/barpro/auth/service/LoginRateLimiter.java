package com.barpro.auth.service;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bloquea el login por correo despues de varios fallos seguidos.
 * En memoria y por instancia: suficiente para un despliegue unico (sin redis).
 */
@Component
public class LoginRateLimiter {

    private static final int MAX_FALLOS = 5;
    private static final Duration VENTANA = Duration.ofMinutes(15);

    private record Registro(int fallos, Instant ultimoFallo) {
    }

    private final ConcurrentHashMap<String, Registro> intentos = new ConcurrentHashMap<>();

    public boolean permitido(String email) {
        Registro registro = intentos.get(clave(email));
        if (registro == null) {
            return true;
        }
        if (registro.fallos() < MAX_FALLOS) {
            return true;
        }
        if (registro.ultimoFallo().plus(VENTANA).isBefore(Instant.now())) {
            intentos.remove(clave(email));
            return true;
        }
        return false;
    }

    public void registrarFallo(String email) {
        Instant ahora = Instant.now();
        intentos.compute(clave(email), (k, actual) -> {
            if (actual == null || actual.ultimoFallo().plus(VENTANA).isBefore(ahora)) {
                return new Registro(1, ahora);
            }
            return new Registro(actual.fallos() + 1, ahora);
        });
    }

    public void limpiar(String email) {
        intentos.remove(clave(email));
    }

    private String clave(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}
