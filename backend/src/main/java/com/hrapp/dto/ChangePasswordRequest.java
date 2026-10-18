package com.hrapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequest {
    @NotBlank(message = "is required")
    private String oldPassword;

    @NotBlank(message = "is required")
    @Size(min = 8, max = 100, message = "must be 8 to 100 characters")
    @Pattern(regexp = ".*\\d.*", message = "must contain at least one number")
    private String newPassword;
}
