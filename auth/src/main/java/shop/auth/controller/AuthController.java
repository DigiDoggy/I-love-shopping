package shop.auth.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import shop.auth.repository.RefreshTokenRepository;
import shop.auth.service.RefreshTokenService;

import java.util.Map;

@Controller
@AllArgsConstructor
public class AuthController {

    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenService refreshTokenService;


    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> payload){
        String requestToken = payload.get("refreshToken");

        return refreshTokenRepository.findByTokenHash(requestToken)
                .map(token->{
                    if (refreshTokenService.isTokenExpired(token)){
                        refreshTokenRepository.delete(token);
                        return ResponseEntity.badRequest().body("Refresh token expired. Please login again.");
                    }

                    String newJwt = jwtUtils.generateToken(token.getUser().getEmail());
                    return ResponseEntity.ok(Map.of("token", newJwt));
                })
                .orElse(ResponseEntity.badRequest().body("Invalid refresh token"));
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
}
