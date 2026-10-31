package com.hrgenius.controller;

import com.hrgenius.entity.Candidate;
import com.hrgenius.entity.Employee;
import com.hrgenius.entity.Interview;
import com.hrgenius.entity.JobOpening;
import com.hrgenius.repository.CandidateRepository;
import com.hrgenius.repository.EmployeeRepository;
import com.hrgenius.repository.InterviewRepository;
import com.hrgenius.repository.JobOpeningRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/v1/interviews")
public class InterviewController {
 private final InterviewRepository interviewRepo;
 private final CandidateRepository candidateRepo;
 private final EmployeeRepository employeeRepo;
 private final JobOpeningRepository jobRepo;

 public InterviewController(
     InterviewRepository interviewRepo,
     CandidateRepository candidateRepo,
     EmployeeRepository employeeRepo,
     JobOpeningRepository jobRepo
 ) {
  this.interviewRepo = interviewRepo;
  this.candidateRepo = candidateRepo;
  this.employeeRepo = employeeRepo;
  this.jobRepo = jobRepo;
 }

 @GetMapping
 @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER','MANAGER')")
 public List<Map<String, Object>> list() {
  Map<Long, Candidate> candidateMap = new HashMap<>();
  for (Candidate c : candidateRepo.findAll()) {
   candidateMap.put(c.getId(), c);
  }
  Map<Long, Employee> empMap = new HashMap<>();
  for (Employee e : employeeRepo.findAll()) {
   empMap.put(e.getId(), e);
  }
  Map<Long, JobOpening> jobMap = new HashMap<>();
  for (JobOpening j : jobRepo.findAll()) {
   jobMap.put(j.getId(), j);
  }

  List<Map<String, Object>> list = new ArrayList<>();
  for (Interview i : interviewRepo.findAll()) {
   Map<String, Object> map = new LinkedHashMap<>();
   map.put("id", i.getId());
   map.put("candidateId", i.getCandidateId());
   Candidate cand = candidateMap.get(i.getCandidateId());
   map.put("candidateName", cand != null ? cand.getName() : "Candidate #" + i.getCandidateId());

   JobOpening job = cand != null && cand.getJobOpeningId() != null ? jobMap.get(cand.getJobOpeningId()) : null;
   map.put("jobTitle", job != null ? job.getTitle() : "Recruitment Process");

   Employee interviewer = empMap.get(i.getInterviewerId());
   map.put("interviewerId", i.getInterviewerId());
   map.put("interviewerName", interviewer != null ? interviewer.getName() : "Interviewer #" + i.getInterviewerId());

   map.put("scheduledAt", i.getScheduledDate());
   map.put("status", i.getResult() != null ? i.getResult() : "SCHEDULED");
   map.put("feedback", i.getFeedback());
   map.put("mode", i.getMode());
   map.put("meetingLink", i.getMeetingLink());
   list.add(map);
  }
  return list;
 }

 @PostMapping
 @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER')")
 public ResponseEntity<?> schedule(@RequestBody Map<String, Object> body) {
  Interview i = new Interview();
  if (body.get("candidateId") != null) {
   i.setCandidateId(((Number) body.get("candidateId")).longValue());
  }
  if (body.get("interviewerId") != null) {
   i.setInterviewerId(((Number) body.get("interviewerId")).longValue());
  }
  if (body.get("scheduledAt") != null) {
   try {
    i.setScheduledDate(LocalDateTime.parse((String) body.get("scheduledAt")));
   } catch (Exception ex) {
    i.setScheduledDate(LocalDateTime.now().plusDays(1));
   }
  }
  if (body.get("mode") != null) {
   i.setMode((String) body.get("mode"));
  }
  if (body.get("meetingLink") != null) {
   i.setMeetingLink((String) body.get("meetingLink"));
  }
  i.setResult("SCHEDULED");
  return ResponseEntity.status(201).body(interviewRepo.save(i));
 }

 @PutMapping("/{id}/feedback")
 @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER','MANAGER')")
 public ResponseEntity<?> feedback(@PathVariable Long id, @RequestBody Map<String, String> body) {
  Interview i = interviewRepo.findById(id).orElseThrow(() -> new NoSuchElementException("Interview not found"));
  if (body.containsKey("feedback")) {
   i.setFeedback(body.get("feedback"));
  }
  if (body.containsKey("status") && body.get("status") != null) {
   i.setResult(body.get("status"));
   // Also update candidate status if selected or rejected
   candidateRepo.findById(i.getCandidateId()).ifPresent(c -> {
    c.setApplicationStatus(body.get("status"));
    candidateRepo.save(c);
   });
  }
  return ResponseEntity.ok(interviewRepo.save(i));
 }
}
