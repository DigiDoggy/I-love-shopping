package shop.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import shop.auth.enums.TokenType;
import shop.auth.model.Token;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TokenRepository extends JpaRepository<Token, UUID> {
    List<Token> findByUserId(UUID userId);

    Optional<Token> findByTokenHash(String tokenHash);

    //    void saveUserByUserId(UUID userId, Token token);
    @Modifying
    @Query("DELETE FROM Token t WHERE t.user.id = :userId AND t.tokenType = :type")
    void deleteByUserIdAndType(UUID userId, TokenType type);


    @Query("SELECT t FROM Token t WHERE t.user.id = :userId AND t.tokenType = :tokenType")
    Optional<Token> findByUserIdAndType(UUID userId, TokenType tokenType);


    @Query("DELETE FROM Token t WHERE t.user.id = :userId")
    void deleteUserTokens(UUID userId);
}
