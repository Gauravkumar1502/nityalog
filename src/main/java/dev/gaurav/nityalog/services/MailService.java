package dev.gaurav.nityalog.services;

import gg.jte.TemplateEngine;
import gg.jte.output.StringOutput;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MailService {
    private final TemplateEngine templateEngine;
    private final JavaMailSender javaMailSender;

    public void send(String to, String subject, String body, boolean isHtmlFormatted) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, isHtmlFormatted);
            helper.setFrom("noreply@yourdomain.com");

            javaMailSender.send(message);
            log.info("Mail successfully sent to {}", to);
        } catch (MailException e) {
            throw new MailSendException("SMTP error while sending mail to " + to, e);
        }
        catch (MessagingException e) {
            throw new MailSendException("Message format error while sending mail to " + to, e);
        }
    }

    public void sendRegistrationEmail(@NotBlank @Email String email, @NotBlank String rawOtp, Duration expireIn) {
        String subject = "Welcome to Our App! Please Verify Your Email";
        StringOutput output = new StringOutput();
        Map<String, Object> model = new HashMap<>();
        model.put("verificationCode", rawOtp);
        model.put("expireInMinutes", expireIn.toMinutes());
        templateEngine.render("registration-email.jte", model, output);
        String body = output.toString();
        send(email, subject, body, true);
    }
}
