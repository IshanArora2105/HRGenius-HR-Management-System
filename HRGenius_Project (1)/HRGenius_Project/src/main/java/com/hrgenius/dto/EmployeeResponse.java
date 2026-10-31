package com.hrgenius.dto;

import com.hrgenius.entity.Employee;
import java.time.LocalDate;

public record EmployeeResponse(
    Long id,
    String employeeCode,
    String name,
    String email,
    String phone,
    LocalDate dateOfJoining,
    String designation,
    String status,
    Long departmentId,
    String departmentName,
    Long managerId
) {
 public static EmployeeResponse from(Employee e) {
  return new EmployeeResponse(e.getId(), e.getEmployeeCode(), e.getName(), e.getEmail(), e.getPhone(), e.getDateOfJoining(), e.getDesignation(), e.getStatus(), e.getDepartmentId(), null, e.getManagerId());
 }
 public static EmployeeResponse from(Employee e, String departmentName) {
  return new EmployeeResponse(e.getId(), e.getEmployeeCode(), e.getName(), e.getEmail(), e.getPhone(), e.getDateOfJoining(), e.getDesignation(), e.getStatus(), e.getDepartmentId(), departmentName, e.getManagerId());
 }
}
