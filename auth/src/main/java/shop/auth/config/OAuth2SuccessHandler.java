package shop.auth.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import shop.auth.enums.TokenType;
import shop.auth.exception.AppException;
import shop.auth.model.User;
import shop.auth.repository.UserRepository;
import shop.auth.service.JWTService;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JWTService jwtService;
    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oauthUser = oauthToken.getPrincipal();

        // email may be absent for some providers; fall back to synthetic
        String email = oauthUser.getAttribute("email");
        if (email == null || email.isBlank() || email.equalsIgnoreCase("null")) {
            String registrationId = oauthToken.getAuthorizedClientRegistrationId();
            String providerUserId = oauthUser.getName(); // equals nameAttribute from CustomOAuth2UserService
            email = registrationId + "_" + providerUserId + "@notVerified.com";
        }

        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new AppException(HttpStatus.BAD_REQUEST, "User not found"));


        String jwt = jwtService.generateAccessToken(email, user);
        jwtService.saveToken(jwt,user, TokenType.REFRESH);
        response.setHeader("Authorization", "Bearer " + jwt);
        response.sendRedirect("/");
    }
}


