package com.quickweather.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickweather.domain.user.User;
import com.quickweather.dto.apiResponse.OperationType;
import com.quickweather.dto.token.TokenRequest;
import com.quickweather.dto.user.user_auth.EmailRequest;
import com.quickweather.dto.user.user_auth.SetNewPasswordRequest;
import com.quickweather.integration.GmailQuickstart;
import com.quickweather.security.JwtUtil;
import com.quickweather.validation.IntegrationTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ExtendWith(CommonTestSetupExtension.class)
class PasswordResetControllerTest extends IntegrationTestConfig {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwtUtil;


    @MockBean
    private GmailQuickstart gmailQuickstart;

    private String tokenUser;

    private User testUser;
    @RegisterExtension
    CommonTestSetupExtension setup = new CommonTestSetupExtension();

    @BeforeEach
    void setUp() {
        tokenUser = setup.getTokenUser();
        testUser = setup.getTestUser();
    }

    @Test
    void shouldResetPasswordSuccessfully() throws Exception {
        EmailRequest emailRequest = new EmailRequest();
        emailRequest.setEmail("testUser@wp.pl");

        mockMvc.perform(post("/api/v1/user/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emailRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password reset link sent"))
                .andExpect(jsonPath("$.operationType").value(OperationType.RESET_PASSWORD.name()))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldSendForgotPasswordWhenUserForgotPassword() throws Exception {
        EmailRequest emailRequest = new EmailRequest();
        emailRequest.setEmail("testUser@wp.pl");

        mockMvc.perform(post("/api/v1/user/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emailRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password reset link sent"))
                .andExpect(jsonPath("$.operationType").value(OperationType.FORGOT_PASSWORD.name()))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldValidateResetTokenSuccessfully() throws Exception {
        // given
        String validResetToken = jwtUtil.generateResetToken(testUser);

        TokenRequest tokenRequest = new TokenRequest();
        tokenRequest.setToken(validResetToken);

        // when + then
        mockMvc.perform(post("/api/v1/user/auth/validate-reset-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tokenRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Token is valid"))
                .andExpect(jsonPath("$.operationType")
                        .value(OperationType.VALIDATE_RESET_TOKEN.name()))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldSetNewPasswordSuccessfully() throws Exception {
        String resetToken = jwtUtil.generateResetToken(testUser);

        SetNewPasswordRequest request = new SetNewPasswordRequest();
        request.setNewPassword("Bartek123!");
        request.setConfirmPassword("Bartek123!");
        request.setToken(resetToken);

        mockMvc.perform(post("/api/v1/user/auth/set-new-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password updated successfully."))
                .andExpect(jsonPath("$.operationType")
                        .value(OperationType.SET_NEW_PASSWORD.name()))
                .andExpect(jsonPath("$.timestamp").exists());
    }

}