package shop.auth.service.reCaptcha;

public interface ICaptchaService {
    void processResponse(String response, String action);
}
