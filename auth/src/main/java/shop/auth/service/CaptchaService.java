package shop.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import shop.auth.config.CaptchaSettings;
import shop.auth.dto.GoogleResponse;
import shop.auth.exception.InvalidReCaptchaException;

import java.net.URI;
import java.util.regex.Pattern;

@Service
public class CaptchaService implements ICaptchaService {
    private final CaptchaSettings captchaSettings;
    private final RestOperations restTemplate;
    private final ReCaptchaAttemptService reCaptchaAttemptService;

    public CaptchaService(CaptchaSettings captchaSettings,
                          RestOperations restTemplate,
                          ReCaptchaAttemptService reCaptchaAttemptService) {
        this.captchaSettings = captchaSettings;
        this.restTemplate = restTemplate;
        this.reCaptchaAttemptService = reCaptchaAttemptService;
    }

    public static String REGISTRATION_ACTION= "register";

    private static Pattern RESPONSE_PATTERN = Pattern.compile("[A-Za-z0-9_-]+");

    @Override
    public void processResponse(String response,String action) {

        if (!responseSanityCheck(response)) {
            throw new InvalidReCaptchaException("Response contains invalid characters");
        }

        if(reCaptchaAttemptService.isBlocked(getClientIP())) {
            throw new InvalidReCaptchaException("Client exceeded maximum number of failed attempts");
        }

        URI verifyUri = URI.create(String.format(
                "https://www.google.com/recaptcha/api/siteverify?secret=%s&response=%s&remoteip=%s",
                getReCaptchaSecret(), response, getClientIP()
        ));

        GoogleResponse googleResponse = restTemplate.getForObject(verifyUri, GoogleResponse.class);

        if(!googleResponse.isSuccess() || !googleResponse.getAction().equals(action)
                || googleResponse.getScore() < captchaSettings.getThreshold()) {
            if(googleResponse.hasClientError()) {
                reCaptchaAttemptService.reCaptchaFailed(getClientIP());
            }
            throw new InvalidReCaptchaException("reCaptcha was not successfully validated");
        }
        reCaptchaAttemptService.reCaptchaSucceeded(getClientIP());
    }

    private boolean responseSanityCheck(String response) {
        return StringUtils.hasText(response) && RESPONSE_PATTERN.matcher(response).matches();
    }

    private String getReCaptchaSecret() {
        return captchaSettings.getSecret(); // из @ConfigurationProperties
    }


    private String getClientIP() {
        var attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) return "0.0.0.0";
        HttpServletRequest req = attrs.getRequest();
        String xf = req.getHeader("X-Forwarded-For");
        return (xf == null) ? req.getRemoteAddr() : xf.split(",")[0].trim();
    }
}
