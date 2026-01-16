package com.smartproject.platform.controller;

import com.smartproject.platform.dto.TaskDependencyDTO;
import com.smartproject.platform.dto.TaskDependencyRequest;
import com.smartproject.platform.service.TaskDependencyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/task-dependencies")
@Tag(name = "Task Dependencies", description = "Task dependency management endpoints")
@SecurityRequirement(name = "bearerAuth")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class TaskDependencyController {

    private final TaskDependencyService dependencyService;

    @PostMapping
    @PreAuthorize("hasAuthority('USER') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    @Operation(summary = "Create task dependency", description = "Create a dependency relationship between tasks")
    public ResponseEntity<TaskDependencyDTO> createDependency(@Valid @RequestBody TaskDependencyRequest request) {
        return new ResponseEntity<>(dependencyService.createDependency(request), HttpStatus.CREATED);
    }

    @GetMapping("/task/{taskId}")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    @Operation(summary = "Get dependencies for task", description = "Get all tasks that this task depends on")
    public ResponseEntity<List<TaskDependencyDTO>> getDependencies(@PathVariable UUID taskId) {
        return ResponseEntity.ok(dependencyService.getDependenciesByTask(taskId));
    }

    @GetMapping("/task/{taskId}/dependent")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    @Operation(summary = "Get dependent tasks", description = "Get all tasks that depend on this task")
    public ResponseEntity<List<TaskDependencyDTO>> getDependentTasks(@PathVariable UUID taskId) {
        return ResponseEntity.ok(dependencyService.getDependentTasks(taskId));
    }

    @GetMapping("/task/{taskId}/all")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    @Operation(summary = "Get all related dependencies", description = "Get all dependency relationships for a task")
    public ResponseEntity<List<TaskDependencyDTO>> getAllRelatedDependencies(@PathVariable UUID taskId) {
        return ResponseEntity.ok(dependencyService.getAllRelatedDependencies(taskId));
    }

    @DeleteMapping("/{dependencyId}")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    @Operation(summary = "Delete dependency", description = "Remove a task dependency")
    public ResponseEntity<Void> deleteDependency(@PathVariable UUID dependencyId) {
        dependencyService.deleteDependency(dependencyId);
        return ResponseEntity.noContent().build();
    }
}
