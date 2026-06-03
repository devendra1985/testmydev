package com.example.testmydev.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = true)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column
    private String address;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "saml_name_id")
    private String samlNameId;

    @Column(name = "saml_provider")
    private String samlProvider;

    public User() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getSamlNameId() { return samlNameId; }
    public void setSamlNameId(String samlNameId) { this.samlNameId = samlNameId; }

    public String getSamlProvider() { return samlProvider; }
    public void setSamlProvider(String samlProvider) { this.samlProvider = samlProvider; }
}