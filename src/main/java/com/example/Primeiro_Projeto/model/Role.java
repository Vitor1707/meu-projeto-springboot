package com.example.Primeiro_Projeto.model;

public enum Role {
    USER, ADMIN;

    public String getAuthority() {
        return this.name();
    }
}
