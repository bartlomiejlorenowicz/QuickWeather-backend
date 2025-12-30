package com.quickweather.controller;

import com.quickweather.domain.user.Role;
import com.quickweather.domain.user.RoleType;
import com.quickweather.domain.user.User;
import com.quickweather.repository.RoleRepository;
import com.quickweather.repository.UserRepository;
import com.quickweather.security.JwtUtil;
import com.quickweather.service.user.CustomUserDetails;
import com.quickweather.validation.IntegrationTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Set;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class UserDeletionControllerTest extends IntegrationTestConfig {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;


    @Test
    void shouldDeleteUserReturnSuccessfullyResponse() throws Exception {

        Role userRole = roleRepository.findByRoleType(RoleType.USER).orElseThrow();

        User user = userRepository.save(
                User.builder()
                        .email("testUser@wp.pl")
                        .firstName("Test")
                        .lastName("User")
                        .password(passwordEncoder.encode("password"))
                        .isEnabled(true)
                        .isLocked(false)
                        .uuid(UUID.randomUUID())
                        .roles(Set.of(userRole))
                        .build()
        );

        CustomUserDetails userDetails = new CustomUserDetails(user);

        String token = (String) jwtUtil
                .generateToken(userDetails, user.getUuid())
                .get("token");

        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/api/v1/delete-user/{userId}", user.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User has been deleted"))
                .andExpect(jsonPath("$.operationType").value("DELETE_ACCOUNT"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

}
