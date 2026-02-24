package com.projeto.examcorrection.service;

import com.projeto.examcorrection.domain.Role;
import com.projeto.examcorrection.domain.User;
import com.projeto.examcorrection.dto.*;
import com.projeto.examcorrection.error.BusinessRuleException;
import com.projeto.examcorrection.error.ConflictException;
import com.projeto.examcorrection.error.ResourceNotFoundException;
import com.projeto.examcorrection.repository.UserRepository;
import com.projeto.examcorrection.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("EMAIL_ALREADY_EXISTS", "Já existe um usuário com este email.");
        }

        User user = new User();
        user.setNome(request.nome());
        user.setEmail(request.email());
        user.setSenhaHash(passwordEncoder.encode(request.senha()));
        user.setRole(request.role());
        user.setAtivo(true);
        user.setDataCriacao(Instant.now());

        user = userRepository.save(user);
        log.info("User registered: id={}, role={}", user.getId(), user.getRole());

        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        return new AuthResponse(token, user.getId(), user.getNome(), user.getEmail(), user.getRole());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessRuleException("INVALID_CREDENTIALS", "Email ou senha inválidos.",
                        org.springframework.http.HttpStatus.UNAUTHORIZED));

        if (!passwordEncoder.matches(request.senha(), user.getSenhaHash())) {
            throw new BusinessRuleException("INVALID_CREDENTIALS", "Email ou senha inválidos.",
                    org.springframework.http.HttpStatus.UNAUTHORIZED);
        }

        if (!user.isAtivo()) {
            throw new BusinessRuleException("USER_INACTIVE", "Usuário inativo.",
                    org.springframework.http.HttpStatus.FORBIDDEN);
        }

        log.info("User logged in: id={}", user.getId());
        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        return new AuthResponse(token, user.getId(), user.getNome(), user.getEmail(), user.getRole());
    }

    public UserResponse getMe(String userId) {
        User user = findById(userId);
        return toResponse(user);
    }

    public List<UserResponse> findAll() {
        return userRepository.findAll().stream().map(this::toResponse).toList();
    }

    public UserResponse findResponseById(String id) {
        return toResponse(findById(id));
    }

    public User findById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND", "Usuário não encontrado."));
    }

    public UserResponse create(UserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("EMAIL_ALREADY_EXISTS", "Já existe um usuário com este email.");
        }

        User user = new User();
        user.setNome(request.nome());
        user.setEmail(request.email());
        user.setSenhaHash(passwordEncoder.encode(request.senha() != null ? request.senha() : "changeme"));
        user.setRole(request.role());
        user.setAtivo(true);
        user.setDataCriacao(Instant.now());

        user = userRepository.save(user);
        log.info("User created: id={}", user.getId());
        return toResponse(user);
    }

    public UserResponse update(String id, UserRequest request) {
        User user = findById(id);
        user.setNome(request.nome());
        user.setEmail(request.email());
        user.setRole(request.role());
        if (request.senha() != null && !request.senha().isBlank()) {
            user.setSenhaHash(passwordEncoder.encode(request.senha()));
        }
        user = userRepository.save(user);
        log.info("User updated: id={}", user.getId());
        return toResponse(user);
    }

    public void delete(String id) {
        User user = findById(id);
        userRepository.delete(user);
        log.info("User deleted: id={}", id);
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getNome(), user.getEmail(), user.getRole(), user.isAtivo(),
                user.getDataCriacao());
    }
}
