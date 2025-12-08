package shop.auth.service.twoFa;

import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import shop.auth.exception.AppException;
import shop.auth.model.User;
import shop.auth.repository.UserRepository;
import shop.auth.service.JWTService;
import shop.auth.service.RefreshTokenService;
import shop.auth.service.UserService;

@Service
@RequiredArgsConstructor
public class TotpVerificationService {
    private final RefreshTokenService refreshTokenService;
    private final JWTService jwtService;
    private final UserRepository userRepository;

    private TimeProvider timeProvider = new SystemTimeProvider();
    private CodeGenerator codeGenerator = new DefaultCodeGenerator();
    private CodeVerifier verifier = new DefaultCodeVerifier(codeGenerator, timeProvider);


    public UserService.AuthResult verifyTotp(String email, String code) throws Exception {
        try {
            User user = userRepository.findByEmail(email).orElseThrow();
            boolean isValid = verifier.isValidCode(user.getTwoFaSecret(), code);

            if (!isValid) {
                throw new AppException(HttpStatus.BAD_REQUEST,"Invalid TOTP code.");
            }

            String accessToken = jwtService.generateToken(user.getEmail());
            String refresh = refreshTokenService.createRefreshToken(user.getId());
            return new UserService.AuthResult(accessToken, refresh, null);

        } catch (Exception e) {
            throw new Exception("Invalid TOTP code.", e);
        }
    }

}
