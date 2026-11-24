package fr.umfds.spmanager.service;


import fr.umfds.spmanager.dto.LoginRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import fr.umfds.spmanager.exception.BadRequestException;

public class AuthServiceTest {

    @Test
    void loginWithNullRequestThrowsBadRequest() {
        AuthService log = new AuthService();
        assertThrows(BadRequestException.class, () -> log.login(null));
    }

    @Test
    void emptyLoginRequestThrowsBadRequest() {
        AuthService log = new AuthService();
        LoginRequest lr = new LoginRequest("", "MDP");
        assertThrows(BadRequestException.class, () -> log.login(lr));
    }

}
