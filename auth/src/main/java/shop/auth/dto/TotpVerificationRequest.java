package shop.auth.dto;

public record TotpVerificationRequest(
        String email,
        String totp
) {
}
