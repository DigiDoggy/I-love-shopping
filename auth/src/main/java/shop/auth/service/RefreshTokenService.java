package shop.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
//import shop.auth.config.JwtProperties;
import shop.auth.exception.AppException;
import shop.auth.model.RefreshToken;
import shop.auth.repository.RefreshTokenRepository;
import shop.auth.repository.UserRepository;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class RefreshTokenService {
    @Value("${jwt.refresh.duration}")
    private long refreshExpirationS;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JWTService jwtService;

    public String createRefreshToken(UUID userId){
        RefreshToken token = new RefreshToken();
        token.setUser(userRepository.findById(userId).orElseThrow());

        String refreshToken = generateRefreshToken();
        String hashToken = sha256(refreshToken);

        token.setIssuedAt(Instant.now());
        token.setExpiresAt(token.getIssuedAt().plusSeconds(refreshExpirationS));
        token.setTokenHash(hashToken);

        log.info("Created refresh token for user: {} ", token.getUser().getEmail());

        try{
            RefreshToken saved= refreshTokenRepository.save(token);
            log.info("Saved refresh token: {}", saved.getTokenHash());

            return refreshToken;
        }catch (Exception e){
            log.error("Error saving refresh token for user: {}, Error: {}", userId, e.getMessage());
            throw new AppException( HttpStatus.INTERNAL_SERVER_ERROR,"Could not create refresh token");
        }
    }

    public String generateRefreshToken(){
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
    public String sha256(String raw){

        try{
            var md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available.", e);
        }

    }

    public boolean isTokenExpired(RefreshToken token){
        return token.getExpiresAt().isBefore(Instant.now());
    }
}
