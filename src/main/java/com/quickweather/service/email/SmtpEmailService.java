package com.quickweather.service.email;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Profile("mail")
@Slf4j
@RequiredArgsConstructor
public class SmtpEmailService implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String from;

    @Override
    public void sendWelcomeEmail(String to, String firstName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject("Welcome to QuickWeather 👋");
        message.setText(
                "Hi " + firstName + ",\n\n" +
                        "Welcome to QuickWeather!\n\n" +
                        "QuickWeather Team"
        );

        mailSender.send(message);
        log.info("SMTP welcome email sent to {}", to);
    }

    @Override
    public void sendForgotPasswordEmail(String to, String resetLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject("Reset your password");
        message.setText(
                "Click the link below to reset your password:\n\n" +
                        resetLink
        );

        mailSender.send(message);
        log.info("SMTP reset password email sent to {}", to);
    }
}