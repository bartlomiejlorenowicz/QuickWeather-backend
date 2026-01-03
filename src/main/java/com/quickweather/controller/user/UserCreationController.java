package com.quickweather.controller.user;

import com.quickweather.dto.user.RegisterUserRequest;
import com.quickweather.dto.user.UserResponse;
import com.quickweather.mapper.UserMapper;
import com.quickweather.service.user.UserCreationService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/v1/user/register")
public class UserCreationController {

    private final UserCreationService userCreationService;

    private final UserMapper userMapper;

    public UserCreationController(UserCreationService userCreationService, UserMapper userMapper) {
        this.userCreationService = userCreationService;
        this.userMapper = userMapper;
    }

    @PostMapping()
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterUserRequest request) {
        UserResponse response = userCreationService.createUser(request);
        log.info("User registered successfully: {}", request.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
