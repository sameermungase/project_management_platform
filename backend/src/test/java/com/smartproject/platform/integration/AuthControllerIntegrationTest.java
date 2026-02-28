package com.smartproject.platform.integration;

import com.smartproject.platform.TestBase;
import com.smartproject.platform.dto.LoginRequest;
import com.smartproject.platform.dto.SignupRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureWebMvc

class AuthControllerIntegrationTest extends TestBase {

    @Test
    @DisplayName("Should register and authenticate user successfully - full flow")
    void registerAndAuthenticateUser_Success() throws Exception {
        // Arrange
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("integrationuser");
        signupRequest.setEmail("integration@example.com");
        signupRequest.setPassword("password123");
        signupRequest.setRole(Set.of("user"));

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("integrationuser");
        loginRequest.setPassword("password123");

        // Act & Assert - Register user
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(signupRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User registered successfully!"));

        // Act & Assert - Authenticate user
        MvcResult result = mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.username").value("integrationuser"))
                .andExpect(jsonPath("$.email").value("integration@example.com"))
                .andExpect(jsonPath("$.roles").isArray())
                .andExpect(jsonPath("$.roles[0]").value("USER"))
                .andReturn();

        String token = result.getResponse().getContentAsString();
        assertTrue(token.contains("\"token\":"));
    }

    @Test
    @DisplayName("Should register admin user successfully")
    void registerAdminUser_Success() throws Exception {
        // Arrange
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("adminuser");
        signupRequest.setEmail("adminuser@example.com");
        signupRequest.setPassword("adminpass123");
        signupRequest.setRole(Set.of("admin"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(signupRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User registered successfully!"));

        // Verify admin can login
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("adminuser");
        loginRequest.setPassword("adminpass123");

        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles[0]").value("ADMIN"));
    }

    @Test
    @DisplayName("Should fail authentication with wrong password")
    void authenticateUser_WrongPassword() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("wrongpassword");

        // Act & Assert
        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should fail authentication with non-existent user")
    void authenticateUser_NonExistentUser() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("nonexistentuser");
        loginRequest.setPassword("password");

        // Act & Assert
        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should fail registration with duplicate username")
    void registerUser_DuplicateUsername() throws Exception {
        // Arrange
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("testuser"); // Already exists from TestBase
        signupRequest.setEmail("newemail@example.com");
        signupRequest.setPassword("password123");
        signupRequest.setRole(Set.of("user"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(signupRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error: Username is already taken!"));
    }

    @Test
    @DisplayName("Should fail registration with duplicate email")
    void registerUser_DuplicateEmail() throws Exception {
        // Arrange
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("newusername");
        signupRequest.setEmail("test@example.com"); // Already exists from TestBase
        signupRequest.setPassword("password123");
        signupRequest.setRole(Set.of("user"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(signupRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error: Email is already in use!"));
    }

    @Test
    @DisplayName("Should fail registration with invalid email format")
    void registerUser_InvalidEmail() throws Exception {
        // Arrange
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("newuser");
        signupRequest.setEmail("invalid-email");
        signupRequest.setPassword("password123");
        signupRequest.setRole(Set.of("user"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(signupRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should fail registration with short password")
    void registerUser_ShortPassword() throws Exception {
        // Arrange
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("newuser");
        signupRequest.setEmail("newuser@example.com");
        signupRequest.setPassword("123"); // Too short
        signupRequest.setRole(Set.of("user"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(signupRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should fail registration with short username")
    void registerUser_ShortUsername() throws Exception {
        // Arrange
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("ab"); // Too short
        signupRequest.setEmail("newuser@example.com");
        signupRequest.setPassword("password123");
        signupRequest.setRole(Set.of("user"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(signupRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle empty request body gracefully")
    void authenticateUser_EmptyRequestBody() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle malformed JSON gracefully")
    void authenticateUser_MalformedJson() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"username\": \"test\", \"password\": }"))
                .andExpect(status().isBadRequest());
    }
}
