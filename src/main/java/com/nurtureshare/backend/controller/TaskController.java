package com.nurtureshare.backend.controller;

import com.nurtureshare.backend.dto.request.CreateTaskCommentRequest;
import com.nurtureshare.backend.dto.request.CreateTaskRequest;
import com.nurtureshare.backend.dto.request.UpdateTaskRequest;
import com.nurtureshare.backend.dto.response.ApiResponse;
import com.nurtureshare.backend.dto.response.TaskCommentResponse;
import com.nurtureshare.backend.dto.response.TaskProgressResponse;
import com.nurtureshare.backend.dto.response.TaskResponse;
import com.nurtureshare.backend.model.User;
import com.nurtureshare.backend.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController extends BaseController {

    private final TaskService taskService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getTasks(
            @RequestParam(required = false) String filter) {
        User currentUser = getCurrentUser();
        List<TaskResponse> tasks = taskService.getTasks(currentUser, filter);
        return ResponseEntity.ok(ApiResponse.ok(tasks));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(
            @Valid @RequestBody CreateTaskRequest request) {
        User currentUser = getCurrentUser();
        TaskResponse task = taskService.createTask(currentUser, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Task created successfully", task));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTask(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTaskRequest request) {
        User currentUser = getCurrentUser();
        TaskResponse task = taskService.updateTask(currentUser, id, request);
        return ResponseEntity.ok(ApiResponse.ok("Task updated successfully", task));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable UUID id) {
        User currentUser = getCurrentUser();
        taskService.deleteTask(currentUser, id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/nudge")
    public ResponseEntity<ApiResponse<Void>> nudgePartner(@PathVariable UUID id) {
        User currentUser = getCurrentUser();
        taskService.nudgePartner(currentUser, id);
        return ResponseEntity.ok(ApiResponse.ok("Nudge sent to partner!", null));
    }

    @GetMapping("/progress")
    public ResponseEntity<ApiResponse<TaskProgressResponse>> getProgress() {
        User currentUser = getCurrentUser();
        TaskProgressResponse progress = taskService.getProgress(currentUser);
        return ResponseEntity.ok(ApiResponse.ok(progress));
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<ApiResponse<List<TaskCommentResponse>>> getComments(@PathVariable UUID id) {
        User currentUser = getCurrentUser();
        List<TaskCommentResponse> comments = taskService.getComments(currentUser, id);
        return ResponseEntity.ok(ApiResponse.ok(comments));
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<ApiResponse<TaskCommentResponse>> addComment(
            @PathVariable UUID id,
            @Valid @RequestBody CreateTaskCommentRequest request) {
        User currentUser = getCurrentUser();
        TaskCommentResponse comment = taskService.addComment(currentUser, id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Comment added successfully", comment));
    }
}
