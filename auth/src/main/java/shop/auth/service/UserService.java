package shop.auth.service;

import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Service;
import shop.auth.model.User;
import shop.auth.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    private BCryptPasswordEncoder bCrypPasswordEncoder = new BCryptPasswordEncoder(12);

    //todo need implement request validation
    public User register(@NotNull User user){
        //here we need use request validation, instead of hashPassword request password
        user.setPasswordHash(bCrypPasswordEncoder.encode(user.getPasswordHash()));
        return userRepository.save(user);
    }
}
