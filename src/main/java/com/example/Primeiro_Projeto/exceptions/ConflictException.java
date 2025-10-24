package com.example.Primeiro_Projeto.exceptions;

public class ConflictException extends RuntimeException{

    public ConflictException(String message) {
        super(message);
    }

    public ConflictException(String fieldName, String feldValue) {
        super(fieldName + " '" + fieldName + "' já está em uso");
    }
}