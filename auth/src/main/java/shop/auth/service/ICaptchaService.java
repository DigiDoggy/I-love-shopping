package shop.auth.service;

public interface ICaptchaService {
    void processResponse(String response, String action);
}
