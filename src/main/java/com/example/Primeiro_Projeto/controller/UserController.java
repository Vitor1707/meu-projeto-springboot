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
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        log.info(" GET /api/users/all - Listar todos os users");
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<UserResponseDTO> findUserById(@PathVariable Long id) {
        log.info(" GET /api/users/id/{} - Buscar user por ID", id);
        return ResponseEntity.ok(userService.findUserById(id));
    }

    @PutMapping("/{id}/update")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable Long id, @RequestBody @Valid UserUpdateRequestDTO requestUpdate) {
        log.info(" PUT /api/users/{}/update - Atualizar user", id);
        UserResponseDTO response = userService.updateUser(id, requestUpdate);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping
    public ResponseEntity<UserResponseDTO> saveUser(@RequestBody @Valid UserRequestDTO request) {
        log.info(" POST /api/users - Criar user");
        UserResponseDTO response = userService.saveUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/id/{id}")
    public ResponseEntity<Void> removeUserById(@PathVariable Long id) {
        log.info(" DELETE /api/users/id/{} - Remover user por ID", id);
        userService.removeUser(id);
        return ResponseEntity.noContent().build();
    }
}