package com.quickweather.service.email;

import org.springframework.stereotype.Service;

@Service
public interface EmailService {

    void sendWelcomeEmail(String to, String firstName);

    void sendForgotPasswordEmail(String to, String resetLink);
}