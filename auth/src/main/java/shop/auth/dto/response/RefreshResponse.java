package shop.auth.dto.response;

public record RefreshResponse(
        String accessToken,
        Long expiresIn
) {
}
