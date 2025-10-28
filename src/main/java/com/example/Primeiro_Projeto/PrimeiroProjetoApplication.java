package com.example.Primeiro_Projeto;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;


@SpringBootApplication
@EnableCaching
public class PrimeiroProjetoApplication {

    public static void main(String[] args) {
		SpringApplication.run(PrimeiroProjetoApplication.class, args);
	}
}
