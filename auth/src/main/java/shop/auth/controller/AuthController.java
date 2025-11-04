package shop.auth.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import shop.auth.dto.AuthDTO;
import shop.auth.model.User;
import shop.auth.repository.RefreshTokenRepository;
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


    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@CookieValue(name = "refreshToken", required = false) String rawToken){
        if (rawToken == null || rawToken.isBlank()){
            return ResponseEntity.badRequest().body("Refresh token cookie is missing");
        }

        return refreshTokenRepository.findByTokenHash(rawToken)
                .map(token->{
                    if (refreshTokenService.isTokenExpired(token)){
                        refreshTokenRepository.delete(token);
                        return ResponseEntity.badRequest().body("Refresh token expired. Please login again.");
                    }

                    String newJwt = jwtService.generateToken(token.getUser().getEmail());
                    return ResponseEntity.ok(Map.of("token", newJwt));
                })
                .orElse(ResponseEntity.badRequest().body("Invalid refresh token"));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody AuthDTO request, HttpServletResponse resp){

        UserService.AuthResult result= userService.login(request);

        ResponseCookie cookie= ResponseCookie.from("refreshToken", result.refreshToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/api/auth")
                .build();

        resp.addHeader("Set-Cookie", cookie.toString());

        return ResponseEntity.ok(Map.of("accessToken", result.accessToken()));
    }

    @PostMapping("/register")
    public User register(@RequestBody User user){
        return userService.register(user);
    }



    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody Map<String, String> payload){
        String requestToken = payload.get("refreshToken");
        if (requestToken == null || requestToken.isBlank()){
            return ResponseEntity.badRequest().body("Refresh token is required");
        }

        return refreshTokenRepository.findByTokenHash(requestToken)
                .map(token->{
                    refreshTokenRepository.delete(token);
                    return ResponseEntity.ok("Logged out successfully.");
                })
                .orElse(ResponseEntity.badRequest().body("Invalid refresh token"));
    }

    @GetMapping("/")
    @ResponseBody
    public String greateIndex(){
        return "Hello World from oa2Auth";
    }

}
