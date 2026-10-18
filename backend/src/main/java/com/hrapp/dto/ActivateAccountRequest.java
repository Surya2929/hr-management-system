package com.hrapp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Account activation by a new employee.
 * Step 1 (verify) sends email + temporaryPassword.
 * Step 2 (complete) sends the same two fields plus newPassword.
 */
@Data
public class ActivateAccountRequest {
    @NotBlank(message = "is required")
    @Email(message = "must be a valid email")
    private String email;

    @NotBlank(message = "is required")
    private String temporaryPassword;

    // Only needed for step 2. Same rules as every other password in the app.
    @Size(min = 8, max = 100, message = "must be 8 to 100 characters")
    @Pattern(regexp = ".*\\d.*", message = "must contain at least one number")
    private String newPassword;
}
