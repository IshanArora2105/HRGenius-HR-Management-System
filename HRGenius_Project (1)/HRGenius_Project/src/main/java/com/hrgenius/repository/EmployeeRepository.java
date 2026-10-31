package com.hrgenius.repository;
import com.hrgenius.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
import org.springframework.data.domain.Page;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
 Page<Employee> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String name, String email, org.springframework.data.domain.Pageable pageable);
 Optional<Employee> findByEmail(String email);
 List<Employee> findByDepartmentId(Long departmentId);
 List<Employee> findByManagerId(Long managerId);
}
