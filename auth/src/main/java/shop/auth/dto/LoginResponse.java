package shop.auth.dto;

public record LoginResponse(
        boolean isLogged,
        String accessToken,
        //duration is needed for token refresh on the frontend
        Long expiresIn,
        String totp
) {
}
