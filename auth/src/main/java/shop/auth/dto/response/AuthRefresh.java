package shop.auth.dto.response;

public record AuthRefresh(
        String accessToken,
        Long refreshExpiresIn
) {
}
