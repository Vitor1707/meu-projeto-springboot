package com.example.Primeiro_Projeto.services;

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

    public List<UserResponseDTO> getAllUsers() {
        log.info(" Listando todos os users");
        return userRepository.findAll()
                .stream()
                .map(UserResponseDTO::new)
                .toList();
    }

    public UserResponseDTO findUserById(Long id) {
        log.info(" Listando user por ID: {}", id);
        return userRepository.findById(id)
                .map(UserResponseDTO::new)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    public UserResponseDTO updateUser(Long id, UserUpdateRequestDTO requestUpdate) {
        log.info(" Atualizando user com ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn(" user {} não encontrado", id);
                    return new ResourceNotFoundException("User", id);
                });

        UserResponseDTO response = updateUserField(user, requestUpdate);
        log.info(" User {} atualizado com sucesso", response.getEmail());
        return response;
    }

    public UserResponseDTO updateMe(String currentUserEmail, UserUpdateRequestDTO requestUpdate) {
        log.info(" Atualizando meu perfil User");
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> {
                    log.warn(" Email {} já está em uso", currentUserEmail);
                    return new ResourceNotFoundException("User", "email", currentUserEmail);
                });


        UserResponseDTO response = updateUserField(user, requestUpdate);
        log.info(" Perfil atualizado com sucesso");
        return response;
    }

    public UserResponseDTO saveUser(UserRequestDTO request) {
        log.info(" Criando user");
        User user = modelMapper.map(request, User.class);

        if(userRepository.existsByEmail(request.getEmail())) {
            log.warn(" Email {} já está em uso", request.getEmail());
            throw new ConflictException("email", request.getEmail());
        }

        user.getRoles().add(Role.USER);

        User userSaved = userRepository.save(user);
        log.info(" User {} criado com sucesso", userSaved.getEmail());
        return new UserResponseDTO(userSaved);
    }

    public UserResponseDTO promoteToAdmin(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("USer", id));

        if(user.getRoles().contains(Role.ADMIN)) {
            throw new ConflictException("User já é ADMIN");
        }

        user.getRoles().add(Role.ADMIN);
        User userUpdate = userRepository.save(user);
        return new UserResponseDTO(userUpdate);
    }

    public UserResponseDTO removeFromAdmin(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("USer", id));

        if(!user.getRoles().contains(Role.ADMIN)) {
            throw new ConflictException("User não é ADMIN");
        }

        user.getRoles().remove(Role.ADMIN);
        User userUpdate = userRepository.save(user);
        return new UserResponseDTO(userUpdate);
    }


    public void removeUser(Long id) {
        log.info(" Removendo User {}", id);
        if(!userRepository.existsById(id)) {
            log.warn("User {} não encontrado", id);
            throw new ResourceNotFoundException("User", id);
        }

        userRepository.deleteById(id);
        log.info(" User {} removido com sucesso", id);
    }

    private UserResponseDTO updateUserField(User user, UserUpdateRequestDTO requestUpdate) {
        if(
                requestUpdate.getUsername() != null &&
                        !requestUpdate.getUsername().isEmpty() &&
                        !requestUpdate.getUsername().contains(user.getUsername())
        ) {
            log.info("username modificado para {}", requestUpdate.getUsername());
            user.setUsername(requestUpdate.getUsername());
        }

        if (
                requestUpdate.getEmail() != null &&
                        !requestUpdate.getEmail().isEmpty() &&
                        !requestUpdate.getEmail().contains(user.getEmail())
        ) {
            if(userRepository.existsByEmailAndIdNot(requestUpdate.getEmail(), user.getId())) {
                log.warn("email {} já está em uso", requestUpdate.getEmail());
                throw new ConflictException("email", requestUpdate.getEmail());
            }
            log.info("email modificado para {}", requestUpdate.getEmail());
            user.setEmail(requestUpdate.getEmail());
        }

        User userUpdate = userRepository.save(user);
        return new UserResponseDTO(userUpdate);
    }
}