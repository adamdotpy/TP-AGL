package fr.umfds.spmanager.service;

import fr.umfds.spmanager.dto.LoginRequest;
import fr.umfds.spmanager.dto.LoginResponse;
import fr.umfds.spmanager.exception.BadRequestException;
import fr.umfds.spmanager.exception.UnauthorizedException;
import fr.umfds.spmanager.model.User;
import fr.umfds.spmanager.repository.UserRepository;
import fr.umfds.spmanager.security.JwtUtil;
import fr.umfds.spmanager.security.PasswordHasher;

import java.util.Optional;

public class AuthService {

    private final UserRepository userRepository;

    public AuthService() {
        this.userRepository = new UserRepository();
    }

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public LoginResponse login(LoginRequest request) {
        if (request == null || request.login() == null || request.login().isBlank()) {
            throw new BadRequestException("Login is required");
        }
        if (request.password() == null || request.password().isBlank()) {
            throw new BadRequestException("Password is required");
        }

        Optional<User> userOptional = userRepository.findByLogin(request.login().trim());
        if (userOptional.isEmpty()) {
            throw new UnauthorizedException("Invalid login or password");
        }

        User user = userOptional.get();
        boolean passwordMatches = PasswordHasher.checkPassword(request.password(), user.getPasswordHash());
        if (!passwordMatches) {
            throw new UnauthorizedException("Invalid login or password");
        }

        String token = JwtUtil.generateToken(user.getId(), user.getLogin(), user.getRole());

        return new LoginResponse(
                token,
                user.getId(),
                user.getRole(),
                user.getFirstName(),
                user.getLastName()
        );
    }
}
