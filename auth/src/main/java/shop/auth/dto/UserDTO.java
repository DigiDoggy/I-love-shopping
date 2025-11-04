package shop.auth.dto;

import java.util.UUID;

public record UserDTO(UUID id,
                      String userName,
                      String email,
                      String password,
                      String phoneCountryCode,
                      String phoneNumber) {
}
