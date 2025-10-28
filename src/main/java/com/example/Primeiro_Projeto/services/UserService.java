package com.example.Primeiro_Projeto.services;

import com.example.Primeiro_Projeto.config.LogMessages;
import com.example.Primeiro_Projeto.dtos.UserRequestDTO;
import com.example.Primeiro_Projeto.dtos.UserResponseDTO;
import com.example.Primeiro_Projeto.dtos.UserUpdateRequestDTO;
import com.example.Primeiro_Projeto.exceptions.ConflictException;
import com.example.Primeiro_Projeto.exceptions.ResourceNotFoundException;
import com.example.Primeiro_Projeto.model.Role;
import com.example.Primeiro_Projeto.model.User;
import com.example.Primeiro_Projeto.repositories.UserRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    public UserService(UserRepository userRepository, ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
    }

    @Cacheable(value = "allUsers")
    public List<UserResponseDTO> getAllUsers() {
        log.info(LogMessages.RESOURCE_LIST_ALL + " - " + LogMessages.CACHE_SAVED, "users");
        return userRepository.findAllByOrderByIdAsc()
                .stream()
                .map(UserResponseDTO::new)
                .toList();
    }

    @Cacheable(value = "usersPage", key = "#pageable.getPageNumber + ' - ' + #pageable.getPageSize")
    public Page<UserResponseDTO> getUserPaginated(Pageable pageable) {
        log.info(LogMessages.RESOURCE_LIST_ALL + " - " + LogMessages.CACHE_SAVED, "users");
        Page<User> usersPage = userRepository.findAll(pageable);

        return usersPage.map(UserResponseDTO::new);
    }

    @Cacheable(value = "user", key = "#id")
    public UserResponseDTO findUserById(Long id) {
        log.info(LogMessages.RESOURCE_FIND_BY_FIELD + " - " + LogMessages.CACHE_SAVED, "user", "id");
        return userRepository.findById(id)
                .map(UserResponseDTO::new)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    @CacheEvict(value = {"user", "allUsers", "usersPage"}, key = "#id")
    public UserResponseDTO updateUser(Long id, UserUpdateRequestDTO requestUpdate) {
        log.info(LogMessages.RESOURCE_UPDATE + " - " + LogMessages.CACHE_CLEANING, "user", id, "updateUser");
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn(LogMessages.RESOURCE_NOT_FOUND, "user", "id", id);
                    return new ResourceNotFoundException("User", id);
                });

        UserResponseDTO response = updateUserField(user, requestUpdate);
        log.info(LogMessages.OPERATION_SUCCESS, "updateUser");
        return response;
    }

    public UserResponseDTO updateMe(String currentUserEmail, UserUpdateRequestDTO requestUpdate) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> {
                    log.warn(LogMessages.RESOURCE_NOT_FOUND, "user", "email", currentUserEmail);
                    return new ResourceNotFoundException("User", "email", currentUserEmail);
                });


        UserResponseDTO response = updateUser(user.getId(), requestUpdate);
        return response;
    }

    @CacheEvict(value = {"allUsers", "usersPage"}, allEntries = true)
    public UserResponseDTO saveUser(UserRequestDTO request) {
        log.info(LogMessages.RESOURCE_CREATE, "user");
        User user = modelMapper.map(request, User.class);

        if(userRepository.existsByEmail(request.getEmail())) {
            log.warn(LogMessages.FIELD_CONFLICT, "email", request.getEmail());
            throw new ConflictException("email", request.getEmail());
        }

        user.getRoles().add(Role.USER);

        User userSaved = userRepository.save(user);
        log.info(LogMessages.OPERATION_SUCCESS, "saveUser");
        return new UserResponseDTO(userSaved);
    }

    @CacheEvict(value = {"user", "allUsers", "usersPage"}, key = "#id")
    public UserResponseDTO promoteToAdmin(Long id) {
        log.info(" Promovendo user {} para ADMIN - " + LogMessages.CACHE_CLEANING, id, "promoteToAdmin");
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("USer", id));

        if(user.getRoles().contains(Role.ADMIN)) {
            log.warn("user já é ADMIN");
            throw new ConflictException("User já é ADMIN");
        }

        user.getRoles().add(Role.ADMIN);
        User userUpdate = userRepository.save(user);
        log.info(LogMessages.OPERATION_SUCCESS, "promoteToAdmin");
        return new UserResponseDTO(userUpdate);
    }

    @CacheEvict(value = {"user", "allUsers", "usersPage"}, key = "#id")
    public UserResponseDTO removeFromAdmin(Long id) {
        log.info(" Removendo user {} de ADMIN - " + LogMessages.CACHE_CLEANING, id, "removeFromAdmin");
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("USer", id));

        if(!user.getRoles().contains(Role.ADMIN)) {
            log.warn("user não é ADMIN");
            throw new ConflictException("User não é ADMIN");
        }

        user.getRoles().remove(Role.ADMIN);
        User userUpdate = userRepository.save(user);
        log.info(LogMessages.OPERATION_SUCCESS, "removeFromAdmin");
        return new UserResponseDTO(userUpdate);
    }

    @CacheEvict(value = {"user", "allUsers", "usersPage"}, key = "#id")
    public void removeUser(Long id) {
        log.info(LogMessages.RESOURCE_DELETE + " - " + LogMessages.CACHE_CLEANING, "user", id, "removeUser");
        if(!userRepository.existsById(id)) {
            log.warn(LogMessages.RESOURCE_NOT_FOUND, "user", "id", id);
            throw new ResourceNotFoundException("User", id);
        }

        userRepository.deleteById(id);
        log.info(LogMessages.OPERATION_SUCCESS, "removeUser");
    }

    public void removeMe(String currentUserEmail) {
        if(!userRepository.existsByEmail(currentUserEmail)) {
            throw new ResourceNotFoundException("User", "email", currentUserEmail);
        }

        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> {
                    log.warn(LogMessages.RESOURCE_NOT_FOUND, "user", "email", currentUserEmail);
                    return new ResourceNotFoundException("User", "email", currentUserEmail);
                });

        removeUser(user.getId());
    }

    private UserResponseDTO updateUserField(User user, UserUpdateRequestDTO requestUpdate) {
        if(
                requestUpdate.getUsername() != null &&
                        !requestUpdate.getUsername().isEmpty() &&
                        !requestUpdate.getUsername().contains(user.getUsername())
        ) {
            log.info(LogMessages.FIELD_UPDATE, "username", requestUpdate.getUsername());
            user.setUsername(requestUpdate.getUsername());
        }

        if (
                requestUpdate.getEmail() != null &&
                        !requestUpdate.getEmail().isEmpty() &&
                        !requestUpdate.getEmail().contains(user.getEmail())
        ) {
            if(userRepository.existsByEmailAndIdNot(requestUpdate.getEmail(), user.getId())) {
                log.warn(LogMessages.FIELD_CONFLICT, "email", requestUpdate.getEmail());
                throw new ConflictException("email", requestUpdate.getEmail());
            }
            log.info(LogMessages.FIELD_UPDATE, "email", requestUpdate.getEmail());
            user.setEmail(requestUpdate.getEmail());
        }

        User userUpdate = userRepository.save(user);
        return new UserResponseDTO(userUpdate);
    }
}