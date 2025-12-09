package shop.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender javaMailSender;
    private final MailSender mailSender;

    public void sendResetPasswordEmail(String email, String resetLink){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Reset your password");
        message.setText("Please reset your password using this link: " + resetLink + "/n/n"+
                "This link is valid for 15 minutes.");

        mailSender.send(message);
    }
}
