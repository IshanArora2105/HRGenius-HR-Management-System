package com.hrgenius.controller;

import com.hrgenius.entity.*;
import com.hrgenius.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/v1/performance")
public class PerformanceController {
 private final PerformanceCycleRepository cycleRepo;
 private final GoalRepository goalRepo;
 private final PerformanceReviewRepository reviewRepo;
 private final EmployeeRepository employeeRepo;

 public PerformanceController(
     PerformanceCycleRepository cycleRepo,
     GoalRepository goalRepo,
     PerformanceReviewRepository reviewRepo,
     EmployeeRepository employeeRepo
 ) {
  this.cycleRepo = cycleRepo;
  this.goalRepo = goalRepo;
  this.reviewRepo = reviewRepo;
  this.employeeRepo = employeeRepo;
 }

 @GetMapping("/cycles")
 public List<PerformanceCycle> listCycles() {
  return cycleRepo.findAll();
 }

 @PostMapping("/cycles")
 @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER')")
 public ResponseEntity<?> createCycle(@RequestBody Map<String, Object> body) {
  PerformanceCycle c = new PerformanceCycle();
  c.setName((String) body.get("name"));
  if (body.get("startDate") != null) {
   c.setStartDate(LocalDate.parse((String) body.get("startDate")));
  } else {
   c.setStartDate(LocalDate.now());
  }
  if (body.get("endDate") != null) {
   c.setEndDate(LocalDate.parse((String) body.get("endDate")));
  } else {
   c.setEndDate(LocalDate.now().plusMonths(6));
  }
  c.setStatus("ACTIVE");
  return ResponseEntity.status(201).body(cycleRepo.save(c));
 }

 @GetMapping("/goals")
 public List<Map<String, Object>> listGoals(@RequestParam(required = false) Long employeeId) {
  Map<Long, Employee> emps = new HashMap<>();
  for (Employee e : employeeRepo.findAll()) {
   emps.put(e.getId(), e);
  }

  List<Goal> goals = employeeId != null
      ? goalRepo.findByEmployeeId(employeeId)
      : goalRepo.findAll();

  List<Map<String, Object>> result = new ArrayList<>();
  for (Goal g : goals) {
   Employee emp = emps.get(g.getEmployeeId());
   Map<String, Object> map = new LinkedHashMap<>();
   map.put("id", g.getId());
   map.put("employeeId", g.getEmployeeId());
   map.put("employeeName", emp != null ? emp.getName() : "Employee #" + g.getEmployeeId());
   map.put("title", g.getTitle());
   map.put("description", g.getDescription());
   map.put("status", g.getStatus());
   map.put("dueDate", g.getDueDate());
   result.add(map);
  }
  return result;
 }

 @PostMapping("/goals")
 @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER','MANAGER')")
 public ResponseEntity<?> createGoal(@RequestBody Map<String, Object> body, Authentication auth) {
  Goal g = new Goal();
  if (body.get("employeeId") != null) {
   g.setEmployeeId(((Number) body.get("employeeId")).longValue());
  }
  g.setTitle((String) body.get("title"));
  g.setDescription((String) body.get("description"));
  if (body.get("dueDate") != null) {
   try {
    g.setDueDate(LocalDate.parse((String) body.get("dueDate")));
   } catch (Exception ignored) {
    g.setDueDate(LocalDate.now().plusMonths(3));
   }
  }
  g.setStatus("IN_PROGRESS");
  // Link to active cycle if any
  cycleRepo.findAll().stream().findFirst().ifPresent(c -> g.setCycleId(c.getId()));
  return ResponseEntity.status(201).body(goalRepo.save(g));
 }

 @PostMapping("/self-assessments")
 public ResponseEntity<?> submitSelfAssessment(@RequestBody Map<String, Object> body, Authentication auth) {
  Long cycleId = ((Number) body.get("cycleId")).longValue();
  String assessmentText = (String) body.get("selfAssessment");

  String username = auth != null ? auth.getName() : "employee";
  Employee emp = employeeRepo.findByEmail(username)
      .or(() -> employeeRepo.findAll().stream().findFirst())
      .orElseThrow(() -> new NoSuchElementException("Employee profile not found"));

  PerformanceReview review = reviewRepo.findByEmployeeId(emp.getId()).stream()
      .filter(r -> r.getCycleId().equals(cycleId))
      .findFirst()
      .orElseGet(() -> {
       PerformanceReview nr = new PerformanceReview();
       nr.setEmployeeId(emp.getId());
       nr.setCycleId(cycleId);
       return nr;
      });

  review.setSelfAssessment(assessmentText);
  review.setStatus("ASSESSED");
  reviewRepo.save(review);
  return ResponseEntity.ok(Map.of("message", "Self-assessment submitted successfully"));
 }

