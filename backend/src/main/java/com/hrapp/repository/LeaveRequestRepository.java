package com.hrapp.repository;
import com.hrapp.entity.LeaveRequest;
import com.hrapp.entity.LeaveRequest.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest,Long> {
    List<LeaveRequest> findByEmployeeIdOrderByAppliedAtDesc(Long employeeId);
    List<LeaveRequest> findByStatusOrderByAppliedAtAsc(LeaveStatus status);
    List<LeaveRequest> findAllByOrderByAppliedAtDesc();

    @Query("SELECT COALESCE(SUM(DATEDIFF(lr.toDate, lr.fromDate)+1),0) FROM LeaveRequest lr WHERE lr.employee.id=:eid AND lr.status='APPROVED' AND YEAR(lr.fromDate)=:year")
    Long countApprovedDaysInYear(@Param("eid") Long employeeId, @Param("year") int year);

    @Query("SELECT COUNT(lr)>0 FROM LeaveRequest lr WHERE lr.employee.id=:eid AND lr.status<>'REJECTED' AND lr.fromDate<=:to AND lr.toDate>=:from")
    boolean existsOverlappingLeave(@Param("eid") Long eid, @Param("from") LocalDate from, @Param("to") LocalDate to);
}
