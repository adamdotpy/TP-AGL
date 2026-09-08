package fr.umfds.spmanager.security;

import fr.umfds.spmanager.model.Role;

public class AuthenticatedUser {
    private final int userId;
    private final String login;
    private final Role role;

    public AuthenticatedUser(int userId, String login, Role role) {
        this.userId = userId;
        this.login = login;
        this.role = role;
    }

    public int getUserId() {
        return userId;
    }

    public String getLogin() {
        return login;
    }

    public Role getRole() {
        return role;
    }
}
