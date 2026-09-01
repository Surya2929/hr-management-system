package com.hrapp.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity @Table(name="leave_requests")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class LeaveRequest {

    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @JsonIgnoreProperties({"user","department","hibernateLazyInitializer","handler"})
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="employee_id", nullable=false)
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(name="leave_type", nullable=false)
    private LeaveType leaveType;

    @Column(name="from_date", nullable=false)
    private LocalDate fromDate;

    @Column(name="to_date", nullable=false)
    private LocalDate toDate;

    @Column(columnDefinition="TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private LeaveStatus status = LeaveStatus.PENDING;

    @Column(name="hr_remarks", length=255)
    private String hrRemarks;

    @Column(name="applied_at", updatable=false)
    private LocalDateTime appliedAt;

    @Column(name="reviewed_at")
    private LocalDateTime reviewedAt;

    @PrePersist protected void onCreate() { appliedAt = LocalDateTime.now(); }

    public enum LeaveType  { SICK, CASUAL, EARNED, UNPAID }
    public enum LeaveStatus { PENDING, APPROVED, REJECTED }
}
