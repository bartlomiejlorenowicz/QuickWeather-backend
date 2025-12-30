package com.quickweather.service.user;

import com.google.api.services.gmail.Gmail;
import com.quickweather.domain.user.User;
import com.quickweather.dto.user.user_auth.SetNewPasswordRequest;
import com.quickweather.exceptions.EmailSendingException;
import com.quickweather.integration.GmailQuickstart;
import com.quickweather.repository.UserRepository;
import com.quickweather.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import static java.util.Objects.isNull;

@Service
@Slf4j
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserSearchService userSearchService;
    private final JwtUtil jwtUtil;
    private final GmailQuickstart gmailQuickstart;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private static final String RESET_EMAIL_SUBJECT = "Password Reset Request";
    private static final String CONTENT_EMAIL = "Click the link to reset your password: ";

    @Value("${app.frontend.base-url:http://localhost:4200}")
    private String frontendBaseUrl;

    public void resetPasswordUsingToken(SetNewPasswordRequest request) {
        // Walidacja tokena resetu
        if (!jwtUtil.validateResetToken(request.getToken())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token.");
        }

        // Pobierz użytkownika na podstawie tokena
        String email = jwtUtil.extractUsernameFromResetToken(request.getToken());
        User user = userSearchService.findByEmail(email);
        log.info("User found: {}", user != null ? user.getEmail() : "null");

        if (isNull(user)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found.");
        }

        // Sprawdzenie, czy nowe hasło i potwierdzenie są takie same
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passwords do not match.");
        }

        // Zmień hasło
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    public void sendPasswordResetEmail(String email, String resetPath) {

        User user = userSearchService.findByEmail(email);

        String resetToken = jwtUtil.generateResetToken(user);
        String resetLink = UriComponentsBuilder.fromHttpUrl(frontendBaseUrl)
                .path(resetPath)
                .queryParam("token", resetToken)
                .toUriString();

        try {
            Gmail service = gmailQuickstart.getGmailService();
            gmailQuickstart.sendEmail(service, user.getEmail(), RESET_EMAIL_SUBJECT,
                    CONTENT_EMAIL + resetLink);
        } catch (Exception e) {
            log.error("Error sending email: {}", e.getMessage(), e);
            throw new EmailSendingException("Failed to send password reset email", e);
        }
    }
}
