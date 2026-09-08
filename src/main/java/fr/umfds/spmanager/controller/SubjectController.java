package fr.umfds.spmanager.controller;

import fr.umfds.spmanager.dto.CreateSubjectRequest;
import fr.umfds.spmanager.dto.SubjectResponse;
import fr.umfds.spmanager.exception.BadRequestException;
import fr.umfds.spmanager.exception.UnauthorizedException;
import fr.umfds.spmanager.security.AuthenticatedUser;
import fr.umfds.spmanager.security.JwtUtil;
import fr.umfds.spmanager.service.SubjectService;
import io.javalin.http.Context;

import java.util.List;

public class SubjectController {

    private final SubjectService subjectService;

    public SubjectController() {
        this.subjectService = new SubjectService();
    }

    public SubjectController(SubjectService subjectService) {
        this.subjectService = subjectService;
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

    public void getAllSubjects(Context ctx) {
        extractUser(ctx);
        List<SubjectResponse> list = subjectService.getAllSubjects();
        ctx.status(200);
        ctx.json(list);
    }

    public void getSubjectById(Context ctx) {
        extractUser(ctx);
        String idStr = ctx.pathParam("id");
        try {
            int id = Integer.parseInt(idStr);
            SubjectResponse res = subjectService.getSubjectById(id);
            ctx.status(200);
            ctx.json(res);
        } catch (NumberFormatException e) {
            ctx.status(400);
            throw new BadRequestException("Invalid subject ID format");
        }
    }

    public void createSubject(Context ctx) {
        AuthenticatedUser currentUser = extractUser(ctx);
        CreateSubjectRequest req = ctx.bodyAsClass(CreateSubjectRequest.class);
        SubjectResponse response = subjectService.createSubject(currentUser, req);
        ctx.status(201);
        ctx.json(response);
    }

    public void updateSubject(Context ctx) {
        AuthenticatedUser currentUser = extractUser(ctx);
        String idStr = ctx.pathParam("id");
        try {
            int id = Integer.parseInt(idStr);
            CreateSubjectRequest req = ctx.bodyAsClass(CreateSubjectRequest.class);
            SubjectResponse response = subjectService.updateSubject(currentUser, id, req);
            ctx.status(200);
            ctx.json(response);
        } catch (NumberFormatException e) {
            ctx.status(400);
            throw new BadRequestException("Invalid subject ID format");
        }
    }
}
