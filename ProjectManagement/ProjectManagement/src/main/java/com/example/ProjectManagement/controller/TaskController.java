package com.example.ProjectManagement.controller;

import com.example.ProjectManagement.DTO.AssignRequest;
import com.example.ProjectManagement.model.Task;
import com.example.ProjectManagement.model.User;
import com.example.ProjectManagement.repository.TaskRepository;
import com.example.ProjectManagement.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
	private final TaskRepository taskRepository;
	private final UserRepository userRepository;
	public TaskController(TaskRepository taskRepository, UserRepository userRepository) {
		this.taskRepository = taskRepository;
		this.userRepository = userRepository;
    }
	@GetMapping("/mine")
	public List<Task> myTasks(Authentication authentication) {
		User me = userRepository.findByUsername(authentication.getName()).orElseThrow();
		return taskRepository.findByAssignedTo(me);
	}
	@GetMapping("/assigned-by-me")
	@PreAuthorize("hasRole('MANAGER')")
	public List<Task> assignedByMe(Authentication authentication) {
		User me = userRepository.findByUsername(authentication.getName()).orElseThrow();
		return taskRepository.findByAssignedBy(me);
	}
	@PostMapping
	@PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<?> assign(@Valid @RequestBody AssignRequest req, Authentication auth) {
		User manager = userRepository.findByUsername(auth.getName()).orElseThrow();
		User employee = userRepository.findByUsername(req.getAssignedTo()).orElseThrow();
		Task task = Task.builder()
				.title(req.getTitle())
				.description(req.getDescription())
				.assignedBy(manager)
				.assignedTo(employee)
				.status("PENDING")
                .dueDate(req.getDueDate() == null || req.getDueDate().isBlank() ? null : LocalDate.parse(req.getDueDate()))
				.build();
		taskRepository.save(task);
		return ResponseEntity.ok(task);
	}

	@PatchMapping("/{id}/status")
	public ResponseEntity<?> updateStatus(@PathVariable Long id,@RequestParam String status, Authentication auth) {
		Task task = taskRepository.findById(id).orElseThrow();
		User me = userRepository.findByUsername(auth.getName()).orElseThrow();
		if (!task.getAssignedTo().getId().equals(me.getId()) && !task.getAssignedBy().getId().equals(me.getId())) {
			return ResponseEntity.status(403).body("errorForbidden");
		}
		task.setStatus(status);
		taskRepository.save(task);
		return ResponseEntity.ok(task);
	}
}



