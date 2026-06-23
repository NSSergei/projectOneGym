package project.storage.user;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;
import project.model.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findById(long id);

    boolean existsByEmail(String email);

    boolean existsById(Long id);

    List<User> findByBalanceGreaterThan(int balance);

    void deleteUserById(long id);

    Optional<User> findByEmail(String email);

}
