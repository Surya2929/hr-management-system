package com.hrapp.repository;
import com.hrapp.entity.LeaveRequest;
import com.hrapp.entity.LeaveRequest.LeaveStatus;
import com.hrapp.entity.LeaveRequest.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest,Long> {
    List<LeaveRequest> findByEmployeeIdOrderByAppliedAtDesc(Long employeeId);
    List<LeaveRequest> findByStatusOrderByAppliedAtAsc(LeaveStatus status);
    List<LeaveRequest> findAllByOrderByAppliedAtDesc();

    // Leaves of one employee with a given status, except one type (used to count paid leave days)
    List<LeaveRequest> findByEmployeeIdAndStatusAndLeaveTypeNot(Long employeeId, LeaveStatus status, LeaveType leaveType);

    @Query("SELECT COUNT(lr)>0 FROM LeaveRequest lr WHERE lr.employee.id=:eid AND lr.status<>'REJECTED' AND lr.fromDate<=:to AND lr.toDate>=:from")
    boolean existsOverlappingLeave(@Param("eid") Long eid, @Param("from") LocalDate from, @Param("to") LocalDate to);

    long countByStatus(LeaveStatus status);

    // Used when HR deletes an employee: removes all their LeaveRequest rows in one query
    @Modifying
    @Query("DELETE FROM LeaveRequest x WHERE x.employee.id = :employeeId")
    void deleteAllByEmployeeId(@Param("employeeId") Long employeeId);
}
