package shop.auth.service;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.auth.dto.AuthDTO;
import shop.auth.dto.UserDTO;
import shop.auth.enums.TokenError;
import shop.auth.enums.TokenType;
import shop.auth.exception.AppException;
import shop.auth.exception.tokenErrors.TokenValidationException;
import shop.auth.mapper.UserMapper;
import shop.auth.model.Token;
import shop.auth.model.User;
import shop.auth.repository.TokenRepository;
import shop.auth.repository.UserRepository;
import shop.auth.service.twoFa.TotpSetupService;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {


    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;
    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final JWTService jWTService;
    private final UserMapper userMapper;
    private final JWTService jwtService;
    private final TotpSetupService totpSetupService;


    private BCryptPasswordEncoder bCrypPasswordEncoder = new BCryptPasswordEncoder(12);

    //todo need implement request validation
    public User register(@NotNull UserDTO userDTO) {

        if (userRepository.existsByEmailIgnoreCase(userDTO.email())) {
            throw new AppException(HttpStatus.CONFLICT, "Email already in use");
        }

        User user = userMapper.registerRequest(userDTO);
        String secret = totpSetupService.createSecrete();

        user.setPasswordHash(bCrypPasswordEncoder.encode(userDTO.password()));
        user.setEmail(user.getEmail().toLowerCase());
        user.setTwoFaSecret(secret);

        return userRepository.save(user);
    }

    public UserService.AuthResult login(AuthDTO request) {
        Authentication authentication =
                authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        User user = userRepository.findByEmail(request.email()).orElseThrow();

        // if we have 2fa enabled, return totp
        if (user.isTwoFaEnabled()) {
            String url = totpSetupService.createQrCode(user.getTwoFaSecret(), user.getEmail());
            return new UserService.AuthResult(null, null, url);
        }

        // if not return jwt, refresh token
        String accessToken = jwtService.generateAccessToken(user.getEmail(), user);
        String refresh = refreshTokenService.createRefreshToken(user.getId());

        return new UserService.AuthResult(accessToken, refresh, null);
    }

    @Transactional
    public Map<String, String> refresh(String refreshToken) {

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new TokenValidationException(TokenError.TOKEN_NOT_FOUND);
        }


        var result = jwtService.validateToken(refreshToken, TokenType.REFRESH);
        User user = result.user();
        Token tokenEntity = result.tokenEntity();

        if (tokenEntity.isBlacklisted()) {
            throw new TokenValidationException(TokenError.TOKEN_BLACKLISTED);
        }

        if (refreshTokenService.isTokenExpired(tokenEntity)) {
            tokenRepository.delete(tokenEntity);
            throw new TokenValidationException(TokenError.TOKEN_EXPIRED);
        } else {
            tokenRepository.delete(tokenEntity);
        }

        String newRefreshToken = refreshTokenService.createRefreshToken(user.getId());
        String newAccessToken = jWTService.generateAccessToken(user.getEmail(), user);

        return Map.of(
                "refreshToken", newRefreshToken,
                "accessToken", newAccessToken
        );

    }

    public boolean logout(String refreshToken) {
        SecurityContextHolder.clearContext();

        if (refreshToken == null || refreshToken.isBlank()) {
            return false;
        }

        var result = jwtService.validateToken(refreshToken, TokenType.REFRESH);
        User user = result.user();
        Token tokenEntity = result.tokenEntity();

        revokeTokensOfUser(user);

        return isRepEmpty(user);
    }

    private boolean isRepEmpty(User user) {
        List<Token> tokens = tokenRepository.findByUserId(user.getId());
        return tokens.isEmpty();
    }

    private void revokeTokensOfUser(User user) {

        List<Token> tokens = tokenRepository.findByUserId(user.getId());

        tokenRepository.deleteUserTokens(user.getId());

    }

}
