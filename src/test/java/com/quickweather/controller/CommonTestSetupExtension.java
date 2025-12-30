package com.quickweather.controller;

import com.quickweather.domain.user.Role;
import com.quickweather.domain.user.User;
import com.quickweather.repository.RoleRepository;
import com.quickweather.repository.UserRepository;
import com.quickweather.security.JwtTestUtil;
import com.quickweather.service.user.UserRoleService;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashSet;
import java.util.Set;

@ActiveProfiles("test")
public class CommonTestSetupExtension implements BeforeEachCallback, AfterEachCallback {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTestUtil jwtTestUtil;

    @Autowired
    private UserRoleService userRoleService;

    private String tokenUser;

    private User testUser;

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        // Autowiring w rozszerzeniu
        SpringExtension.getApplicationContext(context)
                .getAutowireCapableBeanFactory()
                .autowireBean(this);

        userRepository.deleteAll();
        roleRepository.deleteAll();

        Set<Role> roles = new HashSet<>();
        userRoleService.assignDefaultUserRole(roles);

        User user = User.builder()
                .firstName("Adam")
                .lastName("Nowak")
                .email("testUser@wp.pl")
                .password(passwordEncoder.encode("testPassword"))
                .isEnabled(true)
                .roles(roles)
                .build();

        testUser = user;
        userRepository.save(user);

        tokenUser = jwtTestUtil.generateToken(user.getEmail(), "ROLE_USER");
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        userRepository.deleteAll();
        roleRepository.deleteAll();
    }

    public String getTokenUser() {
        return tokenUser;
    }

    public User getTestUser() {
        return testUser;
    }
}
