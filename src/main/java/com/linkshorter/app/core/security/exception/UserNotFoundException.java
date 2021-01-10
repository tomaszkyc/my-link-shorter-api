package com.linkshorter.app.core.security.exception;

import java.util.UUID;

public class UserNotFoundException extends Exception {

    public UserNotFoundException(UUID userId) {
        super(String.format("Użytkownik o podanym id: %s nie istnieje", userId));
    }

    public UserNotFoundException(String username) {
        super(String.format("Użytkownik o podanej nazwie nie istnieje w bazie: " + username));
    }
}
