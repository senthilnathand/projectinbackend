package com.example.ProjectManagement.config;
import com.example.ProjectManagement.model.User;
import com.example.ProjectManagement.model.Task;
import com.example.ProjectManagement.repository.UserRepository;
import com.example.ProjectManagement.repository.TaskRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;
import java.time.LocalDate;

@Configuration
public class DataSeeder {

	@Bean
    CommandLineRunner seedUsers(UserRepository userRepository, TaskRepository taskRepository, PasswordEncoder passwordEncoder) {
		return args -> {
			if (!userRepository.existsByUsername("manager")) {
				userRepository.save(User.builder()
						.username("manager")
						.passwordHash(passwordEncoder.encode("manager123"))
						.roles("MANAGER")
						.build());
			}
			if (!userRepository.existsByUsername("employee")) {
				userRepository.save(User.builder()
						.username("employee")
						.passwordHash(passwordEncoder.encode("employee123"))
						.roles("EMPLOYEE")
						.build());
			}
            if (taskRepository.count() == 0) {
                User manager = userRepository.findByUsername("manager").orElseThrow();
                User employee = userRepository.findByUsername("employee").orElseThrow();
                taskRepository.save(Task.builder()
                        .title("Prepare Q4 KPI report")
                        .description("Collect and summarize KPI metrics for Q4.")
                        .assignedBy(manager)
                        .assignedTo(employee)
                        .status("PENDING")
                        .dueDate(LocalDate.now().plusDays(7))
                        .build());
                taskRepository.save(Task.builder()
                        .title("Update onboarding guide")
                        .description("Revise the onboarding manual with latest screenshots.")
                        .assignedBy(manager)
                        .assignedTo(employee)
                        .status("IN_PROGRESS")
                        .dueDate(LocalDate.now().plusDays(3))
                        .build());
                taskRepository.save(Task.builder()
                        .title("Archive old tickets")
                        .description("Clean up resolved tickets from last quarter.")
                        .assignedBy(manager)
                        .assignedTo(employee)
                        .status("COMPLETED")
                        .dueDate(LocalDate.now().minusDays(1))
                        .build());
            }
		};
	}
}




