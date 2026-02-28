package com.smartproject.platform;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartproject.platform.model.Role;
import com.smartproject.platform.model.User;
import com.smartproject.platform.repository.UserRepository;
import com.smartproject.platform.security.jwt.JwtUtils;
import com.smartproject.platform.security.services.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.Set;
import java.util.UUID;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
public abstract class TestBase {

    @Autowired
    protected WebApplicationContext webApplicationContext;

    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired
    protected JwtUtils jwtUtils;

    protected User testUser;
    protected User testAdmin;
    protected String userToken;
    protected String adminToken;

    @BeforeEach
    void setUpBase() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        // Create test user
        testUser = User.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .email("test@example.com")
                .password(passwordEncoder.encode("password"))
                .roles(Set.of(Role.USER))
                .build();

        // Create test admin
        testAdmin = User.builder()
                .id(UUID.randomUUID())
                .username("admin")
                .email("admin@example.com")
                .password(passwordEncoder.encode("adminpass"))
                .roles(Set.of(Role.ADMIN))
                .build();

        testUser = userRepository.save(testUser);
        testAdmin = userRepository.save(testAdmin);

        // Generate JWT tokens
        UserDetailsImpl userPrincipal = UserDetailsImpl.build(testUser);
        UserDetailsImpl adminPrincipal = UserDetailsImpl.build(testAdmin);
        
        Authentication userAuth = new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());
        Authentication adminAuth = new UsernamePasswordAuthenticationToken(adminPrincipal, null, adminPrincipal.getAuthorities());
        
        userToken = jwtUtils.generateJwtToken(userAuth);
        adminToken = jwtUtils.generateJwtToken(adminAuth);
    }

    protected String createTestUser(String username, String email, Set<Role> roles) {
        User user = User.builder()
                .id(UUID.randomUUID())
                .username(username)
                .email(email)
                .password(passwordEncoder.encode("password"))
                .roles(roles)
                .build();
        
        user = userRepository.save(user);
        
        UserDetailsImpl userPrincipal = UserDetailsImpl.build(user);
        Authentication auth = new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());
        
        return jwtUtils.generateJwtToken(auth);
    }

    protected String asJsonString(final Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
