package fr.umfds.spmanager.service;

import fr.umfds.spmanager.dto.CreateUserRequest;
import fr.umfds.spmanager.dto.UserResponse;
import fr.umfds.spmanager.exception.BadRequestException;
import fr.umfds.spmanager.exception.ForbiddenException;
import fr.umfds.spmanager.model.Role;
import fr.umfds.spmanager.model.User;
import fr.umfds.spmanager.repository.UserRepository;
import fr.umfds.spmanager.security.AuthenticatedUser;
import fr.umfds.spmanager.security.PasswordHasher;

import java.util.ArrayList;
import java.util.List;

public class UserService {

    private final UserRepository userRepository;

    public UserService() {
        this.userRepository = new UserRepository();
    }

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse createTeacher(AuthenticatedUser currentUser, CreateUserRequest req) {
        if (currentUser.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Only administrators can perform this action");
        }

        if (req.login() == null || req.login().isBlank()) {
            throw new BadRequestException("Login is required");
        }
        if (req.password() == null || req.password().isBlank()) {
            throw new BadRequestException("Password is required");
        }

        if (userRepository.findByLogin(req.login().trim()).isPresent()) {
            throw new BadRequestException("Login already exists");
        }

        String passwordHash = PasswordHasher.hashPassword(req.password().trim());
        User teacher = new User(
                req.login().trim(),
                passwordHash,
                Role.TEACHER,
                req.firstName() != null ? req.firstName().trim() : "",
                req.lastName() != null ? req.lastName().trim() : ""
        );

        User saved = userRepository.save(teacher);
        return new UserResponse(
                saved.getId(),
                saved.getLogin(),
                saved.getRole(),
                saved.getFirstName(),
                saved.getLastName()
        );
    }

    public UserResponse createStudent(AuthenticatedUser currentUser, CreateUserRequest req) {
        if (currentUser.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Only administrators can perform this action");
        }

        if (req.login() == null || req.login().isBlank()) {
            throw new BadRequestException("Login is required");
        }
        if (req.password() == null || req.password().isBlank()) {
            throw new BadRequestException("Password is required");
        }

        if (userRepository.findByLogin(req.login().trim()).isPresent()) {
            throw new BadRequestException("Login already exists");
        }

        String passwordHash = PasswordHasher.hashPassword(req.password().trim());
        User student = new User(
                req.login().trim(),
                passwordHash,
                Role.STUDENT,
                req.firstName() != null ? req.firstName().trim() : "",
                req.lastName() != null ? req.lastName().trim() : ""
        );

        User saved = userRepository.save(student);
        return new UserResponse(
                saved.getId(),
                saved.getLogin(),
                saved.getRole(),
                saved.getFirstName(),
                saved.getLastName()
        );
    }

    public List<UserResponse> getAllTeachers() {
        List<User> teachers = userRepository.findAllByRole(Role.TEACHER);
        List<UserResponse> responses = new ArrayList<>();
        for (User u : teachers) {
            responses.add(new UserResponse(
                    u.getId(),
                    u.getLogin(),
                    u.getRole(),
                    u.getFirstName(),
                    u.getLastName()
            ));
        }
        return responses;
    }

    public List<UserResponse> getAllStudents() {
        List<User> students = userRepository.findAllByRole(Role.STUDENT);
        List<UserResponse> responses = new ArrayList<>();
        for (User u : students) {
            responses.add(new UserResponse(
                    u.getId(),
                    u.getLogin(),
                    u.getRole(),
                    u.getFirstName(),
                    u.getLastName()
            ));
        }
        return responses;
    }
}
