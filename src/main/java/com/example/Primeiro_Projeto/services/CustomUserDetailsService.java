package com.example.Primeiro_Projeto.services;

import com.example.Primeiro_Projeto.model.Role;
import com.example.Primeiro_Projeto.model.User;
import com.example.Primeiro_Projeto.repositories.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User com email '" + username + "' não encontrado"));

        List<String> rolesStr = user.getRoles()
                .stream()
                .map(Role::getAuthority)
                .toList();

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles(rolesStr.toArray(new String[0]))
                .build();
    }
}