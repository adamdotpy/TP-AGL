package fr.umfds.spmanager.dto;

import fr.umfds.spmanager.model.Role;

public record LoginResponse(String token, int userId, Role role, String firstName, String lastName){}
