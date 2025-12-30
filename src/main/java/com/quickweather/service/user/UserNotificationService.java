//package com.quickweather.service.user;
//
//import com.google.api.services.gmail.Gmail;
//import com.quickweather.domain.user.EmailTemplate;
//import com.quickweather.integration.GmailQuickstart;
//import com.quickweather.repository.EmailTemplateRepository;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//
//@Service
//@Slf4j
//public class UserNotificationService {
//
//    private final GmailQuickstart gmailQuickstart;
//    private final EmailTemplateRepository emailTemplateRepository;
//
//    public UserNotificationService(GmailQuickstart gmailQuickstart, EmailTemplateRepository emailTemplateRepository) {
//        this.gmailQuickstart = gmailQuickstart;
//        this.emailTemplateRepository = emailTemplateRepository;
//    }
//
//    public void sendWelcomeEmail(String recipientEmail, String firstName) {
//        try {
//
//            // get template from database
//            EmailTemplate template = emailTemplateRepository.findByTemplateCode("WELCOME_EMAIL")
//                    .orElseGet(() -> {
//                        log.warn("Welcome email template not found. Using default template.");
//                        return EmailTemplate.builder()
//                                .subject("Welcome to QuickWeather!")
//                                .body("Hi %s,\n\nThank you for registering with QuickWeather. We hope you enjoy using our service!")
//                                .build();
//                    });
//
//            String subject = template.getSubject();
//            String body = String.format(template.getBody(), firstName);
//
//            Gmail service = gmailQuickstart.getGmailService();
//            gmailQuickstart.sendEmail(service, recipientEmail, subject, body);
//            log.info("Welcome email sent to: {}", recipientEmail);
//        } catch (Exception e) {
//            log.error("Failed to send welcome email to {}: {}", recipientEmail, e.getMessage(), e);
//        }
//    }
//
//}
