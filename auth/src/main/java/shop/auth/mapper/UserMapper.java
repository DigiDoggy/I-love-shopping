package shop.auth.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import shop.auth.dto.AuthDTO;
import shop.auth.dto.UserDTO;
import shop.auth.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mappings({
            @Mapping(target = "passwordHash", source = "password"),
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "createdAt", expression = "java(java.time.Instant.now())"),
            @Mapping(target = "emailVerified", constant = "false"),
            @Mapping(target = "twoFaEnabled", constant = "false"),
            @Mapping(target = "roles", constant = "USER"),
            @Mapping(target = "oauthAccounts", ignore = true),
            @Mapping(target = "tokens", ignore = true),
            @Mapping(target = "addresses", ignore = false)
    })
    User registerRequest(UserDTO userDTO);


    @Mapping(target = "passwordHash", source = "password")
    User loginRequest(AuthDTO AuthDTO);

    @Mapping(target = "password", source = "passwordHash")
    @Mapping(target = "id", ignore = true)
    UserDTO toDTO(User user);

}
