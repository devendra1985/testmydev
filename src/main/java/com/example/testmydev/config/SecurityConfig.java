package com.example.testmydev.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/login.html", "/signup.html",
                    "/api/auth/signup", "/api/auth/login",
                    "/actuator/health", "/actuator/**",
                    "/error"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .saml2Login(saml -> saml
                .loginPage("/login.html")
                .defaultSuccessUrl("/dashboard.html", true)
            )
            .saml2Logout(Customizer.withDefaults())
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/auth/**")
            );

        return http.build();
    }
}
