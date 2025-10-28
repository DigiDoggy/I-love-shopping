package shop.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import shop.auth.config.JwtProperties;
import shop.auth.model.RefreshToken;
import shop.auth.repository.RefreshTokenRepository;
import shop.auth.repository.UserRepository;

import java.time.Instant;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class RefreshTokenService {
    private final JwtProperties jwtProperties;  // takes from application.properties and injects it here ( getRefreshExpirationMs=7 days)
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    public RefreshToken createRefreshToken(UUID userId){
        var token = new RefreshToken();
        token.setUser(userRepository.findById(userId).orElseThrow());
        token.setIssuedAt(Instant.now());
        token.setExpiresAt(token.getIssuedAt().plusMillis(jwtProperties.getRefreshExpirationMs()));
        return refreshTokenRepository.save(token);
    }

    public boolean isTokenExpired(RefreshToken token){
        return token.getExpiresAt().isBefore(Instant.now());
    }
}
