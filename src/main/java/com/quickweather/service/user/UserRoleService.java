package com.quickweather.service.user;

import com.quickweather.domain.user.Role;
import com.quickweather.domain.user.RoleType;
import com.quickweather.repository.RoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@Slf4j
public class UserRoleService {

    private final RoleRepository roleRepository;

    public UserRoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    /**
     * Assigns the default USER role to the user. If it doesn't exist, create it on the fly.
     */
    public void assignDefaultUserRole(Set<Role> roles) {
        Role defaultRole = roleRepository.findByRoleType(RoleType.USER)
                .orElseGet(() -> {
                    log.info("Default role USER not found, creating a new one...");
                    return roleRepository.save(new Role(null, RoleType.USER, new HashSet<>()));
                });
        roles.add(defaultRole);
    }

}
