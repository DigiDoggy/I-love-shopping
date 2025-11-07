package shop.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.auth.config.OAuth2SuccessHandler;
import shop.auth.dto.LoginResponse;
import shop.auth.exception.AppException;
import shop.auth.exception.ResourceNotFoundException;
import shop.auth.model.RefreshToken;
import shop.auth.model.User;
import shop.auth.repository.RefreshTokenRepository;
import shop.auth.repository.UserRepository;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JWTService jWTService;



    @Transactional
    public Map<String, String> refresh(String refreshToken) {

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new AppException(HttpStatus.UNAUTHORIZED, ("Token not found"));
        }

        String toHashToken = refreshTokenService.sha256(refreshToken);

        RefreshToken rt = refreshTokenRepository.findByTokenHash(toHashToken)
                .orElseThrow(() -> new AppException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

        if (refreshTokenService.isTokenExpired(rt)) {

            rt.setRevokedAt(Instant.now());
            refreshTokenRepository.delete(rt);
            throw new AppException(HttpStatus.UNAUTHORIZED, "Refresh token expired. Please login again.");
        } else {
            refreshTokenRepository.delete(rt);
        }

        String newRefreshToken = refreshTokenService.createRefreshToken(rt.getUser().getId());
        String newAccessToken = jWTService.generateToken(rt.getUser().getEmail());
        return Map.of(
                "refreshToken", newRefreshToken,
                "accessToken", newAccessToken
        );
    }

    public ResponseEntity<LoginResponse> logout(String refreshToken, String accessToken) {
        SecurityContextHolder.clearContext();

        String email= jWTService.extractUserEmail(accessToken);

        User user = userRepository.findByEmail(email).orElseThrow(
                ()->new ResourceNotFoundException("User not found.")
        );

        //deleting tokens
        revokeTokensOfUser(user);

        HttpHeaders responseHeaders = new HttpHeaders();

        responseHeaders.add(HttpHeaders.SET_COOKIE, ResponseCookie
                .from("refreshToken", "")
                .maxAge(0)
                .httpOnly(true)
                .path("/api/auth")
                .build()
                .toString());

        responseHeaders.add(HttpHeaders.SET_COOKIE, ResponseCookie
                .from("accessToken", "")
                .maxAge(0)
                .httpOnly(true)
                .path("/")
                .build()
                .toString());
        LoginResponse loginResponse = new LoginResponse(false);

        return ResponseEntity.ok().headers(responseHeaders).body(loginResponse);

    }

    private void revokeTokensOfUser(User user) {
        List<RefreshToken> tokens = refreshTokenRepository.findByUserId(user.getId());

        tokens.forEach(token -> {
            token.setRevokedAt(Instant.now());
            refreshTokenRepository.delete(token);
        });


    }

}
