package shop.auth.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import shop.auth.dto.ResetPasswordRequest;
import shop.auth.dto.UserDTO;
import shop.auth.exception.AppException;
import shop.auth.service.JWTService;
import shop.auth.service.RefreshTokenService;
import shop.auth.service.UserService;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class UserController {
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final JWTService jwtService;


    @GetMapping("/users")
    public List<UserDTO> getUsers() {
        System.out.println("UserController.getUsers");
        return userService.getUsers();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgottenPassword(@RequestParam String email) {
        userService.forgottenPassword(email);
        return ResponseEntity.ok("Check your email for further instructions");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        // check if token is valid
        if (request.token() == null || request.token().isBlank()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Token not found");
        }

        if (jwtService.isTokenExpired(request.token())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Token expired");
        }

        boolean result = userService.resetPassword(
                request.token(),
                request.password(),
                request.confirmPassword()
        );

        return ResponseEntity.ok(result);
    }
}
