package com.example.ProjectManagement.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssignRequest{
    @NotBlank String title;
    String description;
    @NotBlank String assignedTo;
    String dueDate;
}