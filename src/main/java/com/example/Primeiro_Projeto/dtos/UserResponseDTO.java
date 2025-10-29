package com.example.Primeiro_Projeto.dtos;

import com.example.Primeiro_Projeto.model.Role;
import com.example.Primeiro_Projeto.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDTO {
    private Long id;
    private String username;
    private String email;
    private List<Role> roles = new ArrayList<>();
    private List<ProductFromUserDTO> products = new ArrayList<>();

    public UserResponseDTO(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.roles = user.getRoles();
        if(user.getProducts() != null) {
            this.products = user.getProducts()
                    .stream()
                    .map(product -> new ProductFromUserDTO(product.getId(), product.getName(), product.getPrice()))
                    .collect(Collectors.toList());
        }
    }
}