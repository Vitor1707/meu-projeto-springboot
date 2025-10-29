package com.example.Primeiro_Projeto.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductFromUserDTO {
    private Long id;
    private String name;
    private Double price;
}