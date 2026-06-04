package com.example.testmydev.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrations;
import org.springframework.security.saml2.provider.service.registration.InMemoryRelyingPartyRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${KEYCLOAK_HOST:http://localhost:8080}")
    private String keycloakHost;

    @Value("${KEYCLOAK_REALM:myrealm}")
    private String keycloakRealm;

    @Bean
    public RelyingPartyRegistrationRepository relyingPartyRegistrationRepository() {
        String metadataUri = keycloakHost + "/realms/" + keycloakRealm + "/protocol/saml/descriptor";

        RelyingPartyRegistration registration = RelyingPartyRegistrations
                .fromMetadataLocation(metadataUri)
                .registrationId("keycloak")
                .entityId("http://localhost:8087/testmydev")
                .assertionConsumerServiceLocation("http://localhost:8087/testmydev/login/saml2/sso/keycloak")
                .singleLogoutServiceLocation("http://localhost:8087/testmydev/logout/saml2/slo")
                .singleLogoutServiceResponseLocation("http://localhost:8087/testmydev/logout/saml2/slo")
                .build();

        return new InMemoryRelyingPartyRegistrationRepository(registration);
    }

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
