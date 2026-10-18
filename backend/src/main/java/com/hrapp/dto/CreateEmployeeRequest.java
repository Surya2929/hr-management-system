package com.hrapp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** HR fills this to add a new hire. No password - the system generates a temporary one. */
@Data
public class CreateEmployeeRequest {
    @NotBlank(message = "is required")
    @Email(message = "must be a valid email")
    @Size(max = 150, message = "must be at most 150 characters")
    private String email;

    @NotBlank(message = "is required")
    @Size(max = 100, message = "must be at most 100 characters")
    private String firstName;

    @NotBlank(message = "is required")
    @Size(max = 100, message = "must be at most 100 characters")
    private String lastName;

    @Size(max = 20, message = "must be at most 20 characters")
    private String phone;

    @Size(max = 100, message = "must be at most 100 characters")
    private String designation;

    private Long departmentId; // optional
}
