package com.example.Primeiro_Projeto.services;

import com.example.Primeiro_Projeto.dtos.UserRequestDTO;
import com.example.Primeiro_Projeto.dtos.UserResponseDTO;
import com.example.Primeiro_Projeto.dtos.UserUpdateRequestDTO;
import com.example.Primeiro_Projeto.model.Role;
import com.example.Primeiro_Projeto.model.User;
import com.example.Primeiro_Projeto.repositories.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;


    public UserService(UserRepository userRepository, ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
    }

    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserResponseDTO::new)
                .toList();
    }

    public UserResponseDTO findUserById(Long id) {
        return userRepository.findById(id)
                .map(UserResponseDTO::new)
                .orElseThrow(() -> new RuntimeException("User com ID '" + id + "' não encontrado"));
    }

    public UserResponseDTO updateUser(Long id, UserUpdateRequestDTO requestUpdate) {
        User user = userRepository.findById(id)
                .orElseThrow(() ->  new RuntimeException("User com ID '" + id + "' não encontrado"));

        return updateUserField(user, requestUpdate);
    }

    public UserResponseDTO saveUser(UserRequestDTO request) {
        User user = modelMapper.map(request, User.class);

        user.getRoles().add(Role.USER);

        User userSaved = userRepository.save(user);
        return new UserResponseDTO(userSaved);
    }

    public void removeUser(Long id) {
        if(!userRepository.existsById(id)) {
            throw new RuntimeException("User com ID '" + id + "' não encontrado");
        }

        userRepository.deleteById(id);
    }

    private UserResponseDTO updateUserField(User user, UserUpdateRequestDTO requestUpdate) {
        if(
                requestUpdate.getUsername() != null &&
                        !requestUpdate.getUsername().isEmpty() &&
                        !requestUpdate.getUsername().contains(user.getUsername())
        ) {
             user.setUsername(requestUpdate.getUsername());
        }

        if (
                requestUpdate.getEmail() != null &&
                        !requestUpdate.getEmail().isEmpty() &&
                        !requestUpdate.getEmail().contains(user.getEmail())
        ) {
            user.setEmail(requestUpdate.getEmail());
        }

        User userUpdate = userRepository.save(user);
        return new UserResponseDTO(userUpdate);
    }
}