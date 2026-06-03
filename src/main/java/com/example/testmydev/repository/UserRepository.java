package com.example.testmydev.repository;

import com.example.testmydev.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findBySamlNameId(String samlNameId);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
