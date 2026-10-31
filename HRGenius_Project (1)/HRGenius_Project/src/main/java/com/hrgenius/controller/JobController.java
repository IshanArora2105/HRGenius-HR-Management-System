package com.hrgenius.controller;

import com.hrgenius.entity.Candidate;
import com.hrgenius.entity.Department;
import com.hrgenius.entity.JobOpening;
import com.hrgenius.entity.User;
import com.hrgenius.repository.CandidateRepository;
import com.hrgenius.repository.DepartmentRepository;
import com.hrgenius.repository.JobOpeningRepository;
import com.hrgenius.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/v1/jobs")
public class JobController {
 private final JobOpeningRepository jobRepo;
 private final DepartmentRepository deptRepo;
 private final CandidateRepository candidateRepo;
 private final UserRepository userRepo;

 public JobController(JobOpeningRepository jobRepo, DepartmentRepository deptRepo, CandidateRepository candidateRepo, UserRepository userRepo) {
  this.jobRepo = jobRepo;
  this.deptRepo = deptRepo;
  this.candidateRepo = candidateRepo;
  this.userRepo = userRepo;
 }

 @GetMapping
 public List<Map<String, Object>> list() {
  Map<Long, String> deptNames = new HashMap<>();
  for (Department d : deptRepo.findAll()) {
   deptNames.put(d.getId(), d.getName());
  }

  List<Map<String, Object>> list = new ArrayList<>();
  for (JobOpening j : jobRepo.findAll()) {
   Map<String, Object> map = new LinkedHashMap<>();
   map.put("id", j.getId());
   map.put("title", j.getTitle());
   map.put("departmentId", j.getDepartmentId());
   map.put("departmentName", j.getDepartmentId() != null ? deptNames.get(j.getDepartmentId()) : "General");
   map.put("location", j.getLocation() != null ? j.getLocation() : "Hybrid");
   map.put("description", j.getDescription());
   map.put("vacancies", j.getVacancies());
   map.put("status", j.getStatus());
   map.put("postedDate", j.getPostedDate());
   list.add(map);
  }
  return list;
 }

 @PostMapping
 @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER')")
 public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
  JobOpening j = new JobOpening();
  j.setTitle((String) body.get("title"));
  j.setLocation((String) body.getOrDefault("location", "Hybrid"));
  if (body.get("vacancies") != null) {
   j.setVacancies(((Number) body.get("vacancies")).intValue());
  }
  j.setDescription((String) body.get("description"));
  if (body.get("departmentId") != null && !String.valueOf(body.get("departmentId")).isBlank()) {
   j.setDepartmentId(((Number) body.get("departmentId")).longValue());
  }
  j.setStatus("OPEN");
  j.setPostedDate(LocalDate.now());
  return ResponseEntity.status(201).body(jobRepo.save(j));
 }

 @PutMapping("/{id}")
 @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER')")
 public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
  JobOpening j = jobRepo.findById(id).orElseThrow(() -> new NoSuchElementException("Job not found"));
  if (body.containsKey("title")) j.setTitle((String) body.get("title"));
  if (body.containsKey("location")) j.setLocation((String) body.get("location"));
  if (body.containsKey("description")) j.setDescription((String) body.get("description"));
  if (body.containsKey("vacancies") && body.get("vacancies") != null) {
   j.setVacancies(((Number) body.get("vacancies")).intValue());
  }
  if (body.containsKey("departmentId") && body.get("departmentId") != null) {
   j.setDepartmentId(((Number) body.get("departmentId")).longValue());
  }
  if (body.containsKey("status") && body.get("status") != null) {
   j.setStatus((String) body.get("status"));
  }
  return ResponseEntity.ok(jobRepo.save(j));
 }

 @PostMapping("/{id}/apply")
 public ResponseEntity<?> apply(
     @PathVariable Long id,
     @RequestBody(required = false) Map<String, String> body,
     Authentication authentication
 ) {
  JobOpening j = jobRepo.findById(id).orElseThrow(() -> new NoSuchElementException("Job opening not found"));
  String username = authentication != null ? authentication.getName() : "candidate";

  Candidate c = new Candidate();
  c.setJobOpeningId(j.getId());
  c.setApplicationStatus("APPLIED");
  c.setAppliedAt(LocalDate.now());

  String name = (body != null && body.containsKey("name") && !body.get("name").isBlank())
      ? body.get("name")
      : username;
  String email = (body != null && body.containsKey("email") && !body.get("email").isBlank())
      ? body.get("email")
      : (username.contains("@") ? username : username + "@applicant.local");
  String phone = (body != null && body.containsKey("phone")) ? body.get("phone") : "";
  String resume = (body != null && body.containsKey("resume")) ? body.get("resume") : "";

  c.setName(name);
  c.setEmail(email);
  c.setPhone(phone);
  c.setResume(resume);

  candidateRepo.save(c);
  return ResponseEntity.ok(Map.of("message", "Application submitted successfully for " + j.getTitle()));
 }
}
