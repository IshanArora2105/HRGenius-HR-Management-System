package com.hrgenius.controller;

import com.hrgenius.entity.Candidate;
import com.hrgenius.entity.Department;
import com.hrgenius.entity.Employee;
import com.hrgenius.entity.JobOpening;
import com.hrgenius.entity.OnboardingTask;
import com.hrgenius.repository.CandidateRepository;
import com.hrgenius.repository.DepartmentRepository;
import com.hrgenius.repository.EmployeeRepository;
import com.hrgenius.repository.JobOpeningRepository;
import com.hrgenius.repository.OnboardingTaskRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/v1/candidates")
public class CandidateController {
 private final CandidateRepository candidateRepo;
 private final JobOpeningRepository jobRepo;
 private final EmployeeRepository employeeRepo;
 private final DepartmentRepository deptRepo;
 private final OnboardingTaskRepository onboardingRepo;

 public CandidateController(
     CandidateRepository candidateRepo,
     JobOpeningRepository jobRepo,
     EmployeeRepository employeeRepo,
     DepartmentRepository deptRepo,
     OnboardingTaskRepository onboardingRepo
 ) {
  this.candidateRepo = candidateRepo;
  this.jobRepo = jobRepo;
  this.employeeRepo = employeeRepo;
  this.deptRepo = deptRepo;
  this.onboardingRepo = onboardingRepo;
 }

 @GetMapping
 @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER')")
 public List<Map<String, Object>> list(
     @RequestParam(required = false) Long jobId,
     @RequestParam(required = false) String status
 ) {
  Map<Long, String> jobMap = new HashMap<>();
  for (JobOpening j : jobRepo.findAll()) {
   jobMap.put(j.getId(), j.getTitle());
  }

  List<Candidate> candidates;
  if (jobId != null && status != null && !status.isBlank()) {
   candidates = candidateRepo.findByJobOpeningIdAndApplicationStatus(jobId, status);
  } else if (jobId != null) {
   candidates = candidateRepo.findByJobOpeningId(jobId);
  } else if (status != null && !status.isBlank()) {
   candidates = candidateRepo.findByApplicationStatus(status);
  } else {
   candidates = candidateRepo.findAll();
  }

  List<Map<String, Object>> result = new ArrayList<>();
  for (Candidate c : candidates) {
   Map<String, Object> map = new LinkedHashMap<>();
   map.put("id", c.getId());
   map.put("name", c.getName());
   map.put("candidateName", c.getName());
   map.put("email", c.getEmail());
   map.put("phone", c.getPhone() != null ? c.getPhone() : "");
   map.put("jobOpeningId", c.getJobOpeningId());
   map.put("jobTitle", jobMap.getOrDefault(c.getJobOpeningId(), "General Position"));
   map.put("status", c.getApplicationStatus());
   map.put("applicationStatus", c.getApplicationStatus());
   map.put("appliedAt", c.getAppliedAt());
   result.add(map);
  }
  return result;
 }

 @GetMapping("/me/applications")
 public List<Map<String, Object>> myApplications(Authentication auth) {
  String username = auth != null ? auth.getName() : "";
  Map<Long, JobOpening> jobMap = new HashMap<>();
  for (JobOpening j : jobRepo.findAll()) {
   jobMap.put(j.getId(), j);
  }

  List<Candidate> userCandidates = candidateRepo.findAll().stream()
      .filter(c -> c.getEmail().equalsIgnoreCase(username)
          || c.getName().equalsIgnoreCase(username)
          || (username.contains("@") && c.getEmail().equalsIgnoreCase(username)))
      .toList();

  List<Map<String, Object>> result = new ArrayList<>();
  for (Candidate c : userCandidates) {
   JobOpening job = jobMap.get(c.getJobOpeningId());
   Map<String, Object> map = new LinkedHashMap<>();
   map.put("id", c.getId());
   map.put("jobTitle", job != null ? job.getTitle() : "Position");
   map.put("location", job != null ? job.getLocation() : "Hybrid");
   map.put("status", c.getApplicationStatus());
   map.put("appliedAt", c.getAppliedAt());
   result.add(map);
  }
  return result;
 }

 @PutMapping("/{id}/status")
 @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER')")
 public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
  Candidate c = candidateRepo.findById(id).orElseThrow(() -> new NoSuchElementException("Candidate not found"));
  String newStatus = body.get("status");
  if (newStatus != null) {
   c.setApplicationStatus(newStatus);
   candidateRepo.save(c);
  }
  return ResponseEntity.ok(Map.of("message", "Status updated successfully", "status", c.getApplicationStatus()));
 }

 @PostMapping("/{id}/convert")
 @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER')")
 public ResponseEntity<?> convertToEmployee(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> body) {
  Candidate c = candidateRepo.findById(id).orElseThrow(() -> new NoSuchElementException("Candidate not found"));
  JobOpening job = jobRepo.findById(c.getJobOpeningId()).orElse(null);

  Employee e = new Employee();
  e.setEmployeeCode("EMP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
  e.setName(c.getName());
  e.setEmail(c.getEmail());

  // Phone from user body if provided, else candidate's phone
  String phone = (body != null && body.containsKey("phone") && body.get("phone") != null)
      ? String.valueOf(body.get("phone"))
      : (c.getPhone() != null ? c.getPhone() : "");
  e.setPhone(phone);

  // Designation from user body or job title
  String designation = (body != null && body.containsKey("designation") && body.get("designation") != null)
      ? String.valueOf(body.get("designation"))
      : (job != null ? job.getTitle() : "Associate");
  e.setDesignation(designation);

  // Department from user body or job department or dynamic first department
  Long deptId = null;
  if (body != null && body.containsKey("departmentId") && body.get("departmentId") != null) {
   deptId = ((Number) body.get("departmentId")).longValue();
  } else if (job != null && job.getDepartmentId() != null) {
   deptId = job.getDepartmentId();
  } else {
   deptId = deptRepo.findAll().stream().findFirst().map(Department::getId).orElse(null);
  }
  e.setDepartmentId(deptId);

  e.setDateOfJoining(LocalDate.now());
  e.setStatus("ACTIVE");
  Employee saved = employeeRepo.save(e);

  c.setApplicationStatus("SELECTED");
  candidateRepo.save(c);

  // Generate onboarding checklist for new hire
  List<String> defaultTasks = List.of(
      "Complete employee personal profile",
      "Upload identity and educational documents",
      "Sign HR policy and employment contract",
      "Collect IT workstation & configure credentials"
  );
  for (String taskName : defaultTasks) {
   OnboardingTask task = new OnboardingTask();
   task.setEmployeeId(saved.getId());
   task.setTaskName(taskName);
   task.setDescription("Onboarding requirement for " + saved.getName());
   task.setStatus("PENDING");
   task.setDueDate(LocalDate.now().plusDays(7));
   onboardingRepo.save(task);
  }

  return ResponseEntity.ok(Map.of(
      "message", "Candidate successfully converted to Employee",
      "employeeId", saved.getId(),
      "employeeCode", saved.getEmployeeCode()
  ));
 }
}
