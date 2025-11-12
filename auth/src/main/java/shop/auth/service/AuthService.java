package shop.auth.service;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.auth.config.OAuth2SuccessHandler;
import shop.auth.dto.AuthDTO;
import shop.auth.dto.LoginResponse;
import shop.auth.dto.UserDTO;
import shop.auth.exception.AppException;
import shop.auth.exception.ResourceNotFoundException;
import shop.auth.mapper.UserMapper;
import shop.auth.model.RefreshToken;
import shop.auth.model.User;
import shop.auth.repository.RefreshTokenRepository;
import shop.auth.repository.UserRepository;
import shop.auth.service.reCaptcha.CaptchaService;

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
    private final UserMapper userMapper;
    private final JWTService jwtService;


    private BCryptPasswordEncoder bCrypPasswordEncoder = new BCryptPasswordEncoder(12);

    //todo need implement request validation
    public User register(@NotNull UserDTO userDTO){

        if (userRepository.existsByEmailIgnoreCase(userDTO.email())) {
            throw new AppException(HttpStatus.CONFLICT, "Email already in use");
        }

        //here we need use request validation, instead of hashPassword request password
        User user = userMapper.registerRequest(userDTO);
        user.setPasswordHash(bCrypPasswordEncoder.encode(userDTO.password()));
        user.setEmail(user.getEmail().toLowerCase());
        return userRepository.save(user);
    }

    public UserService.AuthResult login(AuthDTO request) {
        Authentication authentication =
                authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        User user = userRepository.findByEmail(request.email()).orElseThrow();
        String accessToken= jwtService.generateToken(user.getEmail());
        String refresh = refreshTokenService.createRefreshToken(user.getId());

        return new UserService.AuthResult(accessToken, refresh);
    }

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

    public boolean logout(String refreshToken, String authorizationHeader) {
        SecurityContextHolder.clearContext();

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return false;
        }

        String accessToken = authorizationHeader.substring(7).trim();
        String email = jWTService.extractUserEmail(accessToken);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        revokeTokensOfUser(user);
        return true;

    }

    private void revokeTokensOfUser(User user) {
        List<RefreshToken> tokens = refreshTokenRepository.findByUserId(user.getId());

        tokens.forEach(token -> {
            token.setRevokedAt(Instant.now());
            refreshTokenRepository.delete(token);
        });


    }

}