 @PostMapping("/reviews")
 @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER','MANAGER')")
 public ResponseEntity<?> createReview(@RequestBody Map<String, Object> body, Authentication auth) {
  Long employeeId = ((Number) body.get("employeeId")).longValue();
  Long cycleId = ((Number) body.get("cycleId")).longValue();
  Integer rating = ((Number) body.get("rating")).intValue();
  String comments = (String) body.get("comments");

  PerformanceReview review = reviewRepo.findByEmployeeId(employeeId).stream()
      .filter(r -> r.getCycleId().equals(cycleId))
      .findFirst()
      .orElseGet(() -> {
       PerformanceReview nr = new PerformanceReview();
       nr.setEmployeeId(employeeId);
       nr.setCycleId(cycleId);
       return nr;
      });

  review.setRating(rating);
  review.setComments(comments);
  review.setStatus("COMPLETED");
  return ResponseEntity.ok(reviewRepo.save(review));
 }

 @GetMapping("/reviews")
 public List<Map<String, Object>> getAllReviews() {
  Map<Long, String> cycleNames = new HashMap<>();
  for (PerformanceCycle c : cycleRepo.findAll()) {
   cycleNames.put(c.getId(), c.getName());
  }

  Map<Long, Employee> emps = new HashMap<>();
  for (Employee e : employeeRepo.findAll()) {
   emps.put(e.getId(), e);
  }

  List<Map<String, Object>> list = new ArrayList<>();
  for (PerformanceReview r : reviewRepo.findAll()) {
   Employee emp = emps.get(r.getEmployeeId());
   Map<String, Object> map = new LinkedHashMap<>();
   map.put("id", r.getId());
   map.put("cycleId", r.getCycleId());
   map.put("cycleName", cycleNames.getOrDefault(r.getCycleId(), "Cycle #" + r.getCycleId()));
   map.put("employeeId", r.getEmployeeId());
   map.put("employeeName", emp != null ? emp.getName() : "Employee #" + r.getEmployeeId());
   map.put("rating", r.getRating() != null ? r.getRating() : "—");
   map.put("status", r.getStatus() != null ? r.getStatus() : "SUBMITTED");
   map.put("comments", r.getComments() != null ? r.getComments() : r.getSelfAssessment());
   map.put("selfAssessment", r.getSelfAssessment());
   map.put("feedback", r.getComments());
   list.add(map);
  }
  return list;
 }

 @GetMapping("/reviews/me")
 public List<Map<String, Object>> getMyReviews(Authentication auth) {
  String username = auth != null ? auth.getName() : "employee";
  Employee emp = employeeRepo.findByEmail(username)
      .or(() -> employeeRepo.findAll().stream().filter(e -> username.equalsIgnoreCase(e.getEmail())).findFirst())
      .orElseGet(() -> employeeRepo.findAll().stream().findFirst().orElse(null));

  if (emp == null) return List.of();
  return getReviews(emp.getId());
 }

 @GetMapping("/reviews/{employeeId}")
 public List<Map<String, Object>> getReviews(@PathVariable Long employeeId) {
  Map<Long, String> cycleNames = new HashMap<>();
  for (PerformanceCycle c : cycleRepo.findAll()) {
   cycleNames.put(c.getId(), c.getName());
  }

  Employee emp = employeeRepo.findById(employeeId).orElse(null);
  String empName = emp != null ? emp.getName() : "Employee #" + employeeId;

  List<Map<String, Object>> list = new ArrayList<>();
  for (PerformanceReview r : reviewRepo.findByEmployeeId(employeeId)) {
   Map<String, Object> map = new LinkedHashMap<>();
   map.put("id", r.getId());
   map.put("cycleId", r.getCycleId());
   map.put("cycleName", cycleNames.getOrDefault(r.getCycleId(), "Cycle #" + r.getCycleId()));
   map.put("employeeId", r.getEmployeeId());
   map.put("employeeName", empName);
   map.put("rating", r.getRating() != null ? r.getRating() : "—");
   map.put("status", r.getStatus() != null ? r.getStatus() : "SUBMITTED");
   map.put("comments", r.getComments() != null ? r.getComments() : r.getSelfAssessment());
   map.put("selfAssessment", r.getSelfAssessment());
   map.put("feedback", r.getComments());
   list.add(map);
  }
  return list;
 }
}
