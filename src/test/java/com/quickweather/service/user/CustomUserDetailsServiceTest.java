package com.quickweather.service.user;

import com.quickweather.domain.user.Role;
import com.quickweather.domain.user.RoleType;
import com.quickweather.domain.user.User;
import com.quickweather.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("bartek@wp.com");
        testUser.setFirstName("bartek");
        testUser.setPassword("password");
        testUser.setLocked(false);
        testUser.setEnabled(true);
        testUser.setUuid(UUID.randomUUID());
        var role = new Role();
        role.setRoleType(RoleType.USER);
        testUser.setRoles(Set.of(role));
    }

    @Test
    void shouldThrowsExceptionNullInput() {
        assertThrows(UsernameNotFoundException.class, () -> customUserDetailsService.loadUserByUsername(null));
    }

    @Test
    void shouldThrowsExceptionBlankInput() {
        assertThrows(UsernameNotFoundException.class, () -> customUserDetailsService.loadUserByUsername("   "));
    }

    @Test
    void shouldThrowsUserNotFoundExceptionWhenUserNotFound() {
        when(userRepository.findByEmail("aaaa@wp.pl")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> customUserDetailsService.loadUserByUsername("aaaa@wp.pl"));
    }

    @Test
    void shouldReturnValidUserDetails() {
        when(userRepository.findByEmail("bartek@wp.com")).thenReturn(Optional.of(testUser));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(testUser.getEmail());

        assertNotNull(userDetails);
        assertEquals("bartek@wp.com", userDetails.getUsername());

        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

        assertNotNull(authorities);
        assertFalse(authorities.isEmpty());
        boolean hasUserRole = authorities.stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_USER"));
        assertTrue(hasUserRole);
    }

}