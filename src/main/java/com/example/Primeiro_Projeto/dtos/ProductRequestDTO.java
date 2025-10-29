package com.example.Primeiro_Projeto.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductRequestDTO {
    @NotBlank(message = "name é obrigatório")
    @Size(min =3, max = 50, message = "name deve ter entre 3 e 50 caracteres")
    private String name;
    @NotNull(message = "price é obrigatório")
    @Min(value = 0)
    private Double price;
}