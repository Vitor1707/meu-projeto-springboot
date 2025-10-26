package com.example.Primeiro_Projeto.controller;

import com.example.Primeiro_Projeto.dtos.UserRequestDTO;
import com.example.Primeiro_Projeto.dtos.UserResponseDTO;
import com.example.Primeiro_Projeto.dtos.UserUpdateRequestDTO;
import com.example.Primeiro_Projeto.services.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        log.info(" GET /api/users/all - Listar todos os users");
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/id/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> findUserById(@PathVariable Long id) {
        log.info(" GET /api/users/id/{} - Buscar user por ID", id);
        return ResponseEntity.ok(userService.findUserById(id));
    }

    @PutMapping("/{id}/update")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable Long id, @RequestBody @Valid UserUpdateRequestDTO requestUpdate) {
        log.info(" PUT /api/users/{}/update - Atualizar user", id);
        UserResponseDTO response = userService.updateUser(id, requestUpdate);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/update/me")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserResponseDTO> updateMe(@AuthenticationPrincipal UserDetails userDetails, @RequestBody @Valid UserUpdateRequestDTO requestUpdate) {
        log.info(" PUT /api/user/update/me - Atualizar meu Perfil");
        String currentUserEmail = userDetails.getUsername();

        UserResponseDTO response = userService.updateMe(currentUserEmail, requestUpdate);
        log.info(" PUT /api/user/update/me - Perfil Atualizado com Sucesso");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> saveUser(@RequestBody @Valid UserRequestDTO request) {
        log.info(" POST /api/users - Criar user");
        UserResponseDTO response = userService.saveUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/promote_to_admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> promoteToAdmin(@PathVariable Long id) {
        UserResponseDTO response = userService.promoteToAdmin(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/id/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> removeUserById(@PathVariable Long id) {
        log.info(" DELETE /api/users/id/{} - Remover user por ID", id);
        userService.removeUser(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/remove_from_admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> removeFromAdmin(@PathVariable Long id) {
        return ResponseEntity.ok(userService.removeFromAdmin(id));
    }
}