package com.quickweather.service.user;

import com.quickweather.domain.user.Role;
import com.quickweather.domain.user.RoleType;
import com.quickweather.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private UserRoleService userRoleService;

    @Test
    void shouldAssignDefaultUserRoleSuccessfully() {
        Role existingRole = new Role();
        existingRole.setId(1L);
        existingRole.setRoleType(RoleType.USER);

        when(roleRepository.findByRoleType(RoleType.USER)).thenReturn(Optional.of(existingRole));

        Set<Role> roles = new HashSet<>();

        userRoleService.assignDefaultUserRole(roles);

        assertTrue(roles.contains(existingRole));
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void assignDefaultUserRole_createNewRole() {
        when(roleRepository.findByRoleType(RoleType.USER)).thenReturn(Optional.empty());

        Role newRole = new Role();
        newRole.setId(2L);
        newRole.setRoleType(RoleType.USER);
        when(roleRepository.save(any(Role.class))).thenReturn(newRole);

        HashSet<Role> roles = new HashSet<>();

        userRoleService.assignDefaultUserRole(roles);

        assertTrue(roles.contains(newRole));
        verify(roleRepository, times(1)).save(any(Role.class));
    }

}