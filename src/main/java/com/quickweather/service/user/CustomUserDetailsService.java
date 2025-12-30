package com.quickweather.service.user;

import com.quickweather.domain.user.User;
import com.quickweather.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (username == null || username.isBlank()) {
            log.error("Email is null or blank. Cannot load user.");
            throw new UsernameNotFoundException("Email is null or blank.");
        }

        log.debug("Attempting to load user by email: {}", username);

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));

        log.debug("User found: {}", user.getEmail());

        return createCustomUserDetails(user);
    }

    public CustomUserDetails createCustomUserDetails(User user) {
        log.debug("Creating CustomUserDetails for user with ID: {}", user.getId());

        var authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getRoleType()))
                .collect(Collectors.toList());

        return new CustomUserDetails(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getEmail(),
                user.getPassword(),
                user.isLocked(),
                user.isEnabled(),
                authorities,
                user.getUuid()
        );
    }

}
