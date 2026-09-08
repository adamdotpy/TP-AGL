package fr.umfds.spmanager.dto;

import java.util.List;

public record PreferenceResponse(int groupId, List<SubjectResponse> rankedSubjects){}