package shop.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AddressRequest(

        @NotBlank
                String street,
        @NotBlank
        String city,
        @NotBlank
        String stateProvince,
        @NotBlank
        String zipCode,
        @NotBlank
        String country,
        boolean isDefault
) {
}
