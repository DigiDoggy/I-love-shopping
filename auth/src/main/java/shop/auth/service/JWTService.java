package shop.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import shop.auth.enums.TokenType;
import shop.auth.exception.AppException;
import shop.auth.model.Token;
import shop.auth.model.User;
import shop.auth.repository.TokenRepository;
import shop.auth.repository.UserRepository;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Service
@Data
@RequiredArgsConstructor
public class JWTService {
    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;


    private final PasswordEncoder passwordEncoder;
    @Value("${jwt.secret}")
    private String secretKey;
    @Value("${jwt.access.duration}")
    private long accessExpirationS;
    @Value("${jwt.refresh.duration}")
    private long refreshExpirationS;
    @Value("${jwt.reset.duration}")
    private long resetPasswordTokenDuration;


    public String generateToken(String email, long expirationSeconds) {

        Map<String, Object> claims = new HashMap<>();

        String token = Jwts.builder()
                .claims()
                .add(claims)
                .subject(email)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationSeconds * 1000))
                .and()
                .signWith(getKey(), Jwts.SIG.HS256)
                .compact();

        // need to save token in to database
        log.info("Generated token for user: {} ", email);
        return token;
    }

    public void saveToken(String token, User user, TokenType tokenType) {
        String tokenHash = passwordEncoder.encode(token);

        long time = switch (tokenType) {
            case ACCESS -> accessExpirationS;
            case REFRESH -> refreshExpirationS;
            case RESET_PASSWORD -> resetPasswordTokenDuration;
            default -> throw new RuntimeException("Invalid token type" + tokenType);
        };

        Token tokenEntity = new Token();
        tokenEntity.setUser(user);
        tokenEntity.setTokenHash(tokenHash);
        tokenEntity.setTokenType(tokenType);
        tokenEntity.setExpiresAt(tokenEntity.getIssuedAt().plusSeconds(time));
        tokenRepository.save(tokenEntity);
    }

    private SecretKey getKey() {
        byte[] keyBytes = Base64.getDecoder().decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(String email, User user) {
        if (user != null) {
            tokenRepository.deleteByUserIdAndType(user.getId(), TokenType.ACCESS);
        }

        String accessToken = generateToken(email, accessExpirationS);

        saveToken(accessToken, user, TokenType.ACCESS);
        return accessToken;
    }

    public String extractUserEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private <T> T extractClaim(String token, Function<Claims, T> ClaimResolver) {
        final Claims claims = extractAllClaims(token);
        return ClaimResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }


    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private String getEmailFromToken(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    //Security check authentication with access token, standard check
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String userEmail = extractUserEmail(token);
        return (userEmail.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    // validate token and return user for All Token (reset, refresh "work for access to") services
    public TokenValidationResult validateToken(String token, TokenType tokenType) {

        String email = extractUserEmail(token);

        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new AppException(HttpStatus.BAD_REQUEST, "User not found")
        );

        Token tokenEntity = tokenRepository.findByUserIdAndType(user.getId(), tokenType).orElseThrow(
                () -> new AppException(HttpStatus.BAD_REQUEST, "Invalid token")
        );

        if (!passwordEncoder.matches(token, tokenEntity.getTokenHash())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Invalid token");
        }

        return new TokenValidationResult(user, tokenEntity);
    }

    public record TokenValidationResult(User user, Token tokenEntity) {

    }

}
