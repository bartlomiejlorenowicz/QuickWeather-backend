package com.quickweather.controller.user;

import com.quickweather.dto.apiResponse.ApiResponse;
import com.quickweather.dto.apiResponse.OperationType;
import com.quickweather.service.user.UserDeletionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/v1/delete-user")
@RequiredArgsConstructor
public class UserDeletionController {

    private final UserDeletionService userDeletionService;

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse> deleteUser(@PathVariable Long userId) {
        userDeletionService.deleteUser(userId);
        ApiResponse apiResponse = ApiResponse.buildApiResponse("User has been deleted", OperationType.DELETE_ACCOUNT);
        return ResponseEntity.ok(apiResponse);
    }
}
