package com.quickweather.service.admin;

import com.quickweather.admin.SecurityEventType;
import com.quickweather.domain.user.SecurityEvent;
import com.quickweather.repository.SecurityEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SecurityEventService {

    private final SecurityEventRepository repository;

    public void logEvent(String username, SecurityEventType eventType, String ipAddress) {
        SecurityEvent event = new SecurityEvent(username, eventType, ipAddress, LocalDateTime.now());
        repository.save(event);
    }

    public String getClientIpAddress() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null && attributes.getRequest() != null) {
            return attributes.getRequest().getRemoteAddr();
        }
        return "unknown-ip";
    }

    public Page<SecurityEvent> getAllEvents(Pageable pageable) {
        return repository.findAll(pageable);
    }

}
