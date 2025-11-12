package shop.auth.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import shop.auth.dto.AuthDTO;
import shop.auth.dto.LoginResponse;
import shop.auth.dto.UserDTO;
import shop.auth.model.User;
import shop.auth.service.AuthService;
import shop.auth.service.UserService;
import shop.auth.service.reCaptcha.CaptchaService;
import shop.auth.service.reCaptcha.ICaptchaService;
import shop.auth.util.CookieUtils;

import java.util.Map;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    @Value("${jwt.refresh.duration}")
    private long refreshDuration;
    @Value("${jwt.access.duration}")
    private long accessTokenDuration;

    private final AuthService authService;
    private final ICaptchaService captchaService;


    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(
            @CookieValue(name = "refreshToken", required = false) String token,
            HttpServletResponse resp) {

        Map<String, String> tokens = authService.refresh(token);

        //        CookieUtils.addCookie(resp, "accessToken", tokens.get("accessToken"), accessTokenDuration);
        CookieUtils.addCookie(resp, "refreshToken", tokens.get("refreshToken"), refreshDuration);


        return ResponseEntity.ok().body(new LoginResponse(true, tokens.get("accessToken"), accessTokenDuration));

    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody AuthDTO request,
            HttpServletResponse resp) {

        UserService.AuthResult result = authService.login(request);

        CookieUtils.addCookie(resp, "refreshToken", result.refreshToken(), refreshDuration);
        //        CookieUtils.addCookie(resp, "accessToken", result.accessToken(), accessTokenDuration);


        return ResponseEntity.ok(new LoginResponse(true, result.accessToken(), accessTokenDuration));
    }


    @PostMapping("/register")
    public User register(@Valid RegisterRequest request) {
        captchaService.processResponse(request.response, CaptchaService.REGISTRATION_ACTION);
        return authService.register(request.user);

    }

    @PostMapping("/logout")
    public ResponseEntity<LoginResponse> logout(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader,
            HttpServletResponse resp) {

        boolean success = authService.logout(refreshToken, authorizationHeader);

        //For clearing cookies in browser
        //        CookieUtils.deleteCookie(resp, "accessToken");
        CookieUtils.deleteCookie(resp, "refreshToken");

        return ResponseEntity.ok(new LoginResponse(success, null, null));

    }

    @GetMapping("/")
    @ResponseBody
    public String createIndex() {
        return "Hello World from oa2Auth";
    }

    // for distributing requests (otherwise further req ==null)
    public record RegisterRequest(UserDTO user, String response, String action) {
    }

    //test reCAptcha
    @PostMapping("/recaptcha/verify")
    public ResponseEntity<?> verifyRecaptcha(@RequestBody Map<String, String> body) {
        String token = body.get("response");
        String action = body.getOrDefault("action", CaptchaService.REGISTRATION_ACTION);
        captchaService.processResponse(token, action);
        return ResponseEntity.ok(Map.of("message", "ok"));
    }

}
