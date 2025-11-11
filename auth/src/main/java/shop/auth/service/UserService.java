package shop.auth.service;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import shop.auth.dto.AuthDTO;
import shop.auth.dto.UserDTO;
import shop.auth.mapper.UserMapper;
import shop.auth.model.User;
import shop.auth.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;
    private final RefreshTokenService refreshTokenService;
    @Autowired
    private UserMapper userMapper;

    private BCryptPasswordEncoder bCrypPasswordEncoder = new BCryptPasswordEncoder(12);

    //todo need implement request validation
    public User register(@NotNull User user){
        //here we need use request validation, instead of hashPassword request password
        user.setPasswordHash(bCrypPasswordEncoder.encode(user.getPasswordHash()));
        return userRepository.save(user);
    }

    public AuthResult login(AuthDTO request) {
        Authentication authentication =
                authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        User user = userRepository.findByEmail(request.email()).orElseThrow();
        String accessToken= jwtService.generateToken(user.getEmail());
        String refresh = refreshTokenService.createRefreshToken(user.getId());

        return new AuthResult(accessToken, refresh);
    }


    public List<UserDTO> getUsers() {

        System.out.println("Searching people");

        return userRepository.findAll()
                .stream()
                .map(userMapper::toDTO)
                .toList();
    }

    public record AuthResult( String accessToken, String refreshToken){}
}
