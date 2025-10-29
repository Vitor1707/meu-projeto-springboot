package com.example.Primeiro_Projeto.dtos;

import com.example.Primeiro_Projeto.model.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponseDTO {
    private Long id;
    private String name;
    private Double price;
    private List<UserFromProductDTO> users = new ArrayList<>();

    public ProductResponseDTO(Product product) {
        this.id = product.getId();
        this.name = product.getName();
        this.price = product.getPrice();
        if (product.getUsers() != null) {
            this.users = product.getUsers()
                    .stream()
                    .map(user -> new UserFromProductDTO(user.getId(), user.getUsername()))
                    .collect(Collectors.toList());
        }
    }
}