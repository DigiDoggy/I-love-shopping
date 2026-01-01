package shop.auth.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import shop.auth.dto.UserDTO;
import shop.auth.dto.request.RegisterRequest;
import shop.auth.model.User;

@Mapper(componentModel = "spring", uses = AddressMapper.class)
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "emailVerified", constant = "false")
    @Mapping(target = "twoFaEnabled", constant = "false")
    @Mapping(target = "roles", expression = "java(shop.auth.enums.Roles.ROLE_USER)")
    @Mapping(target = "oauthAccounts", ignore = true)
    @Mapping(target = "tokens", ignore = true)
    @Mapping(target = "addresses", ignore = true)
    @Mapping(target = "twoFaSecret", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    User toEntity(RegisterRequest request);

    @Mapping(target = "id", expression = "java(user.getId().toString())")
    UserDTO toDTO(User user);

}
