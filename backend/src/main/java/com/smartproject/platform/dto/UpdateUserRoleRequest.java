package com.smartproject.platform.dto;

import com.smartproject.platform.model.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Set;

@Data
public class UpdateUserRoleRequest {
    @NotNull
    private Set<Role> roles;
}
