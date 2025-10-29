package com.example.Primeiro_Projeto.controller;

import com.example.Primeiro_Projeto.config.LogMessages;
import com.example.Primeiro_Projeto.dtos.UserRequestDTO;
import com.example.Primeiro_Projeto.dtos.UserResponseDTO;
import com.example.Primeiro_Projeto.dtos.UserUpdateRequestDTO;
import com.example.Primeiro_Projeto.services.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
        log.info(" GET /api/users/all - " + LogMessages.RESOURCE_LIST_ALL, "users");
        List<UserResponseDTO> response = userService.getAllUsers();
        log.info(" GET /api/users/all - " + LogMessages.OPERATION_SUCCESS, "getAllUsers");
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserResponseDTO>> getUsersPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        log.info(" GET /api/users - " + LogMessages.RESOURCE_LIST_ALL, "users");
        Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        Page<UserResponseDTO> response = userService.getUserPaginated(pageable);
        log.info(" GET /api/users - " + LogMessages.OPERATION_SUCCESS, "getUsersPaginated");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/id/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> findUserById(@PathVariable Long id) {
        log.info(" GET /api/users/id/{} - " + LogMessages.RESOURCE_FIND_BY_FIELD, id, "user", "id");
        UserResponseDTO response = userService.findUserById(id);
        log.info(" GET /api/users/id/{} - " + LogMessages.OPERATION_SUCCESS, id, "findUserById");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/update")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable Long id, @RequestBody @Valid UserUpdateRequestDTO requestUpdate) {
        log.info(" PUT /api/users/{}/update - " + LogMessages.RESOURCE_UPDATE, id, "user", "id");
        UserResponseDTO response = userService.updateUser(id, requestUpdate);
        log.info(" PUT /api/user/{}/update - " + LogMessages.OPERATION_SUCCESS, id, "updateMe");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/update/me")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserResponseDTO> updateMe(@AuthenticationPrincipal UserDetails userDetails, @RequestBody @Valid UserUpdateRequestDTO requestUpdate) {
        log.info(" PUT /api/user/update/me - " + LogMessages.RESOURCE_UPDATE, "user", "email");
        String currentUserEmail = userDetails.getUsername();

        UserResponseDTO response = userService.updateMe(currentUserEmail, requestUpdate);
        log.info(" PUT /api/user/update/me - " + LogMessages.OPERATION_SUCCESS, "updateMe");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("{id_User}/add_product/{id_Product}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserResponseDTO> addProductForUser(@PathVariable Long id_User, @PathVariable Long id_Product) {
        log.info(" POST /api/users/{}/product/{} - Adicionado Product ao User", id_User, id_Product);
        UserResponseDTO response = userService.addProductForUser(id_User, id_Product);
        log.info(" POST /api/users/{}/product/{} - " + LogMessages.OPERATION_SUCCESS, id_User, id_Product, "addProductForUser");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/promote_to_admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> promoteToAdmin(@PathVariable Long id) {
        log.info(" PUT /api/users/{}/promote_to_admin - Promover user para ADMIN", id);
        UserResponseDTO response = userService.promoteToAdmin(id);
        log.info(" PUT /api/users/{}/promote_to_admin - " + LogMessages.OPERATION_SUCCESS, id, "promoteToAdmin");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/id/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> removeUserById(@PathVariable Long id) {
        log.info("DELETE /api/users/id/{} - " + LogMessages.RESOURCE_DELETE, id, "user", "id");
        userService.removeUser(id);
        log.info("DELETE /api/users/id/{} - " + LogMessages.OPERATION_SUCCESS, id, "removeUserById");
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> removeMe(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("DELETE /api/users/me - " + LogMessages.RESOURCE_DELETE, "user", "email");
        String currentUserEmail = userDetails.getUsername();
        userService.removeMe(currentUserEmail);
        log.info("DELETE /api/users/me - " + LogMessages.OPERATION_SUCCESS, "removeMe");
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/remove_from_admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> removeFromAdmin(@PathVariable Long id) {
        log.info(" DELETE /api/users/{}/remove_from_admin - Remover user de ADMIN", id);
        UserResponseDTO response = userService.removeFromAdmin(id);
        log.info(" DELETE /api/users/{}/remove_from_admin - " + LogMessages.OPERATION_SUCCESS, id, "removeFromAdmin");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("{id_User}/remove_product/{id_Product}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserResponseDTO> removeProductFromUser(@PathVariable Long id_User, @PathVariable Long id_Product) {
        log.info(" DELETE /api/users/{}/product/{} - Removendo Product do User", id_User, id_Product);
        UserResponseDTO response = userService.removeProductFromUser(id_User, id_Product);
        log.info(" DELETE /api/users/{}/product/{} - " + LogMessages.OPERATION_SUCCESS, id_User, id_Product, "removeProductFromUser");
        return ResponseEntity.ok(response);
    }
}