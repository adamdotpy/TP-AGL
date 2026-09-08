package fr.umfds.spmanager.controller;

import fr.umfds.spmanager.dto.*;
import fr.umfds.spmanager.exception.BadRequestException;
import fr.umfds.spmanager.exception.UnauthorizedException;
import fr.umfds.spmanager.security.AuthenticatedUser;
import fr.umfds.spmanager.security.JwtUtil;
import fr.umfds.spmanager.service.GroupService;
import fr.umfds.spmanager.service.PreferenceService;
import io.javalin.http.Context;

import java.util.List;
import java.util.Optional;

public class GroupController {

    private final GroupService groupService;
    private final PreferenceService preferenceService;

    public GroupController() {
        this.groupService = new GroupService();
        this.preferenceService = new PreferenceService();
    }

    public GroupController(GroupService groupService, PreferenceService preferenceService) {
        this.groupService = groupService;
        this.preferenceService = preferenceService;
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

    public void createGroup(Context ctx) {
        AuthenticatedUser currentUser = extractUser(ctx);
        CreateGroupRequest req = ctx.bodyAsClass(CreateGroupRequest.class);
        GroupResponse response = groupService.createGroup(currentUser, req);
        ctx.status(201);
        ctx.json(response);
    }

    public void joinGroup(Context ctx) {
        AuthenticatedUser currentUser = extractUser(ctx);
        String idStr = ctx.pathParam("id");
        try {
            int groupId = Integer.parseInt(idStr);
            GroupResponse response = groupService.joinGroup(currentUser, groupId);
            ctx.status(200);
            ctx.json(response);
        } catch (NumberFormatException e) {
            ctx.status(400);
            throw new BadRequestException("Invalid group ID format");
        }
    }

    public void getAllGroups(Context ctx) {
        extractUser(ctx);
        List<GroupResponse> list = groupService.getAllGroups();
        ctx.status(200);
        ctx.json(list);
    }

    public void getMyGroup(Context ctx) {
        AuthenticatedUser currentUser = extractUser(ctx);
        Optional<GroupResponse> groupOpt = groupService.getStudentGroup(currentUser.getUserId());
        if (groupOpt.isPresent()) {
            ctx.status(200);
            ctx.json(groupOpt.get());
        } else {
            ctx.status(404);
            ctx.json(java.util.Map.of("message", "No group found for current student"));
        }
    }

    public void savePreferences(Context ctx) {
        AuthenticatedUser currentUser = extractUser(ctx);
        String idStr = ctx.pathParam("id");
        try {
            int groupId = Integer.parseInt(idStr);
            PreferenceRequest req = ctx.bodyAsClass(PreferenceRequest.class);
            PreferenceResponse response = preferenceService.savePreferences(currentUser, groupId, req);
            ctx.status(200);
            ctx.json(response);
        } catch (NumberFormatException e) {
            ctx.status(400);
            throw new BadRequestException("Invalid group ID format");
        }
    }

    public void getGroupDetails(Context ctx) {
        String idStr = ctx.pathParam("id");
        try {
            int groupId = Integer.parseInt(idStr);
            Optional<GroupDetailsResponse> response = groupService.getGroupDetails(groupId);
            ctx.status(200);
            ctx.json(response.get());
        } catch (NumberFormatException e) {
            ctx.status(400);
            throw new BadRequestException("Invalid group ID format");
        }
    }
}
