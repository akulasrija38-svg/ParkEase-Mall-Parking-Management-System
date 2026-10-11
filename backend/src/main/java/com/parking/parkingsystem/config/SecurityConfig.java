package com.parking.parkingsystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
            .csrf(csrf -> csrf.disable())

            .authorizeHttpRequests(auth -> auth

                // Admin-only operations
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/malls",
                    "/api/malls/*/slots"
                ).hasRole("ADMIN")

                .requestMatchers("/api/admin/**")
                    .hasRole("ADMIN")

                // Public pages and endpoints
                .requestMatchers(
                    "/",
                    "/index.html",
                    "/style.css",
                    "/app.js",
                    "/favicon.ico",
                    "/favicon.svg",
                    "/error",
                    "/api/health",
                    "/api/auth/register",
                    "/api/auth/login",
                    "/api/malls",
                    "/api/malls/*/slots"
                ).permitAll()

                // Authenticated endpoints
                .requestMatchers("/api/auth/me")
                    .authenticated()

                .requestMatchers("/api/bookings/**")
                    .authenticated()

                .requestMatchers("/api/malls/**")
                    .authenticated()

                // Reject all other unmatched requests
                .anyRequest().denyAll()
            )

            .formLogin(form -> form
                .loginProcessingUrl("/api/auth/login")
                .usernameParameter("email")
                .passwordParameter("password")

                .successHandler((request, response, authentication) -> {
                    response.setStatus(200);
                    response.setContentType(
                        MediaType.APPLICATION_JSON_VALUE);
                    response.getWriter().write(
                        "{\"message\":\"Login successful\"}");
                })

                .failureHandler((request, response, exception) -> {
                    response.setStatus(401);
                    response.setContentType(
                        MediaType.APPLICATION_JSON_VALUE);
                    response.getWriter().write(
                        "{\"error\":\"Invalid email or password\"}");
                })

                .permitAll()
            )

            .logout(logout -> logout
                .logoutUrl("/api/auth/logout")
                .logoutSuccessHandler(
                    (request, response, authentication) -> {
                        response.setStatus(200);
                        response.setContentType(
                            MediaType.APPLICATION_JSON_VALUE);
                        response.getWriter().write(
                            "{\"message\":\"Logout successful\"}");
                    })
            );

        return http.build();
    }
}
