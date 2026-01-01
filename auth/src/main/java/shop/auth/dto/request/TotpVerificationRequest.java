package shop.auth.dto.request;

public record TotpVerificationRequest(
        String email,
        String totp
) {
}
