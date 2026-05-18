package com.investagg.repository;

import com.investagg.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private UserRepository userRepository;

    private User persistUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setPassword("hashed");
        return em.persistFlushFind(user);
    }

    @Test
    void findByEmailAndDeletedAtIsNull_found() {
        persistUser("alice@example.com");

        Optional<User> result = userRepository.findByEmailAndDeletedAtIsNull("alice@example.com");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    void findByEmailAndDeletedAtIsNull_notFound_whenEmailDiffers() {
        persistUser("bob@example.com");

        Optional<User> result = userRepository.findByEmailAndDeletedAtIsNull("other@example.com");

        assertThat(result).isEmpty();
    }

    @Test
    void findByEmailAndDeletedAtIsNull_notFound_whenSoftDeleted() {
        User user = persistUser("charlie@example.com");
        user.setDeletedAt(java.time.OffsetDateTime.now());
        em.persistAndFlush(user);

        Optional<User> result = userRepository.findByEmailAndDeletedAtIsNull("charlie@example.com");

        assertThat(result).isEmpty();
    }

    @Test
    void existsByEmailAndDeletedAtIsNull_trueForActiveUser() {
        persistUser("dana@example.com");

        assertThat(userRepository.existsByEmailAndDeletedAtIsNull("dana@example.com")).isTrue();
    }

    @Test
    void existsByEmailAndDeletedAtIsNull_falseForSoftDeleted() {
        User user = persistUser("eve@example.com");
        user.setDeletedAt(java.time.OffsetDateTime.now());
        em.persistAndFlush(user);

        assertThat(userRepository.existsByEmailAndDeletedAtIsNull("eve@example.com")).isFalse();
    }
}
