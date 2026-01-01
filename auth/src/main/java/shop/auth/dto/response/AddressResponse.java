package shop.auth.dto.response;

public record AddressResponse(
        String street,
        String city,
        String stateProvince,
        String zipCode,
        String country,
        boolean isDefault
){
}
