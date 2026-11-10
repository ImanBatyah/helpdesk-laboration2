
package com.helpdesk.config;

import com.helpdesk.model.User;
import com.helpdesk.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initializeUsers(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {

            String adminEmail = System.getenv("HELPDESK_ADMIN_EMAIL");
            String adminPassword = System.getenv("HELPDESK_ADMIN_PASSWORD");

            if (adminEmail != null && !adminEmail.isBlank()
                    && adminPassword != null && !adminPassword.isBlank()) {

                if (!userRepository.existsByEmail(adminEmail)) {

                    User admin = new User(
                            "Helpdesk Admin",
                            adminEmail,
                            passwordEncoder.encode(adminPassword),
                            User.Role.ADMIN
                    );

                    userRepository.save(admin);

                    System.out.println("Testadministratör skapad.");
                }
            }

            String agentEmail = System.getenv("HELPDESK_AGENT_EMAIL");
            String agentPassword = System.getenv("HELPDESK_AGENT_PASSWORD");

            if (agentEmail != null && !agentEmail.isBlank()
                    && agentPassword != null && !agentPassword.isBlank()) {

                if (!userRepository.existsByEmail(agentEmail)) {

                    User agent = new User(
                            "Helpdesk Agent",
                            agentEmail,
                            passwordEncoder.encode(agentPassword),
                            User.Role.AGENT
                    );

                    userRepository.save(agent);

                    System.out.println("Testsupportmedarbetare skapad.");
                }
            }
        };
    }
}
