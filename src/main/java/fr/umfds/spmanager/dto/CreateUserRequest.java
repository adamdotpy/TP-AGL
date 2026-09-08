package fr.umfds.spmanager.dto;

public record CreateUserRequest(String login,String password,String firstName,String lastName){}