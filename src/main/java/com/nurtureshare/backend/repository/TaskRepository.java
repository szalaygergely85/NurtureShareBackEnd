package com.nurtureshare.backend.repository;

import com.nurtureshare.backend.model.Task;
import com.nurtureshare.backend.model.enums.TaskAssignment;
import com.nurtureshare.backend.model.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {
    List<Task> findByCoupleId(UUID coupleId);
    List<Task> findByCoupleIdAndStatus(UUID coupleId, TaskStatus status);
    List<Task> findByCoupleIdAndAssignedTo(UUID coupleId, TaskAssignment assignedTo);
    long countByCoupleId(UUID coupleId);
    long countByCoupleIdAndStatus(UUID coupleId, TaskStatus status);
}
