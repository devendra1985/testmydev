package com.example.testmydev.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.saml2.provider.service.registration.InMemoryRelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.Saml2MessageBinding;
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
        String idpBase = keycloakHost + "/realms/" + keycloakRealm + "/protocol/saml";

        RelyingPartyRegistration registration = RelyingPartyRegistration
                .withRegistrationId("keycloak")
                .entityId("{baseUrl}/testmydev")
                .assertionConsumerServiceLocation("{baseUrl}/login/saml2/sso/{registrationId}")
                .assertionConsumerServiceBinding(Saml2MessageBinding.POST)
                .singleLogoutServiceLocation("{baseUrl}/logout/saml2/slo")
                .singleLogoutServiceResponseLocation("{baseUrl}/logout/saml2/slo")
                .singleLogoutServiceBinding(Saml2MessageBinding.POST)
                .assertingPartyDetails(party -> party
                        .entityId(keycloakHost + "/realms/" + keycloakRealm)
                        .singleSignOnServiceLocation(idpBase)
                        .singleSignOnServiceBinding(Saml2MessageBinding.POST)
                        .singleLogoutServiceLocation(idpBase)
                        .singleLogoutServiceBinding(Saml2MessageBinding.POST)
                        .wantAuthnRequestsSigned(false)
                )
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
