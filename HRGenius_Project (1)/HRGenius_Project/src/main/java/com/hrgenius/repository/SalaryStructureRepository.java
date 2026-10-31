package com.hrgenius.repository;
import com.hrgenius.entity.SalaryStructure;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface SalaryStructureRepository extends JpaRepository<SalaryStructure, Long> {


 Optional<SalaryStructure> findByEmployeeId(Long employeeId);

}
