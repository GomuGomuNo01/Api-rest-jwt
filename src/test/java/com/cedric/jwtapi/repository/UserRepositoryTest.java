package com.cedric.jwtapi.repository;

import com.cedric.jwtapi.entity.Role;
import com.cedric.jwtapi.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:repotest;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User saveAlice() {
        return userRepository.save(User.builder()
                .username("alice")
                .email("alice@example.com")
                .password("encodedPass")
                .role(Role.ROLE_USER)
                .build());
    }

    @Test
    void findByUsername_existingUser_shouldReturnUser() {
        saveAlice();
        Optional<User> result = userRepository.findByUsername("alice");
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("alice");
        assertThat(result.get().getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    void findByUsername_unknownUser_shouldReturnEmpty() {
        assertThat(userRepository.findByUsername("unknown")).isEmpty();
    }

    @Test
    void findByEmail_existingEmail_shouldReturnUser() {
        saveAlice();
        Optional<User> result = userRepository.findByEmail("alice@example.com");
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("alice");
    }

    @Test
    void findByEmail_unknownEmail_shouldReturnEmpty() {
        assertThat(userRepository.findByEmail("nope@example.com")).isEmpty();
    }

    @Test
    void existsByUsername_existing_shouldReturnTrue() {
        saveAlice();
        assertThat(userRepository.existsByUsername("alice")).isTrue();
    }

    @Test
    void existsByUsername_unknown_shouldReturnFalse() {
        assertThat(userRepository.existsByUsername("nobody")).isFalse();
    }

    @Test
    void existsByEmail_existing_shouldReturnTrue() {
        saveAlice();
        assertThat(userRepository.existsByEmail("alice@example.com")).isTrue();
    }

    @Test
    void existsByEmail_unknown_shouldReturnFalse() {
        assertThat(userRepository.existsByEmail("nope@example.com")).isFalse();
    }
}
