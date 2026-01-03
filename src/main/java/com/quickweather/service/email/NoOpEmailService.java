package com.quickweather.service.email;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile({"default", "local", "test"})
@Slf4j
public class NoOpEmailService implements EmailService {

    @Override
    public void sendWelcomeEmail(String to, String firstName) {
        log.info("[NO-OP EMAIL] Welcome email to {}", to);
    }

    @Override
    public void sendForgotPasswordEmail(String to, String resetLink) {
        log.info("[NO-OP EMAIL] Reset password email to {}", to);
    }
}