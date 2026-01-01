package shop.auth.dto;

import shop.auth.dto.response.AddressResponse;
import shop.auth.model.Address;

public record UserDTO(
        String id,
        String email,
        String userName,
        String phoneCountryCode,
        String phoneNumber,
        AddressResponse defaultAddress
        ) {
}
