package com.hrapp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "is required")
    @Email(message = "must be a valid email")
    @Size(max = 150, message = "must be at most 150 characters")
    private String email;

    @NotBlank(message = "is required")
    @Size(min = 8, max = 100, message = "must be 8 to 100 characters")
    @Pattern(regexp = ".*\\d.*", message = "must contain at least one number")
    private String password;

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
