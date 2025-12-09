package shop.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import shop.auth.model.RefreshToken;
import shop.auth.model.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TokenRepository extends JpaRepository<RefreshToken, UUID> {
    List<RefreshToken> findByUserId(UUID userId);
    Optional<RefreshToken> findByTokenHash(String tokenHash);
}
