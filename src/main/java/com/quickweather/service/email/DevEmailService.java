package com.quickweather.service.email;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile({"dev", "local"})
@Slf4j
public class DevEmailService implements EmailService {

    @Override
    public void sendWelcomeEmail(String to, String firstName) {
        log.info("[DEV EMAIL] Welcome mail to {} ({})", to, firstName);
    }

    @Override
    public void sendForgotPasswordEmail(String to, String resetLink) {
        log.info("[DEV EMAIL] Reset password mail to {} → {}", to, resetLink);
    }
}