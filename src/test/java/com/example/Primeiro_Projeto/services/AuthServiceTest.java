package com.example.Primeiro_Projeto.services;

import com.example.Primeiro_Projeto.dtos.LoginRequestDTO;
import com.example.Primeiro_Projeto.dtos.UserRequestDTO;
import com.example.Primeiro_Projeto.dtos.UserResponseDTO;
import com.example.Primeiro_Projeto.exceptions.ConflictException;
import com.example.Primeiro_Projeto.exceptions.ResourceNotFoundException;
import com.example.Primeiro_Projeto.model.Role;
import com.example.Primeiro_Projeto.model.User;
import com.example.Primeiro_Projeto.repositories.UserRepository;
import com.example.Primeiro_Projeto.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.context.support.BeanDefinitionDsl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthService authService;

    private User user;

    private UserRequestDTO request;

    private LoginRequestDTO requestLogin;

    @BeforeEach
    void setUp() {
        user = new User(1L, "User", "user@email.com", "123456", List.of(Role.USER), new ArrayList<>());
        request = new UserRequestDTO("Novo User", "novo_user@email.com", "123456");
        requestLogin = new LoginRequestDTO("user@email.com", "123456");
    }

    @Test
    void deveSalvarUserSeTodosOsDadosForemValidos() {
        when(modelMapper.map(request, User.class)).thenReturn(new User(null, "Novo User", "novo_user@email.com", "123456", new ArrayList<>(), new ArrayList<>()));
        when(userRepository.existsByEmail("novo_user@email.com")).thenReturn(false);
        when(passwordEncoder.encode("123456")).thenReturn("senha_criptografada");
        when(userRepository.save(any(User.class))).thenReturn(new User(1L, "Novo User", "novo_user@email.com", "senha_criptografada", List.of(Role.USER), new ArrayList<>()));

        UserResponseDTO result = authService.register(request);

        assertNotNull(result);
        assertEquals("Novo User", result.getUsername());
        assertEquals("novo_user@email.com", result.getEmail());

        verify(modelMapper).map(request, User.class);
        verify(userRepository).existsByEmail("novo_user@email.com");
        verify(passwordEncoder).encode("123456");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void deveLancarExcecaoAoCriarUserESeuEmailJaEstiverEmUso() {
        when(modelMapper.map(request, User.class)).thenReturn(new User(null, "Novo User", "novo_user@email.com", "123456", new ArrayList<>(), new ArrayList<>()));
        when(userRepository.existsByEmail("novo_user@email.com")).thenReturn(true);

        ConflictException conflict = assertThrows(ConflictException.class, () -> authService.register(request));

        assertTrue(conflict.getMessage().contains("email"));

        verify(modelMapper).map(request, User.class);
        verify(userRepository).existsByEmail("novo_user@email.com");
    }

    @Test
    void deveRetornarUmTokenCasoDadosDoLoginSejamValidos() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);

        when(userRepository.findByEmail("user@email.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken("user@email.com")).thenReturn("token.jwt.generate");

        String result = authService.login(requestLogin);

        assertNotNull(result);
        assertEquals("token.jwt.generate", result);

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail("user@email.com");
        verify(jwtService).generateToken("user@email.com");
    }

    @Test
    void deveLancarExcecaoAoFazerLoginEEmailNaoExistir() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);

        when(userRepository.findByEmail("user@email.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.login(requestLogin));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail("user@email.com");
    }
}