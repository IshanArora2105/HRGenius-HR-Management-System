package com.hrgenius.controller;

import com.hrgenius.entity.Employee;
import com.hrgenius.entity.OnboardingTask;
import com.hrgenius.repository.EmployeeRepository;
import com.hrgenius.repository.OnboardingTaskRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/v1/onboarding")
public class OnboardingController {
 private final OnboardingTaskRepository taskRepo;
 private final EmployeeRepository employeeRepo;

 public OnboardingController(OnboardingTaskRepository taskRepo, EmployeeRepository employeeRepo) {
  this.taskRepo = taskRepo;
  this.employeeRepo = employeeRepo;
 }

 @GetMapping
 @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER','MANAGER')")
 public List<Map<String, Object>> getAllTasks(@RequestParam(required = false) Long employeeId) {
  Map<Long, Employee> emps = new HashMap<>();
  for (Employee e : employeeRepo.findAll()) {
   emps.put(e.getId(), e);
  }

  List<OnboardingTask> tasks = employeeId != null
      ? taskRepo.findByEmployeeId(employeeId)
      : taskRepo.findAll();

  List<Map<String, Object>> list = new ArrayList<>();
  for (OnboardingTask t : tasks) {
   Employee emp = emps.get(t.getEmployeeId());
   Map<String, Object> map = new LinkedHashMap<>();
   map.put("id", t.getId());
   map.put("employeeId", t.getEmployeeId());
   map.put("employeeName", emp != null ? emp.getName() : "Employee #" + t.getEmployeeId());
   map.put("employeeCode", emp != null ? emp.getEmployeeCode() : "EMP-" + t.getEmployeeId());
   map.put("title", t.getTaskName());
   map.put("taskName", t.getTaskName());
   map.put("description", t.getDescription());
   map.put("status", t.getStatus());
   map.put("dueDate", t.getDueDate());
   list.add(map);
  }
  return list;
 }

 @GetMapping("/employee/{employeeId}")
 public List<Map<String, Object>> getEmployeeTasks(@PathVariable Long employeeId) {
  Employee emp = employeeRepo.findById(employeeId).orElse(null);
  String empName = emp != null ? emp.getName() : "Employee #" + employeeId;

  List<Map<String, Object>> list = new ArrayList<>();
  for (OnboardingTask t : taskRepo.findByEmployeeId(employeeId)) {
   Map<String, Object> map = new LinkedHashMap<>();
   map.put("id", t.getId());
   map.put("employeeId", t.getEmployeeId());
   map.put("employeeName", empName);
   map.put("title", t.getTaskName());
   map.put("taskName", t.getTaskName());
   map.put("description", t.getDescription());
   map.put("status", t.getStatus());
   map.put("dueDate", t.getDueDate());
   list.add(map);
  }
  return list;
 }

 @PostMapping("/tasks")
 @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER')")
 public ResponseEntity<?> createTask(@RequestBody Map<String, Object> body) {
  OnboardingTask t = new OnboardingTask();
  if (body.get("employeeId") != null) {
   t.setEmployeeId(((Number) body.get("employeeId")).longValue());
  }
  t.setTaskName((String) body.getOrDefault("title", "Onboarding Task"));
  t.setDescription((String) body.get("description"));
  if (body.get("dueDate") != null && !String.valueOf(body.get("dueDate")).isBlank()) {
   try {
    t.setDueDate(LocalDate.parse((String) body.get("dueDate")));
   } catch (Exception ignored) {
    t.setDueDate(LocalDate.now().plusDays(7));
   }
  } else {
   t.setDueDate(LocalDate.now().plusDays(7));
  }
  t.setStatus("PENDING");
  return ResponseEntity.status(201).body(taskRepo.save(t));
 }

 @PutMapping("/tasks/{id}")
 public ResponseEntity<?> updateTask(@PathVariable Long id, @RequestBody Map<String, String> body) {
  OnboardingTask t = taskRepo.findById(id).orElseThrow(() -> new NoSuchElementException("Task not found"));
  String status = body.get("status");
  if (status != null) {
   t.setStatus(status);
   taskRepo.save(t);
  }
  return ResponseEntity.ok(t);
 }
}
