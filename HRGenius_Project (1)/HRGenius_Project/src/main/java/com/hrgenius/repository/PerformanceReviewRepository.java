package com.hrgenius.repository;
import com.hrgenius.entity.PerformanceReview;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface PerformanceReviewRepository extends JpaRepository<PerformanceReview, Long> {
 List<PerformanceReview> findByEmployeeId(Long employeeId);
 List<PerformanceReview> findByCycleId(Long cycleId);
}
