package shop.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import shop.auth.enums.TokenType;
import shop.auth.exception.AppException;
import shop.auth.model.Token;
import shop.auth.repository.TokenRepository;
import shop.auth.repository.UserRepository;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class RefreshTokenService {
    @Value("${jwt.refresh.duration}")
    private long refreshExpirationS;
    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final JWTService jwtService;
    private final PasswordEncoder passwordEncoder;

    public String createRefreshToken(UUID userId) {
        Token token = new Token();
        token.setUser(userRepository.findById(userId).orElseThrow(

                () -> new AppException(HttpStatus.NOT_FOUND, "User not found")
        ));

        String refreshToken = generateRefreshToken(token.getUser().getEmail());
        String hashToken = passwordEncoder.encode(refreshToken);

        token.setTokenType(TokenType.REFRESH);
        token.setIssuedAt(Instant.now());
        token.setExpiresAt(token.getIssuedAt().plusSeconds(refreshExpirationS));
        token.setTokenHash(hashToken);

        log.info("Created refresh token for user: {} ", token.getUser().getEmail());

        try {
            Token saved = tokenRepository.save(token);
            log.info("Saved refresh token: {}", saved.getTokenHash());

            return refreshToken;
        } catch (Exception e) {
            log.error("Error saving refresh token for user: {}, Error: {}", userId, e.getMessage());
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not create refresh token");
        }
    }

    public String generateRefreshToken(String email) {
        long duration = jwtService.getRefreshExpirationS();
        String token = jwtService.generateToken(email, duration);

        return token;
    }

    public boolean isTokenExpired(Token token) {
        return token.getExpiresAt().isBefore(Instant.now());
    }

}

