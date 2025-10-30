package shop.auth.service;

//todo mb need delete logging for User, Acc (str: 83, 91) and jost create LOA logging for authentication

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import shop.auth.model.OauthAccount;
import shop.auth.model.User;
import shop.auth.repository.OauthAccountRepository;
import shop.auth.repository.UserRepository;
import shop.auth.util.PasswordUtil;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final OauthAccountRepository oauthAccountRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        //need for verification email ( model.User)
        boolean emailVerified;
        OAuth2User oauth2User = super.loadUser(userRequest);
        ServletRequestAttributes req = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oauth2User.getAttributes();

        String nameAttribute = userRequest
                .getClientRegistration()
                .getProviderDetails()
                .getUserInfoEndpoint()
                .getUserNameAttributeName();

        String providerUserId = String.valueOf(attributes.get(nameAttribute));
        String email = String.valueOf(attributes.get("email"));

        //verify email if not present in provider
        if (email == null || email.isBlank() || email.equalsIgnoreCase("null")) {
            emailVerified = false;

            email = registrationId + "_" + providerUserId + "@notVerified.com";
        } else {
            emailVerified = true;
        }

        String finalEmail = email;


        String fullName = String.valueOf(attributes.getOrDefault("name", finalEmail));

        // Find or create user and oauth account
        oauthAccountRepository
                .findByProviderAndProviderUserId(registrationId, providerUserId)
                .orElseGet(() -> {
                    User user = userRepository.findByEmail(finalEmail).orElseGet(() -> {
                        User u = new User();
                        u.setEmail(finalEmail);
                        u.setUserName(fullName);
                        // placeholder password; not used for oauth, but column is non-null
                        u.setPasswordHash(PasswordUtil.generatePasswordHash());
                        u.setPhoneCountryCode("+0");
                        u.setPhoneNumber(finalEmail);
                        u.setRoles("ROLE_USER");
                        u.setEmailVerified(emailVerified);
                        return userRepository.save(u);
                    });

                    assert req != null;
                    log.info("Created new user: {}, Method: {}", user, req.getRequest().getMethod());


                    OauthAccount oa = new OauthAccount();
                    oa.setProvider(registrationId);
                    oa.setProviderUserId(providerUserId);
                    oa.setUser(user);

                    log.info("Created new oauth account: {}, Method: {}", oa, req.getRequest().getMethod());

                    return oauthAccountRepository.save(oa);
                });

        // Expose authorities and attributes to security context
        return new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                nameAttribute
        );
    }
}


