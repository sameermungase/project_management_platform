package com.smartproject.platform.config;

import com.smartproject.platform.model.Priority;
import com.smartproject.platform.model.Project;
import com.smartproject.platform.model.Role;
import com.smartproject.platform.model.Task;
import com.smartproject.platform.model.TaskStatus;
import com.smartproject.platform.model.User;
import com.smartproject.platform.repository.ProjectRepository;
import com.smartproject.platform.repository.TaskRepository;
import com.smartproject.platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;

@Configuration
@Profile("dev")
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            // Create Admin User
            if (!userRepository.existsByUsername("admin")) {
                User admin = User.builder()
                        .username("admin")
                        .email("admin@smartproject.com")
                        .password(passwordEncoder.encode("admin123"))
                        .roles(new HashSet<>(Collections.singletonList(Role.ADMIN)))
                        .build();
                userRepository.save(admin);
            }

            // Create Standard User
            if (!userRepository.existsByUsername("john_doe")) {
                User user = User.builder()
                        .username("john_doe")
                        .email("john@smartproject.com")
                        .password(passwordEncoder.encode("password"))
                        .roles(new HashSet<>(Collections.singletonList(Role.USER)))
                        .build();
                User savedUser = userRepository.save(user);

                // Create Sample Project
                Project project = Project.builder()
                        .name("Smart Collaboration App")
                        .description("Developing the next gen project management tool")
                        .owner(savedUser)
                        .members(new HashSet<>(Collections.singletonList(savedUser)))
                        .build();
                Project savedProject = projectRepository.save(project);
                
                // Create Sample Tasks
                Task task1 = Task.builder()
                        .title("Design database schema")
                        .description("Create ER diagram and design database tables for users, projects, and tasks")
                        .status(TaskStatus.IN_PROGRESS)
                        .priority(Priority.HIGH)
                        .dueDate(LocalDate.now().plusDays(7))
                        .project(savedProject)
                        .assignee(savedUser)
                        .build();
                taskRepository.save(task1);
                
                Task task2 = Task.builder()
                        .title("Implement authentication")
                        .description("Set up JWT-based authentication and authorization")
                        .status(TaskStatus.TO_DO)
                        .priority(Priority.HIGH)
                        .dueDate(LocalDate.now().plusDays(14))
                        .project(savedProject)
                        .assignee(savedUser)
                        .build();
                taskRepository.save(task2);
                
                Task task3 = Task.builder()
                        .title("Create REST API endpoints")
                        .description("Implement CRUD operations for projects and tasks")
                        .status(TaskStatus.TO_DO)
                        .priority(Priority.MEDIUM)
                        .dueDate(LocalDate.now().plusDays(21))
                        .project(savedProject)
                        .build();
                taskRepository.save(task3);
                
                Task task4 = Task.builder()
                        .title("Write unit tests")
                        .description("Add comprehensive unit tests for all services")
                        .status(TaskStatus.TO_DO)
                        .priority(Priority.MEDIUM)
                        .dueDate(LocalDate.now().plusDays(28))
                        .project(savedProject)
                        .build();
                taskRepository.save(task4);
            }
        };
    }
}
