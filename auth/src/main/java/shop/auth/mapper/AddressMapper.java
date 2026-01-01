package shop.auth.mapper;

import org.mapstruct.*;
import shop.auth.dto.request.AddressRequest;
import shop.auth.dto.response.AddressResponse;
import shop.auth.model.Address;

    @Mapper(componentModel = "spring")
    public interface AddressMapper {

        @Mapping(target = "id", ignore = true)
        @Mapping(target = "user", ignore = true)
        @Mapping(target = "defaultAddress", source = "isDefault")
        Address toEntity(AddressRequest dto);
        @Mapping(target = "isDefault", source = "defaultAddress")
        AddressResponse toResponse(Address address);
    }