package shop.auth.dto;

import shop.auth.enums.Roles;
import shop.auth.model.Address;

public record UserDTO(
        String id,
        String email,
        String userName,
        String phoneCountryCode,
        String phoneNumber,
        Roles roles,
        boolean twoFaEnabled,
        String twoFaSecret,
        String createdAt,
        String updatedAt,
        boolean emailVerified,
        Address defaultAddress
        ) {
}
