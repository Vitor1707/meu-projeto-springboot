package com.example.Primeiro_Projeto;

import com.example.Primeiro_Projeto.model.User;
import com.example.Primeiro_Projeto.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class PrimeiroProjetoApplication {

	public static void main(String[] args) {
		SpringApplication.run(PrimeiroProjetoApplication.class, args);
	}

    @Bean
    CommandLineRunner init(UserRepository userRepository) {
        return args -> {
            Logger log = LoggerFactory.getLogger(PrimeiroProjetoApplication.class);

            log.info("Criando 50 users automaticamente");

            for(int i = 1; i <= 50; i++) {
                User user = new User("User " + i, "user" + i + "@email.com");
                userRepository.save(user);
            }

            log.info("Users criados");
        };
    }
}
