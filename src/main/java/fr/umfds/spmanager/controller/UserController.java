package fr.umfds.spmanager.controller;

import fr.umfds.spmanager.dto.CreateUserRequest;
import fr.umfds.spmanager.dto.UserResponse;
import fr.umfds.spmanager.exception.UnauthorizedException;
import fr.umfds.spmanager.security.AuthenticatedUser;
import fr.umfds.spmanager.security.JwtUtil;
import fr.umfds.spmanager.service.UserService;
import io.javalin.http.Context;

import java.util.List;

public class UserController {

    private final UserService userService;

    public UserController() {
        this.userService = new UserService();
    }

    public UserController(UserService userService) {
        this.userService = userService;
    }

    private AuthenticatedUser extractUser(Context ctx) {
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
        return user;
    }

    public void createTeacher(Context ctx) {
        AuthenticatedUser currentUser = extractUser(ctx);
        CreateUserRequest req = ctx.bodyAsClass(CreateUserRequest.class);
        UserResponse response = userService.createTeacher(currentUser, req);
        ctx.status(201);
        ctx.json(response);
    }

    public void createStudent(Context ctx) {
        AuthenticatedUser currentUser = extractUser(ctx);
        CreateUserRequest req = ctx.bodyAsClass(CreateUserRequest.class);
        UserResponse response = userService.createStudent(currentUser, req);
        ctx.status(201);
        ctx.json(response);
    }

    public void getTeachers(Context ctx) {
        extractUser(ctx);
        List<UserResponse> list = userService.getAllTeachers();
        ctx.status(200);
        ctx.json(list);
    }

    public void getStudents(Context ctx) {
        extractUser(ctx);
        List<UserResponse> list = userService.getAllStudents();
        ctx.status(200);
        ctx.json(list);
    }
}
