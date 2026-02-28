package com.smartproject.platform.security.expression;

import com.smartproject.platform.repository.ProjectRepository;
import com.smartproject.platform.repository.UserRepository;
import com.smartproject.platform.service.PermissionService;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;

public class CustomMethodSecurityExpressionHandler extends DefaultMethodSecurityExpressionHandler {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final PermissionService permissionService;

    public CustomMethodSecurityExpressionHandler(
            UserRepository userRepository,
            ProjectRepository projectRepository,
            PermissionService permissionService) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.permissionService = permissionService;
    }

    @Override
    protected MethodSecurityExpressionOperations createSecurityExpressionRoot(
            Authentication authentication, org.aopalliance.intercept.MethodInvocation invocation) {
        CustomMethodSecurityExpressionRoot root = new CustomMethodSecurityExpressionRoot(authentication);
        root.setUserRepository(userRepository);
        root.setProjectRepository(projectRepository);
        root.setPermissionService(permissionService);
        return root;
    }
}
