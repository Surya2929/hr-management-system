package com.hrapp.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name="attendance",
    uniqueConstraints=@UniqueConstraint(columnNames={"employee_id","date"}))
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Attendance {

    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @JsonIgnoreProperties({"user","department","hibernateLazyInitializer","handler"})
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="employee_id", nullable=false)
    private Employee employee;

    @Column(nullable=false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private AttendanceStatus status;

    @Column(name="check_in")
    private LocalTime checkIn;

    @Column(name="check_out")
    private LocalTime checkOut;

    @Column(length=255)
    private String remarks;

    // Calculated by AttendanceService before sending - NOT saved in the database
    @Transient
    private Double workingHours; // checkOut - checkIn, e.g. 8.5

    @Transient
    private Boolean late;        // true if checkIn is after app.attendance.late-after

    public enum AttendanceStatus { PRESENT, ABSENT, HALF_DAY, ON_LEAVE }
}
