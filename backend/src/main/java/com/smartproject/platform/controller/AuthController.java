package com.smartproject.platform.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartproject.platform.model.Role;
import com.smartproject.platform.model.User;
import com.smartproject.platform.repository.UserRepository;
import com.smartproject.platform.security.jwt.JwtUtils;
import com.smartproject.platform.security.services.UserDetailsImpl;
import com.smartproject.platform.dto.JwtResponse;
import com.smartproject.platform.dto.LoginRequest;
import com.smartproject.platform.dto.MessageResponse;
import com.smartproject.platform.dto.SignupRequest;

@Tag(name = "Authentication", description = "Authentication and registration endpoints")
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
  @Autowired
  AuthenticationManager authenticationManager;

  @Autowired
  UserRepository userRepository;

  @Autowired
  PasswordEncoder encoder;

  @Autowired
  JwtUtils jwtUtils;

  @Operation(
          summary = "User login",
          description = "Authenticate user with username and password, returns JWT token"
  )
  @ApiResponses(value = {
          @ApiResponse(
                  responseCode = "200",
                  description = "Login successful",
                  content = @Content(schema = @Schema(implementation = JwtResponse.class))
          ),
          @ApiResponse(
                  responseCode = "401",
                  description = "Invalid credentials",
                  content = @Content(schema = @Schema(implementation = MessageResponse.class))
          )
  })
  @PostMapping("/signin")
  public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

    SecurityContextHolder.getContext().setAuthentication(authentication);
    String jwt = jwtUtils.generateJwtToken(authentication);
    
    UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();    
    List<String> roles = userDetails.getAuthorities().stream()
        .map(item -> item.getAuthority())
        .collect(Collectors.toList());

    return ResponseEntity.ok(new JwtResponse(jwt, 
                         userDetails.getId(), 
                         userDetails.getUsername(), 
                         userDetails.getEmail(), 
                         roles));
  }

  @Operation(
          summary = "User registration",
          description = "Register a new user account with username, email, and password"
  )
  @ApiResponses(value = {
          @ApiResponse(
                  responseCode = "200",
                  description = "Registration successful",
                  content = @Content(schema = @Schema(implementation = MessageResponse.class))
          ),
          @ApiResponse(
                  responseCode = "400",
                  description = "Username or email already exists",
                  content = @Content(schema = @Schema(implementation = MessageResponse.class))
          )
  })
  @PostMapping("/signup")
  public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
    if (userRepository.existsByUsername(signUpRequest.getUsername())) {
      return ResponseEntity
          .badRequest()
          .body(new MessageResponse("Error: Username is already taken!"));
    }

    if (userRepository.existsByEmail(signUpRequest.getEmail())) {
      return ResponseEntity
          .badRequest()
          .body(new MessageResponse("Error: Email is already in use!"));
    }

    // Create new user's account
    User user = User.builder()
            .username(signUpRequest.getUsername())
            .email(signUpRequest.getEmail())
            .password(encoder.encode(signUpRequest.getPassword()))
            .build();

    Set<String> strRoles = signUpRequest.getRole();
    Set<Role> roles = new HashSet<>();

    if (strRoles == null) {
      roles.add(Role.USER);
    } else {
      strRoles.forEach(role -> {
        switch (role) {
        case "admin":
          roles.add(Role.ADMIN);
          break;
        case "manager":
          roles.add(Role.MANAGER);
          break;
        case "technical_lead":
        case "tech_lead":
          roles.add(Role.TECHNICAL_LEAD);
          break;
        default:
          roles.add(Role.USER);
        }
      });
    }

    user.setRoles(roles);
    userRepository.save(user);

    return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
  }
}
