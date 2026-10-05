package com.barpro.auth.bootstrap;

import com.barpro.auth.entity.Role;
import com.barpro.auth.entity.User;
import com.barpro.auth.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    public static final String ADMIN_EMAIL = "admin@velvetgilt.com";
    private static final String ADMIN_PASSWORD = "admin123";

    private final UserRepository users;
    private final PasswordEncoder encoder;

    public AdminBootstrap(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (users.count() > 0) {
            return;
        }
        User admin = new User();
        admin.setEmail(ADMIN_EMAIL);
        admin.setPassword(encoder.encode(ADMIN_PASSWORD));
        admin.setFullName("Administrador");
        admin.setRole(Role.ADMIN);
        admin.setActive(true);
        users.save(admin);
        log.warn("Usuario admin creado: {} / {} (cambiar en produccion)", ADMIN_EMAIL, ADMIN_PASSWORD);
    }
}
