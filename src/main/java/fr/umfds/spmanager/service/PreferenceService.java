package fr.umfds.spmanager.service;

import fr.umfds.spmanager.config.AppConfig;
import fr.umfds.spmanager.dto.PreferenceRequest;
import fr.umfds.spmanager.dto.PreferenceResponse;
import fr.umfds.spmanager.dto.SubjectResponse;
import fr.umfds.spmanager.exception.BadRequestException;
import fr.umfds.spmanager.exception.ForbiddenException;
import fr.umfds.spmanager.exception.NotFoundException;
import fr.umfds.spmanager.model.Preference;
import fr.umfds.spmanager.model.Role;
import fr.umfds.spmanager.model.StudentGroup;
import fr.umfds.spmanager.model.Subject;
import fr.umfds.spmanager.model.User;
import fr.umfds.spmanager.repository.GroupRepository;
import fr.umfds.spmanager.repository.PreferenceRepository;
import fr.umfds.spmanager.repository.SubjectRepository;
import fr.umfds.spmanager.repository.UserRepository;
import fr.umfds.spmanager.security.AuthenticatedUser;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class PreferenceService {

    private final PreferenceRepository preferenceRepository;
    private final GroupRepository groupRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;

    public PreferenceService() {
        this.preferenceRepository = new PreferenceRepository();
        this.groupRepository = new GroupRepository();
        this.subjectRepository = new SubjectRepository();
        this.userRepository = new UserRepository();
    }

    public PreferenceService(PreferenceRepository preferenceRepository,
                             GroupRepository groupRepository,
                             SubjectRepository subjectRepository,
                             UserRepository userRepository) {
        this.preferenceRepository = preferenceRepository;
        this.groupRepository = groupRepository;
        this.subjectRepository = subjectRepository;
        this.userRepository = userRepository;
    }

    public PreferenceResponse savePreferences(AuthenticatedUser currentUser, int groupId, PreferenceRequest req) {
        if (currentUser.getRole() != Role.STUDENT) {
            throw new ForbiddenException("Only students can perform this action");
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(AppConfig.getPreferenceStartDate()) || now.isAfter(AppConfig.getPreferenceEndDate())) {
            throw new BadRequestException("Preferences submission is closed outside the allowed period");
        }

        Optional<StudentGroup> groupOpt = groupRepository.findById(groupId);
        if (groupOpt.isEmpty()) {
            throw new NotFoundException("Group not found");
        }

        StudentGroup group = groupOpt.get();
        boolean isMember = false;
        for (User u : group.getMembers()) {
            if (u.getId() == currentUser.getUserId()) {
                isMember = true;
                break;
            }
        }
        if (!isMember) {
            throw new ForbiddenException("You must be a member of the group to submit preferences");
        }

        if (req == null || req.subjectIds() == null) {
            throw new BadRequestException("Subject list is required");
        }

        List<Integer> subjectIds = req.subjectIds();
        if (subjectIds.size() != 5) {
            throw new BadRequestException("Exactly 5 preferences must be provided");
        }

        Set<Integer> uniqueIds = new HashSet<>(subjectIds);
        if (uniqueIds.size() != 5) {
            throw new BadRequestException("Each subject must be distinct in preferences");
        }

        List<SubjectResponse> rankedSubjects = new ArrayList<>();
        for (Integer subjectId : subjectIds) {
            Optional<Subject> sOpt = subjectRepository.findById(subjectId);
            if (sOpt.isEmpty()) {
                throw new NotFoundException("Subject not found");
            }
            Subject s = sOpt.get();
            String teacherName = "";
            Optional<User> teacherOpt = userRepository.findById(s.getTeacherId());
            if (teacherOpt.isPresent()) {
                User teacher = teacherOpt.get();
                teacherName = teacher.getFirstName() + " " + teacher.getLastName();
            }
            SubjectResponse res = new SubjectResponse(s.getId(), s.getTitle(), s.getDescription(), s.getTeacherId(), teacherName.trim());
            rankedSubjects.add(res);
        }

        preferenceRepository.savePreferences(groupId, subjectIds);

        return new PreferenceResponse(groupId, rankedSubjects);
    }

    public List<Preference> getPreferencesByGroupId(int groupId) {
        return preferenceRepository.findByGroupId(groupId);
    }
}
