package project;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import project.enums.Role;
import project.model.User;
import project.repository.user.UserRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class UserRepositoryIntegrationTest {
    @Autowired
    private UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void findsUserByEmail() {
        User savedUser = saveUser("Sergei@test.com", 100);
        entityManager.clear();

        Optional<User> foundUser =
                userRepository.findByEmail("Sergei@test.com");

        assertThat(foundUser).isPresent();

        User actual = foundUser.orElseThrow();
        assertThat(actual.getId()).isEqualTo(savedUser.getId());
        assertThat(actual.getEmail()).isEqualTo("Sergei@test.com");
        assertThat(actual.getBalance()).isEqualTo(100);
    }

    @Test
    void returnsEmptyWhenEmailIsNotFound() {
        Optional<User> foundUser =
                userRepository.findByEmail("miss@test.com");

        assertThat(foundUser).isEmpty();
    }

    @Test
    void checksWhetherEmailExists() {
        saveUser("Sergei@test.com", 100);
        entityManager.clear();

        assertThat(userRepository.existsByEmail("Sergei@test.com")).isTrue();
        assertThat(userRepository.existsByEmail("miss@test.com")).isFalse();
    }

    @Test
    void checksWhetherIdExists() {
        User savedUser = saveUser("Sergei@test.com", 100);
        entityManager.clear();

        assertThat(userRepository.existsById(savedUser.getId())).isTrue();
        assertThat(userRepository.existsById(Long.MAX_VALUE)).isFalse();
    }

    @Test
    void findsUsersWithBalanceGreaterThanGivenAmount() {
        saveUser("below@test.com", 99);
        saveUser("equal@test.com", 100);
        saveUser("above@test.com", 101);
        entityManager.clear();

        var users = userRepository.findByBalanceGreaterThan(100);

        assertThat(users)
                .extracting(User::getEmail)
                .containsExactly("above@test.com");
    }

    private User saveUser(String email, int balance) {
        User user = User.builder()
                .name("Test")
                .last_name("User")
                .email(email)
                .pass("test-password")
                .role(Role.CLIENT)
                .balance(balance)
                .build();

        return userRepository.saveAndFlush(user);
    }

    @Test
    void deletesUser() {
        User savedUser = saveUser("delete@test.com", 10);

        userRepository.deleteById(savedUser.getId());
        userRepository.flush();
        entityManager.clear();

        assertThat(userRepository.findById(savedUser.getId())).isEmpty();
    }
}
