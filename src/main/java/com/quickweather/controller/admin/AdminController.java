package com.quickweather.controller.admin;

import com.quickweather.domain.user.SecurityEvent;
import com.quickweather.domain.user.UserActivityLog;
import com.quickweather.dto.admin.AdminStatsResponse;
import com.quickweather.dto.admin.AdminUserDTO;
import com.quickweather.dto.apiResponse.ApiResponse;
import com.quickweather.dto.apiResponse.OperationType;
import com.quickweather.dto.user.user_auth.ChangePasswordRequest;
import com.quickweather.service.admin.AdminService;
import com.quickweather.service.admin.SecurityEventService;
import com.quickweather.service.admin.UserActivityService;
import com.quickweather.service.user.PasswordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    private final SecurityEventService securityEventService;

    private final PasswordService passwordService;

    private final UserActivityService userActivityService;

    @GetMapping("/stats")
    public ResponseEntity<AdminStatsResponse> getDashboardStats() {
        AdminStatsResponse stats = adminService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/users")
    public ResponseEntity<Page<AdminUserDTO>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String email
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id"));
        Page<AdminUserDTO> users = adminService.getAllUsers(email, pageable);
        return ResponseEntity.ok(users);
    }

    @PatchMapping("/users/{userId}/enable")
    public ResponseEntity<ApiResponse> enableUser(@PathVariable Long userId) {
        adminService.enableUser(userId);
        ApiResponse apiResponse = ApiResponse.buildApiResponse("User enabled successfully", OperationType.CHANGE_USER_STATUS);
        return ResponseEntity.ok(apiResponse);
    }

    @PatchMapping("/users/{userId}/disable")
    public ResponseEntity<ApiResponse> disableUser(@PathVariable Long userId) {
        adminService.disableUser(userId);
        ApiResponse apiResponse = ApiResponse.buildApiResponse("User disabled successfully", OperationType.CHANGE_USER_STATUS);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/user-activity")
    public ResponseEntity<List<UserActivityLog>> searchUserActivity(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<UserActivityLog> results = userActivityService.search(userId, email, city, startDate, endDate);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/logs")
    public ResponseEntity<Page<SecurityEvent>> getSecurityEvents(Pageable pageable) {
        Page<SecurityEvent> events = securityEventService.getAllEvents(pageable);
        return ResponseEntity.ok(events);
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse> changeAdminPassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {

        log.info("Authenticated admin: {}", authentication.getName());

        String email = authentication.getName();
        passwordService.changePassword(email, request);

        ApiResponse apiResponse = ApiResponse.buildApiResponse(
                "Password changed successfully. Please log in again.",
                OperationType.CHANGE_PASSWORD
        );

        return ResponseEntity.ok(apiResponse);
    }
}
