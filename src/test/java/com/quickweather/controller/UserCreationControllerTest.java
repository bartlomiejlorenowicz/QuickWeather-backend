package com.quickweather.controller;

import com.quickweather.domain.user.Role;
import com.quickweather.dto.user.RegisterUserRequest;
import com.quickweather.dto.user.UserDto;
import com.quickweather.domain.user.User;
import com.quickweather.repository.RoleRepository;
import com.quickweather.repository.UserRepository;
import com.quickweather.service.user.UserRoleService;
import com.quickweather.validation.IntegrationTestConfig;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class UserCreationControllerTest extends IntegrationTestConfig {

    private static final String REGISTER_URL = "/api/v1/user/register";

    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private UserRepository userCreationRepository;

    @Autowired
    private RoleRepository roleRepository;


    @BeforeEach
    void setUp() {
        userCreationRepository.deleteAll();
        roleRepository.deleteAll();

        Set<Role> defaultRoles = new HashSet<>();
        userRoleService.assignDefaultUserRole(defaultRoles);

        defaultRoles.forEach(role -> {
            if (!roleRepository.existsByRoleType(role.getRoleType())) {
                roleRepository.save(role);
            }
        });

        User user = User.builder()
                .firstName("Andy")
                .lastName("Murphy")
                .password("JohnP@ss123!")
                .email("john123@wp.pl")
                .phoneNumber("1234567890")
                .uuid(UUID.randomUUID())
                .build();
        userCreationRepository.save(user);
    }

    @AfterEach
    void tearDown() {
        userCreationRepository.deleteAll();
    }



    @Test
    void shouldFailWhenFirstNameIsInvalid() throws Exception {
        UserDto userDto = UserDto.builder()
                .firstName("A")
                .lastName("Murphy")
                .password("JohnP@ss123!")
                .email("andy.murphy@wp.pl")
                .phoneNumber("1234567890")
                .build();

        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content()
                        .contentType("application/json"))
                .andExpect(jsonPath("$.message").value("first name must have at least 2 letters"));
    }

    @Test
    void shouldRegisterUser() throws Exception {

        String uniqueEmail = "test" + UUID.randomUUID() + "@wp.pl";

        UserDto userDto = UserDto.builder()
                .firstName("John")
                .lastName("Murphy")
                .password("JohnP@ss123!")
                .email(uniqueEmail)
                .phoneNumber("1234567890")
                .build();

        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andDo(print())
                .andExpect(status().isCreated());

        User user = userCreationRepository.findByEmail(userDto.getEmail()).orElseThrow();
        Assertions.assertNotNull(user.getUuid());
    }

    @Test
    void shouldFailWhenLastNameIsInvalid() throws Exception {
        UserDto userDto = UserDto.builder()
                .firstName("Andy")
                .lastName("M".repeat(31))
                .password("JohnP@ss123!")
                .email("john1236@wp.pl")
                .phoneNumber("1234567890")
                .build();

        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content()
                        .contentType("application/json"))
                .andExpect(jsonPath("$.message").value("last name must have maximum 30 letters"));
    }

    @Test
    void shouldFailWhenEmailIsNotInvalid() throws Exception {
        RegisterUserRequest request = RegisterUserRequest.builder()
                .firstName("Andy")
                .lastName("Murphy")
                .password("JohnP@ss123!")
                .email("john1237wp.pl")
                .phoneNumber("1234567890")
                .build();

        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content()
                        .contentType("application/json"))
                .andExpect(jsonPath("$.errors.email")
                        .value("must be a well-formed email address"));

    }

    @Test
    void shouldFailWhenEmailAlreadyExist() throws Exception {
        UserDto userDto = UserDto.builder()
                .firstName("Andy")
                .lastName("Murphy")
                .password("JohnP@ss123!")
                .email("john123@wp.pl")
                .phoneNumber("1234567890")
                .build();

        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.message").value("the given e-mail exists in the database"));
    }
}
