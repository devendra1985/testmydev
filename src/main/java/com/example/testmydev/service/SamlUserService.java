package com.example.testmydev.service;

import com.example.testmydev.model.User;
import com.example.testmydev.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;
import org.springframework.security.saml2.provider.service.userdetails.Saml2UserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SamlUserService implements Saml2UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(SamlUserService.class);

    private final UserRepository userRepository;

    public SamlUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserBySaml2Authentication(Saml2AuthenticatedPrincipal principal) {
        String nameId = principal.getName();
        String registrationId = principal.getRelyingPartyRegistrationId();

        // Look up user by SAML NameID first
        Optional<User> existing = userRepository.findBySamlNameId(nameId);
        if (existing.isPresent()) {
            log.info("SAML login for existing user: {}", existing.get().getUsername());
            return buildUserDetails(existing.get());
        }

        // Auto-provision new user from SAML attributes
        String email = getFirstAttribute(principal, "email", "mail",
                "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress");
        String firstName = getFirstAttribute(principal, "firstName", "givenName",
                "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/givenname");
        String lastName = getFirstAttribute(principal, "lastName", "sn",
                "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/surname");

        String fullName = buildFullName(firstName, lastName, email, nameId);
        String username = deriveUsername(email, nameId);

        // If email already registered, link the SAML identity to that user
        if (email != null) {
            Optional<User> byEmail = userRepository.findByEmail(email);
            if (byEmail.isPresent()) {
                User user = byEmail.get();
                user.setSamlNameId(nameId);
                user.setSamlProvider(registrationId);
                userRepository.save(user);
                log.info("Linked SAML identity to existing user by email: {}", user.getUsername());
                return buildUserDetails(user);
            }
        }

        // Create brand-new user
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setName(fullName);
        newUser.setEmail(email != null ? email : nameId + "@saml.local");
        newUser.setSamlNameId(nameId);
        newUser.setSamlProvider(registrationId);
        // No password for SAML users

        userRepository.save(newUser);
        log.info("Auto-provisioned new user from SAML: {}", username);
        return buildUserDetails(newUser);
    }

    private UserDetails buildUserDetails(User user) {
        return new org.springframework.security.core.userdetails.User(
            user.getUsername(),
            user.getPassword() != null ? user.getPassword() : "{noop}saml-no-password",
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    private String getFirstAttribute(Saml2AuthenticatedPrincipal principal, String... attributeNames) {
        for (String attr : attributeNames) {
            List<Object> values = principal.getAttributes().get(attr);
            if (values != null && !values.isEmpty()) {
                return values.get(0).toString();
            }
        }
        return null;
    }

    private String buildFullName(String firstName, String lastName, String email, String nameId) {
        if (firstName != null && lastName != null) return firstName + " " + lastName;
        if (firstName != null) return firstName;
        if (email != null) return email.split("@")[0];
        return nameId;
    }

    private String deriveUsername(String email, String nameId) {
        String base = email != null ? email.split("@")[0] : nameId.replaceAll("[^a-zA-Z0-9]", "");
        String candidate = base;
        int suffix = 1;
        while (userRepository.existsByUsername(candidate)) {
            candidate = base + suffix++;
        }
        return candidate;
    }
}
