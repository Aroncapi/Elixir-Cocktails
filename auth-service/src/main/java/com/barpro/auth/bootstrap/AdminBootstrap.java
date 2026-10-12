package com.barpro.auth.bootstrap;

import com.barpro.auth.entity.Role;
import com.barpro.auth.entity.User;
import com.barpro.auth.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    public static final String ADMIN_EMAIL = "admin@velvetgilt.com";
    /** Password SOLO de desarrollo; en produccion se pone via variable ADMIN_PASSWORD. */
    private static final String DEV_PASSWORD = "admin123";

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final String password;

    public AdminBootstrap(UserRepository users,
                          PasswordEncoder encoder,
                          @Value("${app.admin.password:admin123}") String password) {
        this.users = users;
        this.encoder = encoder;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (users.count() > 0) {
            return;
        }
        User admin = new User();
        admin.setEmail(ADMIN_EMAIL);
        admin.setPassword(encoder.encode(password));
        admin.setFullName("Administrador");
        admin.setRole(Role.ADMIN);
        admin.setActive(true);
        users.save(admin);
        if (DEV_PASSWORD.equals(password)) {
            log.warn("Usuario admin creado con la password POR DEFECTO de desarrollo. "
                    + "Define la variable ADMIN_PASSWORD antes de exponer esto en la nube.");
        } else {
            log.info("Usuario admin creado: {} (password desde la variable ADMIN_PASSWORD)", ADMIN_EMAIL);
        }
    }
}
