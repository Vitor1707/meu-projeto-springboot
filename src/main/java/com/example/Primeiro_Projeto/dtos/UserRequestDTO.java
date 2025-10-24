package com.example.Primeiro_Projeto.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRequestDTO {
    @NotBlank(message = "username é obrigatório")
    @Size(min = 3, max = 20, message = "username deve ter entre 3 e 20 caracteres")
    private String username;
    @NotBlank(message = "email é obrigatório")
    @Email(message = "formato de email inválido")
    private String email;
}