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
public class LoginRequestDTO {
    @NotBlank(message = "email é obrigatório")
    @Email(message = "formato de email inválido")
    private String email;
    @NotBlank(message = "password é obrigatória")
    @Size(min = 5, message = "password deve ter no mínimo 6 caracteres")
    private String password;
}