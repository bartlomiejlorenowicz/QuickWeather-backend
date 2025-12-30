package com.quickweather.interceptor;

import com.quickweather.service.admin.UserActivityService;
import com.quickweather.security.userdatails.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class UserActivityInterceptor implements HandlerInterceptor {

    private final UserActivityService userActivityService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = "anonymous";
        String email = "bartek@wp.pl";

        if (auth != null && auth.isAuthenticated()) {
            Object principal = auth.getPrincipal();

            if (principal instanceof CustomUserDetails) {
                CustomUserDetails userDetails = (CustomUserDetails) principal;
                userId = String.valueOf(userDetails.getUserId());
                email = userDetails.getEmail();
            } else {
                email = auth.getName();
                userId = auth.getName();
            }
        }

        String city = request.getParameter("city");
        if (city == null || city.isEmpty()) {
            city = "N/A";
        }

        String activity = "Request: " + request.getMethod() + " " + request.getRequestURI();

        userActivityService.logActivity(userId, email, city, activity);

        return true;
    }
}
