package com.hrgenius.dto;
import jakarta.validation.constraints.*; import java.time.LocalDate;
public record EmployeeRequest(@NotBlank @Size(max=150) String name,@NotBlank @Email String email,
 @Size(max=20) String phone,@NotBlank @Size(max=100) String designation,@NotNull Long departmentId,
 @NotNull LocalDate dateOfJoining, Long managerId) {}
