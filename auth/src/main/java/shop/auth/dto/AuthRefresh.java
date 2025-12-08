package shop.auth.dto;

public record AuthRefresh(
        String accessToken,
        Long refreshExpiresIn
) {
}
