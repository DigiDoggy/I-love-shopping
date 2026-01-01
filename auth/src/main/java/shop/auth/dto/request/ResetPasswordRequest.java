package shop.auth.dto.request;

public record ResetPasswordRequest(
        String token,
        String password,
        String confirmPassword
) {
}
