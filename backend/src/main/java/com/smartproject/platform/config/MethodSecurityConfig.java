package com.smartproject.platform.config;

import com.smartproject.platform.repository.ProjectRepository;
import com.smartproject.platform.repository.UserRepository;
import com.smartproject.platform.security.expression.CustomMethodSecurityExpressionHandler;
import com.smartproject.platform.service.PermissionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class MethodSecurityConfig {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final PermissionService permissionService;

    public MethodSecurityConfig(
            UserRepository userRepository,
            ProjectRepository projectRepository,
            PermissionService permissionService) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.permissionService = permissionService;
    }

    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler() {
        return new CustomMethodSecurityExpressionHandler(
                userRepository,
                projectRepository,
                permissionService
        );
    }
}
