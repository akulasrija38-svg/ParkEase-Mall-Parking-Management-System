
package com.parking.parkingsystem.config;

import com.parking.parkingsystem.model.User;
import com.parking.parkingsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminAccountInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${parkease.admin.email:}")
    private String adminEmail;

    @Value("${parkease.admin.password:}")
    private String adminPassword;

    @Value("${parkease.admin.name:ParkEase Admin}")
    private String adminName;

    public AdminAccountInitializer(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (adminEmail.isBlank() || adminPassword.isBlank()) {
            System.out.println("Admin bootstrap skipped: credentials not configured.");
            return;
        }

        if (adminPassword.length() < 12) {
            throw new IllegalStateException(
                "Admin password must contain at least 12 characters."
            );
        }

        String email = adminEmail.trim().toLowerCase();

        var existing = userRepository.findByEmailIgnoreCase(email);

        if (existing.isPresent()) {
            User admin = existing.get();
            admin.setName(adminName);
            admin.setEmail(email);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRole("ADMIN");
            userRepository.save(admin);
            System.out.println("Existing account updated as ADMIN with a new password.");
            return;
        }

        User admin = new User();
        admin.setName(adminName);
        admin.setEmail(email);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setRole("ADMIN");
        userRepository.save(admin);
        System.out.println("ParkEase admin account created successfully.");
    }
}
