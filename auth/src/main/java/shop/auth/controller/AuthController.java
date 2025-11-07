package shop.auth.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import shop.auth.dto.AuthDTO;
import shop.auth.dto.LoginResponse;
import shop.auth.model.User;
import shop.auth.repository.RefreshTokenRepository;
import shop.auth.service.AuthService;
import shop.auth.service.JWTService;
import shop.auth.service.RefreshTokenService;
import shop.auth.service.UserService;

import java.util.Map;

@Controller
@AllArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenService refreshTokenService;
    private final JWTService jwtService;
    private final UserService userService;
    private final AuthService authService;


    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(
            @CookieValue(name = "refreshToken", required = false) String token){

        Map<String,String> tokens = authService.refresh(token);

        tokens.forEach((key, value) -> {
            ResponseCookie cookie= ResponseCookie.from(key, value)
                    .httpOnly(true)
                    .secure(true)
                    .sameSite("None")
                    .path("/api/auth")
                    .build();
        });

        ResponseCookie accessCookie = ResponseCookie.from("accessToken",tokens.get("accessToken"))
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .build();
        ResponseCookie refreshToken = ResponseCookie.from("refreshToken", tokens.get("refreshToken"))
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/api/auth")
                .build();



        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshToken.toString())
                .body(new LoginResponse(true));

    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            @CookieValue(name = "accessToken", required = false) String accessToken,
            @RequestBody AuthDTO request,
            HttpServletResponse resp){

        UserService.AuthResult result= userService.login(request);

        ResponseCookie refreshCookie= ResponseCookie.from("refreshToken", result.refreshToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/api/auth")
                .build();

        ResponseCookie accessCookie = ResponseCookie.from("accessToken",result.accessToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .build();

        resp.addHeader("Set-Cookie", refreshCookie.toString());
        resp.addHeader("Set-Cookie", accessCookie.toString());


        return ResponseEntity.ok(new LoginResponse(true));
    }

    @PostMapping("/register")
    public User register(@RequestBody User user){
        return userService.register(user);
    }



    @PostMapping("/logout")
    public ResponseEntity<LoginResponse> logout(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            @CookieValue(name = "accessToken", required = false) String accessToken) {

        return authService.logout(refreshToken, accessToken);

    }

    @GetMapping("/")
    @ResponseBody
    public String createIndex(){
        return "Hello World from oa2Auth";
    }

}
