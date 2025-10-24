package com.example.Primeiro_Projeto.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateRequestDTO {
    @Size(min = 3, max = 20, message = "username deve ter entre 3 e 20 caracteres")
    private String username;
    @Email(message = "formato de email inválido")
    private String email;
}