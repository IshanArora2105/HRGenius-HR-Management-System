package com.hrgenius.repository;
import com.hrgenius.entity.Goal;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface GoalRepository extends JpaRepository<Goal, Long> {
 List<Goal> findByEmployeeId(Long employeeId);
 List<Goal> findByCycleId(Long cycleId);
}
