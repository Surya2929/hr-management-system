package com.hrapp.dto;

import com.hrapp.entity.Employee;
import lombok.AllArgsConstructor;
import lombok.Data;

/** Returned to HR once: the new employee plus the temporary password to hand over. */
@Data
@AllArgsConstructor
public class CreateEmployeeResponse {
    private Employee employee;
    private String temporaryPassword; // shown only once, stored only as a BCrypt hash
}
