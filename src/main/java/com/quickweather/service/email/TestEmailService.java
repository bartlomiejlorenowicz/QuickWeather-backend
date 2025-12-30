package com.quickweather.service.email;


import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("test")
@Slf4j
@Primary
public class TestEmailService implements EmailService {

    @Override
    public void sendWelcomeEmail(String to, String firstName) {
        log.info("[TEST EMAIL] Welcome mail to {} ({})", to, firstName);
    }

    @Override
    public void sendForgotPasswordEmail(String to, String resetLink) {
        log.info("[TEST EMAIL] Reset password mail to {} → {}", to, resetLink);
    }
}