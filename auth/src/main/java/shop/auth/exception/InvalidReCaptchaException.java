package shop.auth.exception;

import org.springframework.http.HttpStatus;

public class InvalidReCaptchaException extends AppException {

    public InvalidReCaptchaException(String message) {
        super(HttpStatus.BAD_GATEWAY, message);
    }
}
