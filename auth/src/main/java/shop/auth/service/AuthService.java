package shop.auth.service;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.auth.dto.UserDTO;
import shop.auth.dto.request.AddressRequest;
import shop.auth.dto.request.RegisterRequest;
import shop.auth.dto.response.AuthDTO;
import shop.auth.enums.TokenError;
import shop.auth.enums.TokenType;
import shop.auth.exception.AppException;
import shop.auth.exception.tokenErrors.TokenValidationException;
import shop.auth.mapper.AddressMapper;
import shop.auth.mapper.UserMapper;
import shop.auth.model.Address;
import shop.auth.model.Token;
import shop.auth.model.User;
import shop.auth.repository.TokenRepository;
import shop.auth.repository.UserRepository;
import shop.auth.service.twoFa.TotpSetupService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {


    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;
    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final JWTService jwtService;
    private final TotpSetupService totpSetupService;
    private final AddressMapper addressMapper;
    private final PasswordEncoder passwordEncoder;


    //todo need implement request validation
    @Transactional
    public UserDTO register(@NotNull RegisterRequest request) {

        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new AppException(HttpStatus.CONFLICT, "Email already in use");
        }

        User user = userMapper.toEntity(request);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setEmail(user.getEmail().toLowerCase());

        // write addresses for db
        List<Address> addresses = new ArrayList<>();
        if (request.addresses() != null) {
            for (AddressRequest dto : request.addresses()) {
                Address address = addressMapper.toEntity(dto);
                addresses.add(address);
                address.setUser(user);
            }
        }

        long defaults = addresses.stream().filter(Address::isDefaultAddress).count();
        if (defaults > 1) throw new AppException(HttpStatus.BAD_REQUEST, "Only one default address allowed");
        if (!addresses.isEmpty() && defaults == 0) addresses.get(0).setDefaultAddress(true);

        user.setAddresses(addresses);
        User savedUser = userRepository.save(user);

        return userMapper.toDTO(savedUser);
    }

    public UserService.AuthResult login(AuthDTO request) {
        Authentication authentication =
                authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        User user = userRepository.findByEmail(request.email().toLowerCase()).orElseThrow();

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
        boolean expired = refreshTokenService.isTokenExpired(tokenEntity);
        if (expired) {
            throw new TokenValidationException(TokenError.TOKEN_EXPIRED);
        }

        String newRefreshToken = refreshTokenService.createRefreshToken(user.getId());
        String newAccessToken = jwtService.generateAccessToken(user.getEmail(), user);

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

        return isEmpty(user);
    }

    private boolean isEmpty(User user) {
        List<Token> tokens = tokenRepository.findByUserId(user.getId());
        return tokens.isEmpty();
    }

    private void revokeTokensOfUser(User user) {

        List<Token> tokens = tokenRepository.findByUserId(user.getId());

        tokenRepository.deleteUserTokens(user.getId());

    }

}
