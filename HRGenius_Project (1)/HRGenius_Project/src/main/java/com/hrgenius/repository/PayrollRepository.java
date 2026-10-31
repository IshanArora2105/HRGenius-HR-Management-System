package com.hrgenius.repository;
import com.hrgenius.entity.Payroll;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface PayrollRepository extends JpaRepository<Payroll, Long> {
 boolean existsByEmployeeIdAndMonthAndYear(Long employeeId, Integer month, Integer year);
 List<Payroll> findByEmployeeIdOrderByYearDescMonthDesc(Long employeeId);
 Optional<Payroll> findByEmployeeIdAndMonthAndYear(Long employeeId, Integer month, Integer year);
}
