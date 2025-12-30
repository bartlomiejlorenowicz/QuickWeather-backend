package com.quickweather.controller.user;

import com.quickweather.dto.user.UserDto;
import com.quickweather.service.user.UserCreationService;
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

    public UserCreationController(UserCreationService userCreationService) {
        this.userCreationService = userCreationService;
    }

    @PostMapping()
    public ResponseEntity<Void> register(@RequestBody UserDto userDto) {
            userCreationService.createUser(userDto);
            log.info("User registered successfully: {}", userDto.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED).build();
    }

}
