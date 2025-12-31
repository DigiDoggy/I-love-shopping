package shop.auth.dto.request;

import jakarta.validation.constraints.NotNull;

public record AddressRequest(

        @NotNull(message = "Street must not be blank")
        String street,
        @NotNull(message = "City must not be blank")
        String city,
        @NotNull(message = "State/Province must not be blank")
        String stateProvince,
        @NotNull(message = "Zip code must not be blank")
        String zipCode,
        @NotNull(message = "Country must not be blank")
        String country,
        @NotNull(message = "Default address must not be blank")
        boolean isDefault
) {
}
