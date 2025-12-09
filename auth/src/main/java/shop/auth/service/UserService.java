package shop.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import shop.auth.dto.UserDTO;
import shop.auth.exception.AppException;
import shop.auth.mapper.UserMapper;
import shop.auth.model.User;
import shop.auth.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final PasswordEncoder passwordEncoder;
    @Value("${JWT_RESET_PASSWORD_TOKEN_DURATION_SECONDS}")
    private long resetPasswordTokenDuration;

    private final UserRepository userRepository;
    private final JWTService jwtService;
    private final UserMapper userMapper;
    private final EmailService emailService;


    public List<UserDTO> getUsers() {

        System.out.println("Searching people");

        return userRepository.findAll()
                .stream()
                .map(userMapper::toDTO)
                .toList();
    }

    public void forgottenPassword(String email){
        User user = userRepository.findByEmail(email).orElseThrow(
                ()-> new RuntimeException("User not found")
        );

        String resetToken = jwtService.generateToken(user.getEmail(), resetPasswordTokenDuration);

        String resetUrl = "http://localhost:8080/api/reset-password?token=" + resetToken;
        emailService.sendResetPasswordEmail(email, resetUrl);
    }

    public boolean resetPassword(String token, String newPassword, String confirmPassword){

        if(!newPassword.equals(confirmPassword)){
            throw new RuntimeException("Passwords do not match");
        }

        String email = jwtService.validResetToken(token);

       User user=userRepository.findByEmail(email).orElseThrow(
               ()-> new AppException(HttpStatus.BAD_REQUEST ,"User not found")
       );

       user.setPasswordHash(passwordEncoder.encode(newPassword));

       userRepository.save(user);

       return true;
    }


    public void createMailForgetPassword(String email){



    }








    public record AuthResult( String accessToken, String refreshToken, String totp){}
}
