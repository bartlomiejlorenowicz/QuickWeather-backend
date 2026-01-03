package com.quickweather.controller;

import com.quickweather.admin.SecurityEventType;
import com.quickweather.domain.user.SecurityEvent;
import com.quickweather.domain.user.User;
import com.quickweather.domain.user.UserActivityLog;
import com.quickweather.dto.admin.AdminStatsResponse;
import com.quickweather.dto.admin.AdminUserDTO;
import com.quickweather.dto.weatherDtos.weather.request.CityLog;
import com.quickweather.dto.weatherDtos.weather.request.TopWeather;
import com.quickweather.dto.apiResponse.OperationType;
import com.quickweather.dto.user.user_auth.ChangePasswordRequest;
import com.quickweather.service.admin.AdminService;
import com.quickweather.service.admin.SecurityEventService;
import com.quickweather.service.admin.UserActivityService;
import com.quickweather.service.user.PasswordService;
import com.quickweather.validation.IntegrationTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminControllerTest extends IntegrationTestConfig {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminService adminService;

    @MockBean
    private SecurityEventService securityEventService;

    @MockBean
    private PasswordService passwordService;

    @MockBean
    private UserActivityService userActivityService;

    private AdminStatsResponse adminStatsResponse;

    @BeforeEach
    void setUp() {
        adminStatsResponse = new AdminStatsResponse();
        adminStatsResponse.setActiveUsers(15);
        adminStatsResponse.setTotalUsers(20);
        adminStatsResponse.setInactiveUsers(5);

        CityLog cityLog1 = new CityLog("London", 2);
        CityLog cityLog2 = new CityLog("Warsaw", 5);
        CityLog cityLog3 = new CityLog("Cracow", 1);

        List<CityLog> cityLogs = List.of(cityLog1, cityLog2, cityLog3);
        adminStatsResponse.setCityLogs(cityLogs);

        TopWeather topWeather = new TopWeather();
        topWeather.setLabel("Warsaw");
        topWeather.setCount(5);
        adminStatsResponse.setTopWeather(topWeather);
    }

    @BeforeEach
    void setUpApiResponse() {

    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void shouldReturnUsersStatsSuccessfully() throws Exception {

        when(adminService.getDashboardStats()).thenReturn(adminStatsResponse);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/admin/stats")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeUsers").value(15))
                .andExpect(jsonPath("$.totalUsers").value(20))
                .andExpect(jsonPath("$.inactiveUsers").value(5))
                .andExpect(jsonPath("$.cityLogs").isNotEmpty())
                .andExpect(jsonPath("$.cityLogs[0].city").value("London"))
                .andExpect(jsonPath("$.cityLogs", hasSize(3)));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void shouldReturnAllUsersSuccessfully() throws Exception {
        AdminUserDTO adminUserDTO1 = new AdminUserDTO();
        adminUserDTO1.setId(1L);
        adminUserDTO1.setFirstName("Matt");
        adminUserDTO1.setLastName("Lenat");
        adminUserDTO1.setEmail("matt@gmail.com");
        adminUserDTO1.setEnabled(true);

        AdminUserDTO adminUserDTO2 = new AdminUserDTO();
        adminUserDTO2.setId(2L);
        adminUserDTO2.setFirstName("Dany");
        adminUserDTO2.setLastName("Niclas");
        adminUserDTO2.setEmail("Dany@gmail.com");
        adminUserDTO2.setEnabled(true);

        List<AdminUserDTO> users = List.of(adminUserDTO1, adminUserDTO2);
        Page<AdminUserDTO> userPage = new PageImpl<>(users);

        when(adminService.getAllUsers(null, PageRequest.of(0, 10, Sort.by("id")))).thenReturn(userPage);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/admin/users")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].email").value("matt@gmail.com"))
                .andExpect(jsonPath("$.content[1].firstName").value("Dany"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void shouldEnableUserWithIdWithSuccessfully() throws Exception {

        User user = new User();
        user.setId(1L);

        doNothing().when(adminService).enableUser(user.getId());

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/admin/users/{userId}/enable", user.getId())
                        .param("userId", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User enabled successfully"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.operationType").value(OperationType.CHANGE_USER_STATUS.name()));

        verify(adminService, times(1)).enableUser(user.getId());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void shouldDisableUserWithIdWithSuccessfully() throws Exception {

        User user = new User();
        user.setId(1L);

        doNothing().when(adminService).disableUser(user.getId());

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/admin/users/{userId}/disable", user.getId())
                        .param("userId", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User disabled successfully"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.operationType").value(OperationType.CHANGE_USER_STATUS.name()));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void shouldReturnUserActivityLogsSuccessfully() throws Exception {
        List<UserActivityLog> logs = List.of(
                new UserActivityLog("1", "mark@wp.pl", "Warsaw", LocalDateTime.of(2025, 4, 1, 10, 0), "\"Request: GET /api/v1/weather/city/air-quality\""),
                new UserActivityLog("2", "dany@google.com", "Cracow", LocalDateTime.of(2025, 4, 2, 11, 0), "\"Request: GET /api/v1/weather/city\"")
        );

        when(userActivityService.search(null, null, null, null, null)).thenReturn(logs);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/admin/user-activity")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$.[0].email").value("mark@wp.pl"))
                .andExpect(jsonPath("$.[0].activity").value("\"Request: GET /api/v1/weather/city/air-quality\""));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void shouldReturnUserSecurityEventSuccessfully() throws Exception {

        SecurityEvent securityEvent1 = new SecurityEvent();
        securityEvent1.setId(1L);
        securityEvent1.setEventType(SecurityEventType.ACCOUNT_CREATED);

        SecurityEvent securityEvent2 = new SecurityEvent();
        securityEvent2.setId(2L);
        securityEvent2.setEventType(SecurityEventType.ACCOUNT_DELETED);

        List<SecurityEvent> securityEvents = List.of(securityEvent1, securityEvent2);
        Page<SecurityEvent> securityEventsPage = new PageImpl<>(securityEvents, PageRequest.of(0,10, Sort.unsorted()), securityEvents.size());

        when(securityEventService.getAllEvents(PageRequest.of(0,10, Sort.unsorted()))).thenReturn(securityEventsPage);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/admin/logs")
                        .param("page","0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].eventType").value(SecurityEventType.ACCOUNT_CREATED.name()))
                .andExpect(jsonPath("$.content[1].eventType").value(SecurityEventType.ACCOUNT_DELETED.name()));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void shouldChangeAdminPasswordSuccessfully() throws Exception {
        ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest();
        changePasswordRequest.setCurrentPassword("oldPass");
        changePasswordRequest.setNewPassword("newPass");
        changePasswordRequest.setConfirmPassword("newPAss");

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonRequest = objectMapper.writeValueAsString(changePasswordRequest);

        doNothing().when(passwordService).changePassword("admin@wp.pl", changePasswordRequest);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/admin/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password changed successfully. Please log in again."))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.operationType").value(OperationType.CHANGE_PASSWORD.name()));

        verify(passwordService).changePassword(eq("admin"), any(ChangePasswordRequest.class));
    }
}