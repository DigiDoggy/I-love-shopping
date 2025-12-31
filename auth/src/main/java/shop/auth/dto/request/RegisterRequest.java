package shop.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record RegisterRequest(
        @Email(message = "Invalid email address")
        @NotBlank(message = "Email must not be blank")
        String email,
        @NotBlank(message = "User name must not be blank")
        String userName,
        @NotBlank(message = "Password must not be blank")
        @Size(min = 8, max = 30, message = "Password must be at least 8 characters long")
        String password,
        @NotBlank(message = "Phone country code must not be blank")
        String phoneCountryCode,
        @NotBlank(message = "Phone number must not be blank")
        String phoneNumber,
        List<AddressRequest> addresses
) {
}
