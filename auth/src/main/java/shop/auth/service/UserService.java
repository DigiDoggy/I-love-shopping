package shop.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import shop.auth.dto.UserDTO;
import shop.auth.enums.TokenType;
import shop.auth.exception.AppException;
import shop.auth.mapper.UserMapper;
import shop.auth.model.User;
import shop.auth.repository.UserRepository;
import shop.auth.service.twoFa.TotpSetupService;
import shop.auth.service.twoFa.TotpVerificationService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final PasswordEncoder passwordEncoder;
    @Value("${JWT_RESET_PASSWORD_TOKEN_DURATION_SECONDS}")
    private long resetPasswordTokenDuration;

    private final UserRepository userRepository;
    private final JWTService jwtService;
    private final UserMapper userMapper;
    private final EmailService emailService;
    private final TotpVerificationService verifyTotpService;
    private final TotpSetupService totpSetupService;


    public List<UserDTO> getUsers() {

        System.out.println("Searching people");

        return userRepository.findAll()
                .stream()
                .map(userMapper::toDTO)
                .toList();
    }

    public void forgottenPassword(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new RuntimeException("User not found")
        );

        String resetToken = jwtService.generateToken(user.getEmail(), resetPasswordTokenDuration);
        jwtService.saveToken(resetToken, user, TokenType.RESET_PASSWORD);

        String resetUrl = "http://localhost:8080/api/reset-password?token=" + resetToken;
        emailService.sendResetPasswordEmail(email, resetUrl);
    }

    public boolean resetPassword(String token, String newPassword, String confirmPassword) {

        if (!newPassword.equals(confirmPassword)) {
            throw new RuntimeException("Passwords do not match");
        }


        var result = jwtService.validateToken(token, TokenType.RESET_PASSWORD);

        User user = result.user();

        user.setPasswordHash(passwordEncoder.encode(newPassword));

        userRepository.save(user);

        return true;
    }

    public void enableTwoFa(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new AppException(HttpStatus.BAD_REQUEST,"User not found")
        );

        if (user.isTwoFaEnabled()) {
            throw new AppException(HttpStatus.BAD_REQUEST,"Two factor authentication is already enabled");
        }

        String secret =totpSetupService.createSecrete();

        user.setTwoFaEnabled(true);
        user.setTwoFaSecret(secret);
        userRepository.save(user);
    }

    public void disableTwoFa(String email, String code) {

        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new AppException(HttpStatus.BAD_REQUEST,"User not found")
        );

        if (!user.isTwoFaEnabled()) {
            throw new AppException(HttpStatus.BAD_REQUEST,"Two factor authentication is not enabled");
        }

        boolean isVerified = verifyTotpService.verifyTotpCode(email, code);
        if (!isVerified) {
            throw new AppException(
                    HttpStatus.BAD_REQUEST, "Invalid TOTP code");
        }

        user.setTwoFaEnabled(false);
        user.setTwoFaSecret(null);
        userRepository.save(user);
    }


    public record AuthResult(String accessToken, String refreshToken, String totp) {
    }
}
