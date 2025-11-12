package shop.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

//
@Component
@ConfigurationProperties(prefix = "google.recaptcha.key")
@Getter
@Setter
public class CaptchaSettings {

    private String site;
    private String secret;
    private float threshold;

}
