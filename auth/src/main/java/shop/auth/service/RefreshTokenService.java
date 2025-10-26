package shop.auth.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import shop.auth.model.RefreshToken;
import shop.auth.repository.RefreshTokenRepository;
import shop.auth.repository.UserRepository;

import java.time.Instant;
import java.util.UUID;

@AllArgsConstructor
@Data
@Service
public class RefreshTokenService {
    @Value("${jwt.refreshExpirationMs}")
    private Long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    public RefreshToken createRefreshToken(UUID userId){
        var token = new RefreshToken();
        token.setUser(userRepository.findById(userId).orElseThrow());
        token.setExpiresAt(token.getIssuedAt().plusMillis(refreshTokenDurationMs));

        return refreshTokenRepository.save(token);
    }

    public boolean isTokenExpired(RefreshToken token){
        return token.getExpiresAt().isBefore(Instant.now());
    }
}
