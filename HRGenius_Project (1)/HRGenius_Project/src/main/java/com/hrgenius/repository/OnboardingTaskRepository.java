package com.hrgenius.repository;
import com.hrgenius.entity.OnboardingTask;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface OnboardingTaskRepository extends JpaRepository<OnboardingTask, Long> {
 List<OnboardingTask> findByEmployeeId(Long employeeId);
}
