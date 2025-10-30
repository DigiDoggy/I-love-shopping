package shop.auth.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.UUID;

public class PasswordUtil {
    private static final BCryptPasswordEncoder encoder= new BCryptPasswordEncoder(12);

    public static String generatePasswordHash(){
        String randomPassword = UUID.randomUUID().toString();
        return encoder.encode(randomPassword);
    }
}
