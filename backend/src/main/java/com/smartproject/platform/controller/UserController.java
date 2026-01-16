package com.smartproject.platform.controller;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.smartproject.platform.dto.MessageResponse;
import com.smartproject.platform.dto.OrganizationUserDTO;
import com.smartproject.platform.dto.UpdateUserRoleRequest;
import com.smartproject.platform.dto.UserDetailDTO;
import com.smartproject.platform.exception.ResourceNotFoundException;
import com.smartproject.platform.exception.UnauthorizedException;
import com.smartproject.platform.model.Role;
import com.smartproject.platform.model.User;
import com.smartproject.platform.repository.UserRepository;
import com.smartproject.platform.security.services.UserDetailsImpl;
import com.smartproject.platform.service.UserService;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "Endpoints for managing users")
@SecurityRequirement(name = "bearerAuth")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('TECHNICAL_LEAD') or hasAuthority('MANAGER')")
    @Operation(summary = "List all users", description = "Get a list of all registered users. Accessible by Admin, TL and Manager.")
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> new UserDTO(user.getId(), user.getUsername(), user.getEmail(), user.getRoles()))
                .collect(Collectors.toList());
    }

    @GetMapping("/profile")
    @Operation(summary = "Get current user profile", description = "Get detailed profile of the authenticated user")
    public ResponseEntity<UserDetailDTO> getCurrentUserProfile() {
        return ResponseEntity.ok(userService.getUserProfile(getCurrentUserId()));
    }

    @GetMapping("/profile/{id}")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER') or hasAuthority('TECHNICAL_LEAD')")
    @Operation(summary = "Get user profile by ID", description = "Get detailed profile of any user (Admin, Manager, TL only)")
    public ResponseEntity<UserDetailDTO> getUserProfile(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUserProfile(id));
    }

    @GetMapping("/organization")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Get all organization users", description = "Get detailed list of all users with their projects and tasks (Admin only)")
    public ResponseEntity<List<OrganizationUserDTO>> getOrganizationUsers() {
        return ResponseEntity.ok(userService.getOrganizationUsers());
    }

    @PutMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Update user roles", description = "Update roles for a user (Admin only)")
    public ResponseEntity<UserDTO> updateUserRoles(@PathVariable UUID id, @Valid @RequestBody UpdateUserRoleRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        user.setRoles(request.getRoles());
        User updatedUser = userRepository.save(user);
        
        return ResponseEntity.ok(new UserDTO(updatedUser.getId(), updatedUser.getUsername(), 
                                             updatedUser.getEmail(), updatedUser.getRoles()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('TECHNICAL_LEAD')")
    @Operation(summary = "Delete user", description = "Delete a user by ID. Admins can delete anyone. TLs can only delete USERs.")
    public ResponseEntity<?> deleteUser(@PathVariable UUID id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        
        // Security check for TL
        User targetUser = userRepository.findById(id).get();
        if (isTechLead() && !isTargetSimpleUser(targetUser)) {
             return ResponseEntity.status(403).body(new MessageResponse("Error: Technical Leads can only delete normal Users."));
        }

        userRepository.deleteById(id);
        return ResponseEntity.ok(new MessageResponse("User deleted successfully!"));
    }

    // Helper methods for security checks
    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("User not authenticated");
        }
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userDetails.getId();
    }

    private boolean isTechLead() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("TECHNICAL_LEAD"));
    }

    private boolean isTargetSimpleUser(User user) {
        // Check if target has ONLY user role or NO privileged roles
        Set<Role> roles = user.getRoles();
        return roles.size() == 1 && roles.contains(Role.USER);
    }
    
    // DTO for user list to avoid exposing password
    @Data
    public static class UserDTO {
        private UUID id;
        private String username;
        private String email;
        private Set<Role> roles;

        public UserDTO(UUID id, String username, String email, Set<Role> roles) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.roles = roles;
        }
    }
}
