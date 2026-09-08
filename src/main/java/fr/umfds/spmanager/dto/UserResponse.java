package fr.umfds.spmanager.dto;

import fr.umfds.spmanager.model.Role;

public record UserResponse(int id, String login, Role role, String firstName, String lastName){}