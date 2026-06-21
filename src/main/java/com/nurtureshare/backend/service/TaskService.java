package com.nurtureshare.backend.service;

import com.nurtureshare.backend.dto.request.CreateTaskCommentRequest;
import com.nurtureshare.backend.dto.request.CreateTaskRequest;
import com.nurtureshare.backend.dto.request.UpdateTaskRequest;
import com.nurtureshare.backend.dto.response.TaskCommentResponse;
import com.nurtureshare.backend.dto.response.TaskProgressResponse;
import com.nurtureshare.backend.dto.response.TaskResponse;
import com.nurtureshare.backend.dto.response.UserResponse;
import com.nurtureshare.backend.exception.ResourceNotFoundException;
import com.nurtureshare.backend.exception.UnauthorizedException;
import com.nurtureshare.backend.model.Couple;
import com.nurtureshare.backend.model.Task;
import com.nurtureshare.backend.model.TaskComment;
import com.nurtureshare.backend.model.User;
import com.nurtureshare.backend.model.enums.TaskAssignment;
import com.nurtureshare.backend.model.enums.TaskStatus;
import com.nurtureshare.backend.repository.TaskCommentRepository;
import com.nurtureshare.backend.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class TaskService {

    private final CoupleService coupleService;
    private final TaskRepository taskRepository;
    private final TaskCommentRepository taskCommentRepository;

    public List<TaskResponse> getTasks(User currentUser, String filter) {
        Couple couple = coupleService.getCoupleForUser(currentUser);
        UUID coupleId = couple.getId();

        List<Task> tasks;
        if (filter == null || filter.isBlank()) {
            tasks = taskRepository.findByCoupleId(coupleId);
        } else {
            switch (filter.toUpperCase()) {
                case "ME" -> tasks = taskRepository.findByCoupleIdAndAssignedTo(coupleId, TaskAssignment.ME);
                case "PARTNER" -> tasks = taskRepository.findByCoupleIdAndAssignedTo(coupleId, TaskAssignment.PARTNER);
                case "BOTH" -> tasks = taskRepository.findByCoupleIdAndAssignedTo(coupleId, TaskAssignment.BOTH);
                case "COMPLETED" -> tasks = taskRepository.findByCoupleIdAndStatus(coupleId, TaskStatus.COMPLETED);
                default -> tasks = taskRepository.findByCoupleId(coupleId);
            }
        }

        return tasks.stream().map(this::toTaskResponse).collect(Collectors.toList());
    }

    public TaskResponse createTask(User currentUser, CreateTaskRequest req) {
        Couple couple = coupleService.getCoupleForUser(currentUser);

        Task task = Task.builder()
                .couple(couple)
                .createdBy(currentUser)
                .title(req.getTitle())
                .description(req.getDescription())
                .assignedTo(req.getAssignedTo())
                .dueDate(req.getDueDate())
                .status(TaskStatus.PENDING)
                .nudgeOnSave(req.isNudgeOnSave())
                .build();
        task = taskRepository.save(task);

        if (req.isNudgeOnSave()) {
            log.info("NUDGE sent to partner for task: {}", task.getTitle());
        }

        return toTaskResponse(task);
    }

    public TaskResponse updateTask(User currentUser, UUID taskId, UpdateTaskRequest req) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        if (!task.getCouple().hasUser(currentUser.getId())) {
            throw new UnauthorizedException("You are not authorized to update this task.");
        }

        if (req.getTitle() != null) task.setTitle(req.getTitle());
        if (req.getDescription() != null) task.setDescription(req.getDescription());
        if (req.getAssignedTo() != null) task.setAssignedTo(req.getAssignedTo());
        if (req.getDueDate() != null) task.setDueDate(req.getDueDate());
        if (req.getStatus() != null) task.setStatus(req.getStatus());
        if (req.getNudgeOnSave() != null) task.setNudgeOnSave(req.getNudgeOnSave());

        task = taskRepository.save(task);
        return toTaskResponse(task);
    }

    public void deleteTask(User currentUser, UUID taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        if (!task.getCouple().hasUser(currentUser.getId())) {
            throw new UnauthorizedException("You are not authorized to delete this task.");
        }

        taskRepository.delete(task);
    }

    public void nudgePartner(User currentUser, UUID taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        if (!task.getCouple().hasUser(currentUser.getId())) {
            throw new UnauthorizedException("You are not authorized to nudge for this task.");
        }

        log.info("NUDGE sent to partner for task: {}", task.getTitle());
    }

    public TaskProgressResponse getProgress(User currentUser) {
        Couple couple = coupleService.getCoupleForUser(currentUser);
        UUID coupleId = couple.getId();

        long total = taskRepository.countByCoupleId(coupleId);
        long completed = taskRepository.countByCoupleIdAndStatus(coupleId, TaskStatus.COMPLETED);
        long pending = total - completed;
        int percent = total == 0 ? 0 : (int) ((completed * 100.0) / total);

        return TaskProgressResponse.builder()
                .total(total)
                .completed(completed)
                .pending(pending)
                .percentComplete(percent)
                .build();
    }

    public List<TaskCommentResponse> getComments(User currentUser, UUID taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        if (!task.getCouple().hasUser(currentUser.getId())) {
            throw new UnauthorizedException("You are not authorized to view comments for this task.");
        }

        return taskCommentRepository.findByTaskIdOrderByCreatedAtAsc(taskId)
                .stream()
                .map(this::toTaskCommentResponse)
                .collect(Collectors.toList());
    }

    public TaskCommentResponse addComment(User currentUser, UUID taskId, CreateTaskCommentRequest req) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        if (!task.getCouple().hasUser(currentUser.getId())) {
            throw new UnauthorizedException("You are not authorized to comment on this task.");
        }

        TaskComment comment = TaskComment.builder()
                .task(task)
                .author(currentUser)
                .content(req.getContent())
                .build();
        comment = taskCommentRepository.save(comment);

        task.setCommentCount(task.getCommentCount() + 1);
        taskRepository.save(task);

        return toTaskCommentResponse(comment);
    }

    private TaskResponse toTaskResponse(Task task) {
        UserResponse createdByResponse = UserResponse.builder()
                .id(task.getCreatedBy().getId())
                .email(task.getCreatedBy().getEmail())
                .name(task.getCreatedBy().getName())
                .avatarUrl(task.getCreatedBy().getAvatarUrl())
                .createdAt(task.getCreatedBy().getCreatedAt())
                .build();

        return TaskResponse.builder()
                .id(task.getId())
                .coupleId(task.getCouple().getId())
                .createdBy(createdByResponse)
                .assignedTo(task.getAssignedTo())
                .title(task.getTitle())
                .description(task.getDescription())
                .dueDate(task.getDueDate())
                .status(task.getStatus())
                .nudgeOnSave(task.isNudgeOnSave())
                .commentCount(task.getCommentCount())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }

    private TaskCommentResponse toTaskCommentResponse(TaskComment comment) {
        UserResponse authorResponse = UserResponse.builder()
                .id(comment.getAuthor().getId())
                .email(comment.getAuthor().getEmail())
                .name(comment.getAuthor().getName())
                .avatarUrl(comment.getAuthor().getAvatarUrl())
                .createdAt(comment.getAuthor().getCreatedAt())
                .build();

        return TaskCommentResponse.builder()
                .id(comment.getId())
                .taskId(comment.getTask().getId())
                .author(authorResponse)
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
