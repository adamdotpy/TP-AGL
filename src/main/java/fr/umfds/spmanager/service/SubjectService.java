package fr.umfds.spmanager.service;

import fr.umfds.spmanager.dto.CreateSubjectRequest;
import fr.umfds.spmanager.dto.SubjectResponse;
import fr.umfds.spmanager.exception.BadRequestException;
import fr.umfds.spmanager.exception.ForbiddenException;
import fr.umfds.spmanager.exception.NotFoundException;
import fr.umfds.spmanager.model.Role;
import fr.umfds.spmanager.model.Subject;
import fr.umfds.spmanager.model.User;
import fr.umfds.spmanager.repository.SubjectRepository;
import fr.umfds.spmanager.repository.UserRepository;
import fr.umfds.spmanager.security.AuthenticatedUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;

    public SubjectService() {
        this.subjectRepository = new SubjectRepository();
        this.userRepository = new UserRepository();
    }

    public SubjectService(SubjectRepository subjectRepository, UserRepository userRepository) {
        this.subjectRepository = subjectRepository;
        this.userRepository = userRepository;
    }

    public List<SubjectResponse> getAllSubjects() {
        List<Subject> subjects = subjectRepository.findAll();
        List<SubjectResponse> list = new ArrayList<>();
        for (Subject s : subjects) {
            String teacherName = "";
            Optional<User> teacherOpt = userRepository.findById(s.getTeacherId());
            if (teacherOpt.isPresent()) {
                User teacher = teacherOpt.get();
                teacherName = teacher.getFirstName() + " " + teacher.getLastName();
            }
            SubjectResponse res = new SubjectResponse(s.getId(), s.getTitle(), s.getDescription(), s.getTeacherId(), teacherName.trim());
            list.add(res);
        }
        return list;
    }

    public SubjectResponse getSubjectById(int id) {
        Optional<Subject> subjectOpt = subjectRepository.findById(id);
        if (subjectOpt.isEmpty()) {
            throw new NotFoundException("Subject not found");
        }
        Subject s = subjectOpt.get();
        String teacherName = "";
        Optional<User> teacherOpt = userRepository.findById(s.getTeacherId());
        if (teacherOpt.isPresent()) {
            User teacher = teacherOpt.get();
            teacherName = teacher.getFirstName() + " " + teacher.getLastName();
        }
        SubjectResponse res = new SubjectResponse(s.getId(), s.getTitle(), s.getDescription(), s.getTeacherId(), teacherName.trim());
        return res;
    }

    public SubjectResponse createSubject(AuthenticatedUser currentUser, CreateSubjectRequest req) {
        if (currentUser.getRole() != Role.TEACHER) {
            throw new ForbiddenException("Only teachers can perform this action");
        }

        if (req == null) {
            throw new BadRequestException("Request body cannot be null");
        }

        if (req.title() == null || req.title().isBlank()) {
            throw new BadRequestException("Title is required");
        }

        if (req.description() == null || req.description().isBlank()) {
            throw new BadRequestException("Description is required");
        }

        Subject subject = new Subject();
        subject.setTitle(req.title().trim());
        subject.setDescription(req.description().trim());
        subject.setTeacherId(currentUser.getUserId());

        Subject saved = subjectRepository.save(subject);

        String teacherName = "";
        Optional<User> teacherOpt = userRepository.findById(saved.getTeacherId());
        if (teacherOpt.isPresent()) {
            User teacher = teacherOpt.get();
            teacherName = teacher.getFirstName() + " " + teacher.getLastName();
        }

        SubjectResponse res = new SubjectResponse(saved.getId(), saved.getTitle(), saved.getDescription(), saved.getTeacherId(), teacherName.trim());
        return res;
    }

    public SubjectResponse updateSubject(AuthenticatedUser currentUser, int id, CreateSubjectRequest req) {
        if (currentUser.getRole() != Role.TEACHER) {
            throw new ForbiddenException("Only teachers can perform this action");
        }

        if (req == null) {
            throw new BadRequestException("Request body cannot be null");
        }

        if (req.title() == null || req.title().isBlank()) {
            throw new BadRequestException("Title is required");
        }

        if (req.description() == null || req.description().isBlank()) {
            throw new BadRequestException("Description is required");
        }

        Optional<Subject> subjectOpt = subjectRepository.findById(id);
        if (subjectOpt.isEmpty()) {
            throw new NotFoundException("Subject not found");
        }

        Subject subject = subjectOpt.get();
        if (subject.getTeacherId() != currentUser.getUserId()) {
            throw new ForbiddenException("You can only modify your own subjects");
        }

        subject.setTitle(req.title().trim());
        subject.setDescription(req.description().trim());

        subjectRepository.update(subject);

        String teacherName = "";
        Optional<User> teacherOpt = userRepository.findById(subject.getTeacherId());
        if (teacherOpt.isPresent()) {
            User teacher = teacherOpt.get();
            teacherName = teacher.getFirstName() + " " + teacher.getLastName();
        }

        SubjectResponse res = new SubjectResponse(subject.getId(), subject.getTitle(), subject.getDescription(), subject.getTeacherId(), teacherName.trim());
        return res;
    }
}
