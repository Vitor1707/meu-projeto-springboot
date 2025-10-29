package com.example.Primeiro_Projeto.services;

import com.example.Primeiro_Projeto.config.LogMessages;
import com.example.Primeiro_Projeto.dtos.LoginRequestDTO;
import com.example.Primeiro_Projeto.dtos.UserRequestDTO;
import com.example.Primeiro_Projeto.dtos.UserResponseDTO;
import com.example.Primeiro_Projeto.exceptions.ConflictException;
import com.example.Primeiro_Projeto.exceptions.ResourceNotFoundException;
import com.example.Primeiro_Projeto.model.Role;
import com.example.Primeiro_Projeto.model.User;
import com.example.Primeiro_Projeto.repositories.UserRepository;
import com.example.Primeiro_Projeto.security.JwtService;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, ModelMapper modelMapper, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.modelMapper = modelMapper;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    @CacheEvict(value = {"allUsers", "usersPage"}, allEntries = true)
    public UserResponseDTO register(UserRequestDTO request) {
        log.info(LogMessages.RESOURCE_CREATE, "user");
        User user = modelMapper.map(request, User.class);

        if(userRepository.existsByEmail(user.getEmail())) {
            log.warn("email {} já está em uso", user.getEmail());
            throw new ConflictException("email", user.getEmail());
        }

        user.getRoles().add(Role.USER);

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        User userSaved = userRepository.save(user);
        log.info(LogMessages.OPERATION_SUCCESS, "saveUser");
        return new UserResponseDTO(userSaved);
    }

    public String login(LoginRequestDTO request) {
        log.info(" Fazendo login do user");
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("User {} não encontrado", request.getEmail());
                    return new ResourceNotFoundException("User", "email", request.getEmail());
                });

        String token = jwtService.generateToken(user.getEmail());
        log.info(" Login realizado com sucesso");
        return token;
    }
}
