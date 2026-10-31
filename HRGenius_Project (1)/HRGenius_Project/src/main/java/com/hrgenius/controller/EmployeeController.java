package com.hrgenius.controller;

import com.hrgenius.dto.*;
import com.hrgenius.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/employees")
public class EmployeeController {
 private final EmployeeService service;

 public EmployeeController(EmployeeService service) {
  this.service = service;
 }

 @PostMapping
 @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER')")
 public ResponseEntity<EmployeeResponse> create(@Valid @RequestBody EmployeeRequest r) {
  return ResponseEntity.status(201).body(service.getResponse(service.create(r).getId()));
 }

 @GetMapping
 @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER','MANAGER')")
 public Page<EmployeeResponse> list(@RequestParam(required=false) String search, Pageable pageable) {
  return service.list(search, pageable);
 }

 @GetMapping("/me")
 public ResponseEntity<EmployeeResponse> getCurrentEmployee(Authentication authentication) {
  String username = authentication != null ? authentication.getName() : "admin";
  return ResponseEntity.ok(service.getForUsername(username));
 }

 @GetMapping("/{id}")
 @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER','MANAGER','EMPLOYEE')")
 public EmployeeResponse get(@PathVariable long id) {
  return service.getResponse(id);
 }

 @GetMapping("/{id}/team")
 @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER','MANAGER','EMPLOYEE')")
 public List<EmployeeResponse> getTeam(@PathVariable long id) {
  return service.getTeam(id);
 }

 @PutMapping("/{id}")
 @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER')")
 public EmployeeResponse update(@PathVariable long id, @Valid @RequestBody EmployeeRequest r) {
  return service.getResponse(service.update(id, r).getId());
 }

 @DeleteMapping("/{id}")
 @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER')")
 public ResponseEntity<?> deactivate(@PathVariable long id) {
  service.deactivate(id);
  return ResponseEntity.noContent().build();
 }
}
