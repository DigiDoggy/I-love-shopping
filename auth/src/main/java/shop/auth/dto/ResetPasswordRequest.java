package shop.auth.dto;

public record ResetPasswordRequest(
        String token,
        String password,
        String confirmPassword
) {
}
