package com.barpro.core.security;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Limita llamadas a endpoints publicos por clave (IP). En memoria, por instancia.
 */
@Component
public class IpRateLimiter {

    private final int maxIntentos;
    private final Duration ventana;

    private record Registro(int fallos, Instant ultimoFallo) {
    }

    private final ConcurrentHashMap<String, Registro> intentos = new ConcurrentHashMap<>();

    public IpRateLimiter() {
        this(3, Duration.ofMinutes(10));
    }

    public IpRateLimiter(int maxIntentos, Duration ventana) {
        this.maxIntentos = maxIntentos;
        this.ventana = ventana;
    }

    public boolean permitido(String clave) {
        Registro registro = intentos.get(clave);
        if (registro == null) {
            return true;
        }
        if (registro.fallos() < maxIntentos) {
            return true;
        }
        if (registro.ultimoFallo().plus(ventana).isBefore(Instant.now())) {
            intentos.remove(clave);
            return true;
        }
        return false;
    }

    public void registrarUso(String clave) {
        Instant ahora = Instant.now();
        intentos.compute(clave, (k, actual) -> {
            if (actual == null || actual.ultimoFallo().plus(ventana).isBefore(ahora)) {
                return new Registro(1, ahora);
            }
            return new Registro(actual.fallos() + 1, ahora);
        });
    }
}
