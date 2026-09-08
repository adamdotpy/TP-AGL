package fr.umfds.spmanager.controller;

import fr.umfds.spmanager.dto.LoginRequest;
import fr.umfds.spmanager.dto.LoginResponse;
import fr.umfds.spmanager.dto.UserResponse;
import fr.umfds.spmanager.exception.UnauthorizedException;
import fr.umfds.spmanager.model.User;
import fr.umfds.spmanager.repository.UserRepository;
import fr.umfds.spmanager.security.AuthenticatedUser;
import fr.umfds.spmanager.security.JwtUtil;
import fr.umfds.spmanager.service.AuthService;
import io.javalin.http.Context;

import java.util.Optional;

public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    public AuthController() {
        this.authService = new AuthService();
        this.userRepository = new UserRepository();
    }

    public AuthController(AuthService authService, UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    public void login(Context ctx) {
        LoginRequest req = ctx.bodyAsClass(LoginRequest.class);
        LoginResponse response = authService.login(req);
        ctx.status(200);
        ctx.json(response);
    }

    public void getCurrentUser(Context ctx) {
        String authHeader = ctx.header("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            ctx.status(401);
            throw new UnauthorizedException("Authentication token is missing");
        }
        String token = authHeader.substring(7);
        AuthenticatedUser user = JwtUtil.validateToken(token);
        if (user == null) {
            ctx.status(401);
            throw new UnauthorizedException("Invalid or expired token");
        }

        Optional<User> uOpt = userRepository.findById(user.getUserId());
        if (uOpt.isEmpty()) {
            ctx.status(404);
            throw new UnauthorizedException("User not found");
        }

        User u = uOpt.get();
        ctx.json(new UserResponse(u.getId(), u.getLogin(), u.getRole(), u.getFirstName(), u.getLastName()));
    }
}
