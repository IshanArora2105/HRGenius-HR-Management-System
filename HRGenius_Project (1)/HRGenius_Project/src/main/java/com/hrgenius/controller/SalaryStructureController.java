package com.hrgenius.controller;

import com.hrgenius.entity.Department;
import com.hrgenius.entity.Employee;
import com.hrgenius.entity.SalaryStructure;
import com.hrgenius.repository.DepartmentRepository;
import com.hrgenius.repository.EmployeeRepository;
import com.hrgenius.repository.SalaryStructureRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/v1/salary-structures")
public class SalaryStructureController {
 private final SalaryStructureRepository salaryRepo;
 private final EmployeeRepository employeeRepo;
 private final DepartmentRepository deptRepo;

 public SalaryStructureController(SalaryStructureRepository salaryRepo, EmployeeRepository employeeRepo, DepartmentRepository deptRepo) {
  this.salaryRepo = salaryRepo;
  this.employeeRepo = employeeRepo;
  this.deptRepo = deptRepo;
 }

 @GetMapping
 @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER','MANAGER')")
 public List<Map<String, Object>> listAll() {
  Map<Long, Department> depts = new HashMap<>();
  for (Department d : deptRepo.findAll()) {
   depts.put(d.getId(), d);
  }

  Map<Long, Employee> emps = new HashMap<>();
  for (Employee e : employeeRepo.findAll()) {
   emps.put(e.getId(), e);
  }

  List<Map<String, Object>> result = new ArrayList<>();
  for (SalaryStructure s : salaryRepo.findAll()) {
   Employee emp = emps.get(s.getEmployeeId());
   Map<String, Object> map = new LinkedHashMap<>();
   map.put("id", s.getId());
   map.put("employeeId", s.getEmployeeId());
   map.put("employeeCode", emp != null ? emp.getEmployeeCode() : "EMP-" + s.getEmployeeId());
   map.put("employeeName", emp != null ? emp.getName() : "Employee #" + s.getEmployeeId());
   map.put("designation", emp != null ? emp.getDesignation() : "Staff");
   String deptName = (emp != null && emp.getDepartmentId() != null && depts.containsKey(emp.getDepartmentId()))
       ? depts.get(emp.getDepartmentId()).getName() : "General";
   map.put("departmentName", deptName);
   BigDecimal basic = s.getBasic() != null ? s.getBasic() : BigDecimal.ZERO;
   BigDecimal hra = s.getHra() != null ? s.getHra() : BigDecimal.ZERO;
   BigDecimal other = s.getOtherAllowances() != null ? s.getOtherAllowances() : BigDecimal.ZERO;
   BigDecimal deductions = s.getDeductions() != null ? s.getDeductions() : BigDecimal.ZERO;
   BigDecimal gross = basic.add(hra).add(other);
   BigDecimal net = gross.subtract(deductions);

   map.put("basic", basic);
   map.put("basicPay", basic);
   map.put("hra", hra);
   map.put("otherAllowances", other);
   map.put("allowances", other);
   map.put("deductions", deductions);
   map.put("grossPay", gross);
   map.put("netPay", net);
   result.add(map);
  }
  return result;
 }

 @GetMapping("/me")
 public ResponseEntity<?> getMyStructure(Authentication auth) {
  String username = auth != null ? auth.getName() : "admin";
  Employee emp = employeeRepo.findByEmail(username)
      .or(() -> employeeRepo.findAll().stream().filter(e -> username.equalsIgnoreCase(e.getEmail())).findFirst())
      .orElseGet(() -> employeeRepo.findAll().stream().findFirst().orElse(null));

  if (emp == null) {
   return ResponseEntity.badRequest().body(Map.of("message", "No employee profile found"));
  }
  return getStructure(emp.getId());
 }

 @GetMapping("/{employeeId}")
 public ResponseEntity<?> getStructure(@PathVariable Long employeeId) {
  SalaryStructure s = salaryRepo.findByEmployeeId(employeeId)
      .orElseThrow(() -> new NoSuchElementException("Salary structure not defined for employee ID " + employeeId));

  Employee emp = employeeRepo.findById(employeeId).orElse(null);

  BigDecimal basic = s.getBasic() != null ? s.getBasic() : BigDecimal.ZERO;
  BigDecimal hra = s.getHra() != null ? s.getHra() : BigDecimal.ZERO;
  BigDecimal other = s.getOtherAllowances() != null ? s.getOtherAllowances() : BigDecimal.ZERO;
  BigDecimal deductions = s.getDeductions() != null ? s.getDeductions() : BigDecimal.ZERO;
  BigDecimal gross = basic.add(hra).add(other);
  BigDecimal net = gross.subtract(deductions);

  Map<String, Object> map = new LinkedHashMap<>();
  map.put("id", s.getId());
  map.put("employeeId", s.getEmployeeId());
  map.put("employeeCode", emp != null ? emp.getEmployeeCode() : "EMP-" + employeeId);
  map.put("employeeName", emp != null ? emp.getName() : "Employee #" + employeeId);
  map.put("designation", emp != null ? emp.getDesignation() : "Staff");
  map.put("basic", basic);
  map.put("basicPay", basic);
  map.put("hra", hra);
  map.put("otherAllowances", other);
  map.put("allowances", other);
  map.put("deductions", deductions);
  map.put("grossPay", gross);
  map.put("netPay", net);
  return ResponseEntity.ok(map);
 }

 @PostMapping
 @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER')")
 public ResponseEntity<?> createOrUpdate(@RequestBody Map<String, Object> body) {
  if (body.get("employeeId") == null) {
   return ResponseEntity.badRequest().body(Map.of("message", "Employee ID is required"));
  }
  Long employeeId = ((Number) body.get("employeeId")).longValue();
  Employee emp = employeeRepo.findById(employeeId)
      .orElseThrow(() -> new NoSuchElementException("Employee not found with ID " + employeeId));

  SalaryStructure s = salaryRepo.findByEmployeeId(employeeId).orElseGet(() -> {
   SalaryStructure ns = new SalaryStructure();
   ns.setEmployeeId(employeeId);
   return ns;
  });

  BigDecimal basic = body.get("basic") != null ? new BigDecimal(String.valueOf(body.get("basic"))) : BigDecimal.ZERO;
  BigDecimal hra = body.get("hra") != null ? new BigDecimal(String.valueOf(body.get("hra"))) : BigDecimal.ZERO;
  BigDecimal other = body.get("otherAllowances") != null ? new BigDecimal(String.valueOf(body.get("otherAllowances"))) : BigDecimal.ZERO;

  // Use user-defined deductions if specified by user; otherwise standard PF (12% of basic)
  BigDecimal deductions;
  if (body.get("deductions") != null && !String.valueOf(body.get("deductions")).isBlank()) {
   deductions = new BigDecimal(String.valueOf(body.get("deductions")));
  } else {
   deductions = basic.multiply(new BigDecimal("0.12")).setScale(2, java.math.RoundingMode.HALF_UP);
  }

  s.setBasic(basic);
  s.setHra(hra);
  s.setOtherAllowances(other);
  s.setDeductions(deductions);

  SalaryStructure saved = salaryRepo.save(s);
  return ResponseEntity.ok(Map.of(
      "message", "Salary structure saved successfully for " + emp.getName(),
      "id", saved.getId(),
      "employeeId", saved.getEmployeeId()
  ));
 }

 @DeleteMapping("/{id}")
 @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER')")
 public ResponseEntity<?> delete(@PathVariable Long id) {
  salaryRepo.deleteById(id);
  return ResponseEntity.noContent().build();
 }
}
