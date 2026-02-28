package com.smartproject.platform.service;

import com.smartproject.platform.dto.ProjectDTO;
import com.smartproject.platform.dto.ProjectRequest;
import com.smartproject.platform.exception.ResourceNotFoundException;
import com.smartproject.platform.exception.UnauthorizedException;
import com.smartproject.platform.mapper.ProjectMapper;
import com.smartproject.platform.model.Project;
import com.smartproject.platform.model.Role;
import com.smartproject.platform.model.User;
import com.smartproject.platform.repository.ProjectRepository;
import com.smartproject.platform.repository.UserRepository;
import com.smartproject.platform.security.services.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private ActivityLogService activityLogService;

    @InjectMocks
    private ProjectService projectService;

    private User testUser;
    private User testMember;
    private Project testProject;
    private ProjectRequest projectRequest;
    private ProjectDTO projectDTO;
    private UUID projectId;
    private UUID userId;
    private UUID memberId;

    @BeforeEach
    void setUp() {
        projectId = UUID.randomUUID();
        userId = UUID.randomUUID();
        memberId = UUID.randomUUID();

        testUser = User.builder()
                .id(userId)
                .username("testuser")
                .email("test@example.com")
                .roles(Set.of(Role.USER))
                .build();

        testMember = User.builder()
                .id(memberId)
                .username("member")
                .email("member@example.com")
                .roles(Set.of(Role.USER))
                .build();

        testProject = Project.builder()
                .id(projectId)
                .name("Test Project")
                .description("Test Description")
                .owner(testUser)
                .members(new HashSet<>())
                .build();

        projectRequest = new ProjectRequest();
        projectRequest.setName("Test Project");
        projectRequest.setDescription("Test Description");
        projectRequest.setMemberIds(Set.of(memberId));

        projectDTO = new ProjectDTO();
        projectDTO.setId(projectId);
        projectDTO.setName("Test Project");
        projectDTO.setDescription("Test Description");

        setupSecurityContext(testUser);
    }

    private void setupSecurityContext(User user) {
        UserDetailsImpl userDetails = UserDetailsImpl.build(user);
        Authentication authentication = mock(Authentication.class);
        lenient().when(authentication.getPrincipal()).thenReturn(userDetails);
        
        SecurityContext securityContext = mock(SecurityContext.class);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        
        SecurityContextHolder.setContext(securityContext);
        
        lenient().when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
    }

    @Test
    @DisplayName("Should create project successfully with valid data")
    void createProject_Success() {
        // Arrange
        lenient().when(userRepository.findById(memberId)).thenReturn(Optional.of(testMember));
        when(projectMapper.toEntity(projectRequest)).thenReturn(testProject);
        when(projectRepository.save(testProject)).thenReturn(testProject);
        when(projectMapper.toDTO(testProject)).thenReturn(projectDTO);

        // Act
        ProjectDTO result = projectService.createProject(projectRequest);

        // Assert
        assertNotNull(result);
        assertEquals(projectDTO.getId(), result.getId());
        assertEquals(projectDTO.getName(), result.getName());
        assertEquals(projectDTO.getDescription(), result.getDescription());

        verify(projectRepository).save(testProject);
        verify(activityLogService).logActivity(eq(testUser), eq("PROJECT_CREATED"), 
            contains("Test Project"), eq(projectId), eq("PROJECT"));
    }

    @Test
    @DisplayName("Should create project without members when memberIds is null")
    void createProject_NoMembers() {
        // Arrange
        projectRequest.setMemberIds(null);
        testProject.setMembers(new HashSet<>());
        
        when(projectMapper.toEntity(projectRequest)).thenReturn(testProject);
        when(projectRepository.save(testProject)).thenReturn(testProject);
        when(projectMapper.toDTO(testProject)).thenReturn(projectDTO);

        // Act
        ProjectDTO result = projectService.createProject(projectRequest);

        // Assert
        assertNotNull(result);
        verify(projectRepository).save(testProject);
        verify(activityLogService).logActivity(eq(testUser), eq("PROJECT_CREATED"), 
            contains("Test Project"), eq(projectId), eq("PROJECT"));
    }

    @Test
    @DisplayName("Should get project by id when user is owner")
    void getProjectById_Success_Owner() {
        // Arrange
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));
        when(projectMapper.toDTO(testProject)).thenReturn(projectDTO);

        // Act
        ProjectDTO result = projectService.getProjectById(projectId);

        // Assert
        assertNotNull(result);
        assertEquals(projectDTO.getId(), result.getId());
        verify(projectRepository).findById(projectId);
        verify(projectMapper).toDTO(testProject);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when project does not exist")
    void getProjectById_ProjectNotFound() {
        // Arrange
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            projectService.getProjectById(projectId);
        });

        verify(projectRepository).findById(projectId);
        verify(projectMapper, never()).toDTO(any());
    }

    @Test
    @DisplayName("Should update project successfully when user is owner")
    void updateProject_Success() {
        // Arrange
        ProjectRequest updateRequest = new ProjectRequest();
        updateRequest.setName("Updated Project");
        updateRequest.setDescription("Updated Description");
        updateRequest.setMemberIds(Set.of(memberId));

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));
        lenient().when(userRepository.findById(memberId)).thenReturn(Optional.of(testMember));
        when(projectRepository.save(testProject)).thenReturn(testProject);
        when(projectMapper.toDTO(testProject)).thenReturn(projectDTO);

        // Act
        ProjectDTO result = projectService.updateProject(projectId, updateRequest);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Project", testProject.getName());
        assertEquals("Updated Description", testProject.getDescription());
        verify(projectRepository).save(testProject);
        verify(activityLogService).logActivity(eq(testUser), eq("PROJECT_UPDATED"), 
            eq("Updated project details"), eq(projectId), eq("PROJECT"));
    }

    @Test
    @DisplayName("Should throw UnauthorizedException when non-owner tries to update project")
    void updateProject_Unauthorized() {
        // Arrange
        User anotherUser = User.builder()
                .id(UUID.randomUUID())
                .username("anotheruser")
                .roles(Set.of(Role.USER))
                .build();
        setupSecurityContext(anotherUser);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));

        // Act & Assert
        assertThrows(UnauthorizedException.class, () -> {
            projectService.updateProject(projectId, projectRequest);
        });

        verify(projectRepository, never()).save(any());
        verify(activityLogService, never()).logActivity(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should delete project successfully when user is owner")
    void deleteProject_Success() {
        // Arrange
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));

        // Act
        assertDoesNotThrow(() -> {
            projectService.deleteProject(projectId);
        });

        // Assert
        verify(projectRepository).delete(testProject);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when trying to delete non-existent project")
    void deleteProject_ProjectNotFound() {
        // Arrange
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            projectService.deleteProject(projectId);
        });

        verify(projectRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should throw UnauthorizedException when non-owner tries to delete project")
    void deleteProject_Unauthorized() {
        // Arrange
        User anotherUser = User.builder()
                .id(UUID.randomUUID())
                .username("anotheruser")
                .roles(Set.of(Role.USER))
                .build();
        setupSecurityContext(anotherUser);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));

        // Act & Assert
        assertThrows(UnauthorizedException.class, () -> {
            projectService.deleteProject(projectId);
        });

        verify(projectRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should add member to project successfully when user has permission")
    void addMemberToProject_Success() {
        // Arrange
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));
        when(userRepository.findById(memberId)).thenReturn(Optional.of(testMember));
        when(projectRepository.save(testProject)).thenReturn(testProject);

        // Act
        assertDoesNotThrow(() -> {
            projectService.addMemberToProject(projectId, memberId);
        });

        // Assert
        assertTrue(testProject.getMembers().contains(testMember));
        verify(projectRepository).save(testProject);
        verify(activityLogService).logActivity(eq(testUser), eq("MEMBER_ADDED"), 
            contains("member"), eq(projectId), eq("PROJECT"));
    }

    @Test
    @DisplayName("Should not add member if already exists in project")
    void addMemberToProject_AlreadyMember() {
        // Arrange
        testProject.getMembers().add(testMember);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));
        when(userRepository.findById(memberId)).thenReturn(Optional.of(testMember));

        // Act
        assertDoesNotThrow(() -> {
            projectService.addMemberToProject(projectId, memberId);
        });

        // Assert
        verify(projectRepository, never()).save(any());
        verify(activityLogService, never()).logActivity(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should remove member from project successfully")
    void removeMemberFromProject_Success() {
        // Arrange
        testProject.getMembers().add(testMember);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));
        when(userRepository.findById(memberId)).thenReturn(Optional.of(testMember));
        when(projectRepository.save(testProject)).thenReturn(testProject);

        // Act
        assertDoesNotThrow(() -> {
            projectService.removeMemberFromProject(projectId, memberId);
        });

        // Assert
        assertFalse(testProject.getMembers().contains(testMember));
        verify(projectRepository).save(testProject);
        verify(activityLogService).logActivity(eq(testUser), eq("MEMBER_REMOVED"), 
            contains("member"), eq(projectId), eq("PROJECT"));
    }

    @Test
    @DisplayName("Should throw UnauthorizedException when trying to remove project owner")
    void removeMemberFromProject_RemoveOwner() {
        // Arrange
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));

        // Act & Assert
        assertThrows(UnauthorizedException.class, () -> {
            projectService.removeMemberFromProject(projectId, userId);
        });

        verify(projectRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should get all projects for user with pagination")
    void getAllProjects_Success() {
        // Arrange
        List<Project> projects = Arrays.asList(testProject);
        Page<Project> projectPage = new PageImpl<>(projects);
        Pageable pageable = PageRequest.of(0, 10);

        when(projectRepository.findByOwnerIdOrMembersId(userId, pageable))
                .thenReturn(projectPage);
        when(projectMapper.toDTO(testProject)).thenReturn(projectDTO);

        // Act
        Page<ProjectDTO> result = projectService.getAllProjects(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(projectDTO, result.getContent().get(0));
        verify(projectRepository).findByOwnerIdOrMembersId(userId, pageable);
    }
}
