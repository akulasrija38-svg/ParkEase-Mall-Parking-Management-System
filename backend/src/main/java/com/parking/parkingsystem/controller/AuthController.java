
package com.parking.parkingsystem.controller;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.parking.parkingsystem.model.User;
import com.parking.parkingsystem.repository.UserRepository;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public record RegisterRequest(

            @NotBlank(message = "Name is required")
            @Pattern(
                regexp = "^[A-Za-z]{4,14}$",
                message = "Name must contain 4–14 letters only"
            )
            String name,

            @NotBlank(message = "Email is required")
            @Email(message = "Enter a valid email address")
            @Size(max = 254, message = "Email is too long")
            String email,

            @NotBlank(message = "Phone number is required")
            @Pattern(
                regexp = "^[6-9][0-9]{9}$",
                message = "Enter a valid 10-digit Indian mobile number starting with 6, 7, 8, or 9"
            )
            String phone,

            @NotBlank(message = "Password is required")
            @Size(
                min = 8,
                max = 64,
                message = "Password must contain 8–64 characters"
            )
            @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9\\s])\\S+$",
                message = "Password must contain uppercase, lowercase, a number, and a special character, without spaces"
            )
            String password
    ) {}

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> register(
            @Valid @RequestBody RegisterRequest request) {

        String name = request.name();
        String email = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);
        String phone = request.phone().trim();

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException(
                    "An account with this email already exists"
            );
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPhone(phone);
        user.setPassword(
                passwordEncoder.encode(request.password())
        );
        user.setRole("USER");

        userRepository.save(user);

        return Map.of(
                "message",
                "Registration successful. Please log in."
        );
    }

    @GetMapping("/me")
    public Map<String, String> me(Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException(
                    "Please log in first"
            );
        }

        User user = userRepository
                .findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException(
                        "User account not found"
                ));

        Map<String, String> result = new HashMap<>();
        result.put("name", user.getName());
        result.put("email", user.getEmail());
        result.put(
                "phone",
                user.getPhone() == null ? "" : user.getPhone()
        );
        result.put("role", user.getRole());

        return result;
    }
}
