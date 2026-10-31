package com.hrgenius.service;

import com.hrgenius.dto.EmployeeRequest;
import com.hrgenius.dto.EmployeeResponse;
import com.hrgenius.entity.Department;
import com.hrgenius.entity.Employee;
import com.hrgenius.repository.DepartmentRepository;
import com.hrgenius.repository.EmployeeRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class EmployeeService {
 private final EmployeeRepository repo;
 private final DepartmentRepository deptRepo;

 public EmployeeService(EmployeeRepository repo, DepartmentRepository deptRepo) {
  this.repo = repo;
  this.deptRepo = deptRepo;
 }

 public Employee create(EmployeeRequest r) {
  Employee e = new Employee();
  e.setEmployeeCode("EMP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
  e.setName(r.name());
  e.setEmail(r.email());
  e.setPhone(r.phone());
  e.setDesignation(r.designation());
  e.setDepartmentId(r.departmentId());
  e.setDateOfJoining(r.dateOfJoining());
  e.setManagerId(r.managerId());
  e.setStatus("ACTIVE");
  return repo.save(e);
 }

 public Page<EmployeeResponse> list(String q, Pageable p) {
  Map<Long, String> deptMap = getDepartmentNameMap();
  Page<Employee> page = (q == null || q.isBlank())
      ? repo.findAll(p)
      : repo.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(q, q, p);
  return page.map(e -> EmployeeResponse.from(e, deptMap.get(e.getDepartmentId())));
 }

 public Employee get(long id) {
  return repo.findById(id).orElseThrow(() -> new java.util.NoSuchElementException("Employee not found"));
 }

 public EmployeeResponse getResponse(long id) {
  Employee e = get(id);
  String deptName = e.getDepartmentId() != null
      ? deptRepo.findById(e.getDepartmentId()).map(Department::getName).orElse(null)
      : null;
  return EmployeeResponse.from(e, deptName);
 }

 public Employee update(long id, EmployeeRequest r) {
  Employee e = get(id);
  e.setName(r.name());
  e.setEmail(r.email());
  e.setPhone(r.phone());
  e.setDesignation(r.designation());
  e.setDepartmentId(r.departmentId());
  e.setDateOfJoining(r.dateOfJoining());
  e.setManagerId(r.managerId());
  return repo.save(e);
 }

 public void deactivate(long id) {
  Employee e = get(id);
  e.setStatus("INACTIVE");
  repo.save(e);
 }

 public EmployeeResponse getForUsername(String username) {
  Employee emp = repo.findByEmail(username)
      .or(() -> repo.findAll().stream().filter(e -> username.equalsIgnoreCase(e.getEmail())).findFirst())
      .orElseGet(() -> repo.findAll().stream().findFirst().orElseGet(() -> {
       Employee fallback = new Employee();
       fallback.setId(1L);
       fallback.setEmployeeCode("EMP-ADMIN");
       fallback.setName(username);
       fallback.setEmail(username + "@hrgenius.local");
       fallback.setDesignation("Administrator");
       fallback.setStatus("ACTIVE");
       return fallback;
      }));
  String deptName = emp.getDepartmentId() != null
      ? deptRepo.findById(emp.getDepartmentId()).map(Department::getName).orElse(null)
      : null;
  return EmployeeResponse.from(emp, deptName);
 }

 public List<EmployeeResponse> getTeam(long id) {
  Map<Long, String> deptMap = getDepartmentNameMap();
  Employee emp = get(id);
  List<Employee> reports = repo.findByManagerId(id);
  if (!reports.isEmpty()) {
   return reports.stream().map(e -> EmployeeResponse.from(e, deptMap.get(e.getDepartmentId()))).toList();
  }
  if (emp.getDepartmentId() != null) {
   return repo.findByDepartmentId(emp.getDepartmentId()).stream()
       .filter(e -> !e.getId().equals(id))
       .map(e -> EmployeeResponse.from(e, deptMap.get(e.getDepartmentId())))
       .toList();
  }
  return List.of();
 }

 private Map<Long, String> getDepartmentNameMap() {
  Map<Long, String> map = new HashMap<>();
  for (Department d : deptRepo.findAll()) {
   map.put(d.getId(), d.getName());
  }
  return map;
 }
}
