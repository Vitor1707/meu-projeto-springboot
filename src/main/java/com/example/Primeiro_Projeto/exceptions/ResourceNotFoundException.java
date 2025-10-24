package com.example.Primeiro_Projeto.exceptions;

public class ResourceNotFoundException extends RuntimeException{

    public ResourceNotFoundException(String resourceName, Long id) {
        super(resourceName + " com ID '" + id + "' não encontrado");
    }

    public ResourceNotFoundException(String resourceName, String fieldName, String fieldValue) {
        super(resourceName + " com " + fieldName + " '" + fieldValue + "' não encontrado");
    }
}
