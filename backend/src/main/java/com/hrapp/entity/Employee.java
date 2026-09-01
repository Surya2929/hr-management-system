package com.hrapp.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity @Table(name="employees")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Employee {

    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @JsonIgnoreProperties({"password","hibernateLazyInitializer","handler"})
    @OneToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="user_id", nullable=false, unique=true)
    private User user;

    @JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="department_id")
    private Department department;

    @Column(name="first_name", nullable=false, length=100)
    private String firstName;

    @Column(name="last_name", nullable=false, length=100)
    private String lastName;

    @Column(length=20)
    private String phone;

    @Column(length=100)
    private String designation;

    @Column(precision=12, scale=2)
    private BigDecimal salary;

    @Column(name="date_joined")
    private LocalDate dateJoined;

    @Column(name="created_at", updatable=false)
    private LocalDateTime createdAt;

    @Column(name="updated_at")
    private LocalDateTime updatedAt;

    @PrePersist  protected void onCreate() { createdAt = updatedAt = LocalDateTime.now(); }
    @PreUpdate   protected void onUpdate() { updatedAt = LocalDateTime.now(); }
}
