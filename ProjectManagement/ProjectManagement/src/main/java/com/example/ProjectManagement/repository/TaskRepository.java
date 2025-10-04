package com.example.ProjectManagement.repository;

import com.example.ProjectManagement.model.Task;
import com.example.ProjectManagement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
	List<Task> findByAssignedTo(User assignedTo);
	List<Task> findByAssignedBy(User assignedBy);
}




