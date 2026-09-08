package fr.umfds.spmanager.dto;

import java.util.List;

public record GroupDetailsResponse(int id, String name, List<UserResponse> members, List<SubjectResponse> rankedSubjects){}
