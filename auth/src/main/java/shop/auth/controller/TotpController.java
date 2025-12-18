package shop.auth.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import shop.auth.dto.LoginResponse;
import shop.auth.dto.TotpVerificationRequest;
import shop.auth.model.UserPrincipal;
import shop.auth.service.UserService;
import shop.auth.service.twoFa.TotpVerificationService;
import shop.auth.util.CookieUtils;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/two-2fa")
public class TotpController {

    private final UserService userService;
    private final TotpVerificationService verifyTotpService;
    @Value("${jwt.refresh.duration}")
    private long refreshDuration;
    @Value("${jwt.access.duration}")
    private long accessTokenDuration;


    @PostMapping("/verify")
    public ResponseEntity<?> verifyTotp(@RequestBody TotpVerificationRequest request, HttpServletResponse resp) throws Exception {
        UserService.AuthResult result = verifyTotpService.verifyTotp(request.email(), request.totp());
        if (result != null) {

            CookieUtils.addCookie(resp, "refreshToken", result.refreshToken(), refreshDuration);

            return ResponseEntity.ok(new LoginResponse(true, result.accessToken(), accessTokenDuration, null));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid TOTP code.");
        }
    }

    @PostMapping("two-enable")
    public ResponseEntity<?> enableTwoFa(@AuthenticationPrincipal UserDetails principal) {
        //userName that is email
        userService.enableTwoFa(principal.getUsername());
        return ResponseEntity.ok("OK");
    }

    @PostMapping("two-disable")
    public ResponseEntity<?> disableTwoFa(@AuthenticationPrincipal UserPrincipal principal, @RequestParam String code){

        // Identify the target user via the authenticated principal (JWT),
        // not from request data, to avoid acting on another user's account.
       userService.disableTwoFa(principal.getUsername(), code);

        return ResponseEntity.ok("OK");
    }

}
