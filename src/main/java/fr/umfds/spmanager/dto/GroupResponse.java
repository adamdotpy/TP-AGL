package fr.umfds.spmanager.dto;

import java.util.List;

public record GroupResponse(int id,String name, List<UserResponse> members) {}