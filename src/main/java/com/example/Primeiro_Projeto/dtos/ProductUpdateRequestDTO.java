package com.example.Primeiro_Projeto.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductUpdateRequestDTO {
    @Size(min =3, max = 50, message = "name deve ter entre 3 e 50 caracteres")
    private String name;
    @Min(value = 0)
    private Double price;
}
