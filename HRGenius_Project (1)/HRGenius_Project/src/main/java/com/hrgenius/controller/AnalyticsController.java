package com.hrgenius.controller;

import com.hrgenius.entity.*;
import com.hrgenius.repository.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Month;
import java.util.*;

@RestController
@RequestMapping("/api/v1/analytics")
@PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER')")
public class AnalyticsController {
 private final EmployeeRepository employeeRepo;
 private final DepartmentRepository deptRepo;
 private final CandidateRepository candidateRepo;
 private final PayrollRepository payrollRepo;
 private final SalaryStructureRepository salaryRepo;
 private final PerformanceReviewRepository reviewRepo;

 public AnalyticsController(
     EmployeeRepository employeeRepo,
     DepartmentRepository deptRepo,
     CandidateRepository candidateRepo,
     PayrollRepository payrollRepo,
     SalaryStructureRepository salaryRepo,
     PerformanceReviewRepository reviewRepo
 ) {
  this.employeeRepo = employeeRepo;
  this.deptRepo = deptRepo;
  this.candidateRepo = candidateRepo;
  this.payrollRepo = payrollRepo;
  this.salaryRepo = salaryRepo;
  this.reviewRepo = reviewRepo;
 }

 @GetMapping("/headcount")
 public Map<String, Object> getHeadcount() {
  List<Employee> employees = employeeRepo.findAll();
  long activeCount = employees.stream().filter(e -> "ACTIVE".equalsIgnoreCase(e.getStatus())).count();

  Map<Long, String> deptNames = new HashMap<>();
  for (Department d : deptRepo.findAll()) {
   deptNames.put(d.getId(), d.getName());
  }

  Map<String, Integer> deptCounts = new LinkedHashMap<>();
  for (Employee e : employees) {
   if ("ACTIVE".equalsIgnoreCase(e.getStatus())) {
    String dept = (e.getDepartmentId() != null && deptNames.containsKey(e.getDepartmentId()))
        ? deptNames.get(e.getDepartmentId())
        : "Unassigned";
    deptCounts.put(dept, deptCounts.getOrDefault(dept, 0) + 1);
  }
  }

  List<Map<String, Object>> chart = new ArrayList<>();
  if (deptCounts.isEmpty()) {
   chart.add(Map.of("label", "General", "value", (int) activeCount));
  } else {
   deptCounts.forEach((dept, count) -> chart.add(Map.of("label", dept, "value", count)));
  }

  return Map.of(
      "headcount", activeCount,
      "total", activeCount,
      "data", chart
  );
 }

 @GetMapping("/hiring")
 public Map<String, Object> getHiring() {
  List<Candidate> candidates = candidateRepo.findAll();
  long hires = candidates.stream()
      .filter(c -> "SELECTED".equalsIgnoreCase(c.getApplicationStatus()))
      .count();

  Map<String, Integer> statusCounts = new LinkedHashMap<>();
  statusCounts.put("APPLIED", 0);
  statusCounts.put("SHORTLISTED", 0);
  statusCounts.put("INTERVIEWED", 0);
  statusCounts.put("SELECTED", 0);
  statusCounts.put("REJECTED", 0);

  for (Candidate c : candidates) {
   String s = c.getApplicationStatus() != null ? c.getApplicationStatus().toUpperCase() : "APPLIED";
   statusCounts.put(s, statusCounts.getOrDefault(s, 0) + 1);
  }

  List<Map<String, Object>> chart = new ArrayList<>();
  statusCounts.forEach((status, count) -> chart.add(Map.of("label", status, "value", count)));

  return Map.of(
      "totalHires", hires,
      "total", candidates.size(),
      "data", chart
  );
 }

 @GetMapping("/attrition")
 public Map<String, Object> getAttrition() {
  List<Employee> employees = employeeRepo.findAll();
  long active = employees.stream().filter(e -> "ACTIVE".equalsIgnoreCase(e.getStatus())).count();
  long inactive = employees.stream().filter(e -> "INACTIVE".equalsIgnoreCase(e.getStatus())).count();

  List<Map<String, Object>> chart = new ArrayList<>();
  chart.add(Map.of("label", "Active Staff", "value", (int) active));
  chart.add(Map.of("label", "Inactive / Exited", "value", (int) inactive));

  return Map.of(
      "totalAttrition", inactive,
      "total", inactive,
      "data", chart
  );
 }

 @GetMapping("/payroll-cost")
 public Map<String, Object> getPayrollCost() {
  List<Payroll> payrollList = payrollRepo.findAll();
  BigDecimal totalCost = BigDecimal.ZERO;
  Map<String, BigDecimal> monthlyTotals = new LinkedHashMap<>();

  if (!payrollList.isEmpty()) {
   for (Payroll p : payrollList) {
    totalCost = totalCost.add(p.getNetPay());
    String label = Month.of(Math.max(1, Math.min(12, p.getMonth()))).name().substring(0, 3) + " " + p.getYear();
    monthlyTotals.put(label, monthlyTotals.getOrDefault(label, BigDecimal.ZERO).add(p.getNetPay()));
   }
  } else {
   // Compute from user-defined active salary structures
   for (SalaryStructure s : salaryRepo.findAll()) {
    BigDecimal gross = s.getBasic().add(s.getHra()).add(s.getOtherAllowances());
    BigDecimal net = gross.subtract(s.getDeductions());
    totalCost = totalCost.add(net);
   }
   if (totalCost.compareTo(BigDecimal.ZERO) > 0) {
    monthlyTotals.put("Current Est.", totalCost);
   }
  }

  List<Map<String, Object>> chart = new ArrayList<>();
  monthlyTotals.forEach((period, amt) -> chart.add(Map.of("label", period, "value", amt.intValue())));

  return Map.of(
      "totalCost", "₹" + totalCost.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString(),
      "total", totalCost.intValue(),
      "data", chart
  );
 }

 @GetMapping("/performance")
 public Map<String, Object> getPerformance() {
  List<PerformanceReview> reviews = reviewRepo.findAll();
  Map<Integer, Integer> distribution = new LinkedHashMap<>();
  for (int i = 1; i <= 5; i++) distribution.put(i, 0);

  for (PerformanceReview r : reviews) {
   if (r.getRating() != null && r.getRating() >= 1 && r.getRating() <= 5) {
    distribution.put(r.getRating(), distribution.get(r.getRating()) + 1);
   }
  }

  List<Map<String, Object>> chart = new ArrayList<>();
  distribution.forEach((star, count) -> chart.add(Map.of("label", star + " Stars", "value", count)));

  return Map.of(
      "totalReviews", reviews.size(),
      "total", reviews.size(),
      "data", chart
  );
 }
}
