package fr.umfds.spmanager.service;

import fr.umfds.spmanager.dto.*;
import fr.umfds.spmanager.exception.BadRequestException;
import fr.umfds.spmanager.exception.ForbiddenException;
import fr.umfds.spmanager.exception.NotFoundException;
import fr.umfds.spmanager.model.*;
import fr.umfds.spmanager.repository.GroupRepository;
import fr.umfds.spmanager.repository.PreferenceRepository;
import fr.umfds.spmanager.repository.SubjectRepository;
import fr.umfds.spmanager.repository.UserRepository;
import fr.umfds.spmanager.security.AuthenticatedUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GroupService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final PreferenceRepository preferenceRepository;
    private final SubjectRepository subjectRepository;

    public GroupService() {
        this.groupRepository = new GroupRepository();
        this.userRepository = new UserRepository();

        // new preferenceRepository for group preferences

        this.preferenceRepository = new PreferenceRepository();

        this.subjectRepository = new SubjectRepository();

    }

    public GroupService(GroupRepository groupRepository, UserRepository userRepository, PreferenceRepository preferenceRepository, SubjectRepository subjectRepository) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        //
        this.preferenceRepository = preferenceRepository;
        this.subjectRepository = subjectRepository;
    }

    public GroupResponse createGroup(AuthenticatedUser currentUser, CreateGroupRequest req) {
        if (currentUser.getRole() != Role.STUDENT) {
            throw new ForbiddenException("Only students can perform this action");
        }

        if (req == null || req.name() == null || req.name().isBlank()) {
            throw new BadRequestException("Group name is required");
        }

        String groupName = req.name().trim();

        if (groupRepository.findByName(groupName).isPresent()) {
            throw new BadRequestException("A group with this name already exists");
        }

        Optional<StudentGroup> existingGroup = groupRepository.findGroupByStudentId(currentUser.getUserId());
        if (existingGroup.isPresent()) {
            throw new BadRequestException("Student already belongs to a group");
        }

        StudentGroup newGroup = new StudentGroup(groupName);
        StudentGroup savedGroup = groupRepository.save(newGroup);

        groupRepository.addMember(savedGroup.getId(), currentUser.getUserId());

        List<User> members = groupRepository.getMembers(savedGroup.getId());
        List<UserResponse> memberResponses = new ArrayList<>();
        for (User u : members) {
            memberResponses.add(new UserResponse(
                    u.getId(),
                    u.getLogin(),
                    u.getRole(),
                    u.getFirstName(),
                    u.getLastName()
            ));
        }

        return new GroupResponse(savedGroup.getId(), savedGroup.getName(), memberResponses);
    }

    public GroupResponse joinGroup(AuthenticatedUser currentUser, int groupId) {
        if (currentUser.getRole() != Role.STUDENT) {
            throw new ForbiddenException("Only students can perform this action");
        }

        Optional<StudentGroup> groupOpt = groupRepository.findById(groupId);
        if (groupOpt.isEmpty()) {
            throw new NotFoundException("Group not found");
        }

        Optional<StudentGroup> existingGroup = groupRepository.findGroupByStudentId(currentUser.getUserId());
        if (existingGroup.isPresent()) {
            throw new BadRequestException("Student already belongs to a group");
        }

        int membersCount = groupRepository.getMembersCount(groupId);
        if (membersCount >= 4) {
            throw new BadRequestException("Group is full (maximum 4 members)");
        }

        groupRepository.addMember(groupId, currentUser.getUserId());

        StudentGroup group = groupRepository.findById(groupId).get();
        List<UserResponse> memberResponses = new ArrayList<>();
        for (User u : group.getMembers()) {
            memberResponses.add(new UserResponse(
                    u.getId(),
                    u.getLogin(),
                    u.getRole(),
                    u.getFirstName(),
                    u.getLastName()
            ));
        }

        return new GroupResponse(group.getId(), group.getName(), memberResponses);
    }

    public List<GroupResponse> getAllGroups() {
        List<StudentGroup> groups = groupRepository.findAll();
        List<GroupResponse> responses = new ArrayList<>();
        for (StudentGroup g : groups) {
            List<UserResponse> memberResponses = new ArrayList<>();
            for (User u : g.getMembers()) {
                memberResponses.add(new UserResponse(
                        u.getId(),
                        u.getLogin(),
                        u.getRole(),
                        u.getFirstName(),
                        u.getLastName()
                ));
            }
            responses.add(new GroupResponse(g.getId(), g.getName(), memberResponses));
        }
        return responses;
    }

    public Optional<GroupResponse> getStudentGroup(int studentId) {
        Optional<StudentGroup> groupOpt = groupRepository.findGroupByStudentId(studentId);
        if (groupOpt.isEmpty()) {
            return Optional.empty();
        }
        StudentGroup g = groupOpt.get();
        List<UserResponse> memberResponses = new ArrayList<>();
        for (User u : g.getMembers()) {
            memberResponses.add(new UserResponse(
                    u.getId(),
                    u.getLogin(),
                    u.getRole(),
                    u.getFirstName(),
                    u.getLastName()
            ));
        }
        return Optional.of(new GroupResponse(g.getId(), g.getName(), memberResponses));
    }

    public Optional<GroupDetailsResponse> getGroupDetails(int groupId) {
        /* Returns group details such as group info, members, preferences */
        Optional<StudentGroup> group = groupRepository.findById(groupId); // id; name WE KNOW WHAT GROUP IT IS
        if (group.isEmpty()) {
            throw new NotFoundException("Group Not Found");
        }

        int grp_id = group.get().getId();
        String grp_name = group.get().getName();
        List<UserResponse> membersR = new ArrayList<>();
        //public record UserResponse(int id, String login, Role role, String firstName, String lastName){}
        List<SubjectResponse> rankedSubjectsR = new ArrayList<>();
        //public record SubjectResponse(int id, String title, String description, int teacherId, String teacherName){}

        List<User> members = groupRepository.getMembers(groupId);
        for (User u : members) {
            membersR.add(new UserResponse(
                    u.getId(),
                    u.getLogin(),
                    u.getRole(),
                    u.getFirstName(),
                    u.getLastName()
            ));
        }

        List<Preference> rankedSubjects = preferenceRepository.findByGroupId(groupId);
        for (Preference p : rankedSubjects) {
            int subject_id = p.getSubjectId();
            //Optional<Subject> findById(int id)
            Subject subject = subjectRepository.findById(subject_id).get();
            User teacher_name = userRepository.findById(subject.getTeacherId()).get();
            rankedSubjectsR.add(new SubjectResponse(
                    subject.getId(),
                    subject.getTitle(),
                    subject.getDescription(),
                    subject.getTeacherId(),
                    teacher_name.getFirstName() + " " + teacher_name.getLastName()

            ));
        }

        return Optional.of(new GroupDetailsResponse(grp_id, grp_name, membersR, rankedSubjectsR));
    }
}
