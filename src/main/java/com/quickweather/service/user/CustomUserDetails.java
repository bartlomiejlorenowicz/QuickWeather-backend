package com.quickweather.service.user;

import com.quickweather.domain.user.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.UUID;
@Getter
@AllArgsConstructor
@Setter
public class CustomUserDetails implements UserDetails {

    private final Long userId;
    private final String username;
    private final String name;
    private final String email;
    private final String password;
    private final boolean isLocked;
    private final boolean isEnabled;
    private final Collection<? extends GrantedAuthority> authorities;
    private final UUID uuid;

    public CustomUserDetails(User user) {
        this.userId = user.getId();
        this.username = user.getEmail();
        this.email = user.getEmail();
        this.name = user.getFirstName();
        this.password = user.getPassword();
        this.isLocked = user.isLocked();
        this.isEnabled = user.isEnabled();
        this.uuid = user.getUuid();

        this.authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getRoleType().name()))
                .toList();
    }


    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
