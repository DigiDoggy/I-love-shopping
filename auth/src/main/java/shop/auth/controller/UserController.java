package shop.auth.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import shop.auth.dto.UserDTO;
import shop.auth.service.RefreshTokenService;
import shop.auth.service.UserService;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class UserController {
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;


    @GetMapping("/users")
    public List<UserDTO> getUsers(){
        System.out.println("UserController.getUsers");
        return userService.getUsers();
    }
//    @GetMapping("/me")
}
