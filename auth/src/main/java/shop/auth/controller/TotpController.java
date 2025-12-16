package shop.auth.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import shop.auth.dto.LoginResponse;
import shop.auth.dto.TotpVerificationRequest;
import shop.auth.service.UserService;
import shop.auth.service.twoFa.TotpVerificationService;
import shop.auth.util.CookieUtils;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class TotpController {

    private final TotpVerificationService verifyTotpService;
    @Value("${jwt.refresh.duration}")
    private long refreshDuration;
    @Value("${jwt.access.duration}")
    private long accessTokenDuration;


    @PostMapping("/verify-totp")
    public ResponseEntity<?> verifyTotp(@RequestBody TotpVerificationRequest request, HttpServletResponse resp) throws Exception {
        UserService.AuthResult result = verifyTotpService.verifyTotp(request.email(), request.totp());
        if (result != null) {

            CookieUtils.addCookie(resp, "refreshToken", result.refreshToken(), refreshDuration);

            return ResponseEntity.ok(new LoginResponse(true, result.accessToken(), accessTokenDuration, null));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid TOTP code.");
        }
    }
}
