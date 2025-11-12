package shop.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import shop.auth.dto.AuthDTO;
import shop.auth.dto.LoginResponse;
import shop.auth.model.User;
import shop.auth.service.AuthService;
import shop.auth.service.CaptchaService;
import shop.auth.service.ICaptchaService;
import shop.auth.service.UserService;
import shop.auth.util.CookieUtils;

import java.util.Map;


@Controller
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    @Value("${jwt.refresh.duration}")
    private long refreshDuration;
    @Value("${jwt.access.duration}")
    private long accessTokenDuration;


    private final UserService userService;
    private final AuthService authService;
    private final ICaptchaService captchaService;


    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(
            @CookieValue(name = "refreshToken", required = true) String token,
            HttpServletResponse resp){

         Map<String, String> tokens = authService.refresh(token);

        CookieUtils.addCookie(resp, "accessToken",  tokens.get("accessToken"),  accessTokenDuration);
        CookieUtils.addCookie(resp, "refreshToken", tokens.get("refreshToken"), refreshDuration);


        return ResponseEntity.ok().body(new LoginResponse(true));

    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody AuthDTO request,
            HttpServletResponse resp){

        UserService.AuthResult result= userService.login(request);

        CookieUtils.addCookie(resp, "refreshToken", result.refreshToken(), refreshDuration);
        CookieUtils.addCookie(resp, "accessToken", result.accessToken(), accessTokenDuration);

        return ResponseEntity.ok(new LoginResponse(true));
    }


    @PostMapping("/register")
    public User register(@RequestBody @Valid User user,
                         HttpServletRequest request ){

        String response = request.getParameter("response");
        captchaService.processResponse(response, CaptchaService.REGISTRATION_ACTION);
        return userService.register(user);
    }



    @PostMapping("/logout")
    public ResponseEntity<LoginResponse> logout(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            @CookieValue(name = "accessToken", required = false) String accessToken,
            HttpServletResponse resp) {

        ResponseEntity<LoginResponse> responseEntity= authService.logout(refreshToken, accessToken);

        //For clearing cookies in browser
        CookieUtils.deleteCookie(resp, "accessToken");
        CookieUtils.deleteCookie(resp, "refreshToken");

        return responseEntity;
    }

    @GetMapping("/")
    @ResponseBody
    public String createIndex(){
        return "Hello World from oa2Auth";
    }

}
