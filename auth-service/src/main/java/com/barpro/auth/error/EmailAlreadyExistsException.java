package com.barpro.auth.error;

public class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException(String email) {
        super("Ya existe un usuario con el email " + email);
    }
}
