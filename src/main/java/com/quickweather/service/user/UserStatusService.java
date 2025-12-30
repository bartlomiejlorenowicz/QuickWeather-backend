//package com.quickweather.service.user;
//
//import com.quickweather.domain.user.Role;
//import com.quickweather.dto.user.UserId;
//import com.quickweather.domain.user.User;
//import com.quickweather.exceptions.UserErrorType;
//import com.quickweather.exceptions.UserValidationException;
//import com.quickweather.repository.UserRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.HashSet;
//import java.util.Set;
//
//@Service
//@Slf4j
//@RequiredArgsConstructor
//public class UserStatusService {
//
//    private final UserRepository userRepository;
//    private final UserSearchService userSearchService;
//    private final UserRoleService userRoleService;
//
//    @Transactional
//    public void enableUser(UserId userId) {
//        User user = userSearchService.findById(userId.getId());
//        if (user.getRoles() == null || user.getRoles().isEmpty()) {
//            Set<Role> roles = new HashSet<>();
//            userRoleService.assignDefaultUserRole(roles);
//            user.setRoles(roles);
//        }
//        if (user.isEnabled()) {
//            throw new UserValidationException(UserErrorType.ACCOUNT_ENABLED, "User is already enabled");
//        }
//        user.setEnabled(true);
//        userRepository.save(user);
//        log.info("User with ID {} has been enabled", userId);
//    }
//
//    @Transactional
//    public void disableUser(UserId userId) {
//        User user = userSearchService.findById(userId.getId());
//        if (user.getRoles() == null || user.getRoles().isEmpty()) {
//            Set<Role> roles = new HashSet<>();
//            userRoleService.assignDefaultUserRole(roles);
//            user.setRoles(roles);
//        }
//        if (!user.isEnabled()) {
//            throw new UserValidationException(UserErrorType.ACCOUNT_DISABLED, "User is already disabled");
//        }
//        user.setEnabled(false);
//        userRepository.save(user);
//        log.info("User with ID {} has been disabled", userId);
//    }
//
//}
