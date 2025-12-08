package shop.auth.repository;

import shop.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    Optional<User> findByPhoneNumber(String phoneNumber);
    User findByUserName(String username);

    boolean existsByEmailIgnoreCase(String email);

    Optional<User> findByTwoFaSecret(String secret);
}
