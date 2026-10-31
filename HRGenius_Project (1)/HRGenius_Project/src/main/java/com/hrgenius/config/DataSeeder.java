package com.hrgenius.config;

import com.hrgenius.entity.*;
import com.hrgenius.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Configuration
public class DataSeeder {
 @Bean
 CommandLineRunner seed(
     UserRepository users,
     DepartmentRepository departments,
     EmployeeRepository employees,
     JobOpeningRepository jobs,
     CandidateRepository candidates,
     InterviewRepository interviews,
     SalaryStructureRepository salaryStructures,
     PerformanceCycleRepository cycles,
     GoalRepository goals,
     PerformanceReviewRepository reviews,
     OnboardingTaskRepository onboardingTasks,
     PasswordEncoder encoder
 ) {
  return args -> {
   // Seed Departments
   Department hrDept;
   Department engDept;
   if (departments.count() == 0) {
    hrDept = new Department();
    hrDept.setName("Human Resources");
    hrDept.setDescription("Core HR operations, recruitment, and employee relations");
    hrDept = departments.save(hrDept);

    engDept = new Department();
    engDept.setName("Engineering");
    engDept.setDescription("Software engineering, architecture, and technology infrastructure");
    engDept = departments.save(engDept);

    Department prodDept = new Department();
    prodDept.setName("Product & Design");
    prodDept.setDescription("Product development, UX research, and design systems");
    departments.save(prodDept);
   } else {
    hrDept = departments.findAll().get(0);
    engDept = departments.findAll().size() > 1 ? departments.findAll().get(1) : hrDept;
   }

   // Seed Employees
   Employee adminEmp = null;
   if (employees.count() == 0) {
    adminEmp = new Employee();
    adminEmp.setEmployeeCode("EMP-1001");
    adminEmp.setName("System Administrator");
    adminEmp.setEmail("admin");
    adminEmp.setPhone("+91 98765 43210");
    adminEmp.setDesignation("HR Operations Lead");
    adminEmp.setStatus("ACTIVE");
    adminEmp.setDepartmentId(hrDept.getId());
    adminEmp.setDateOfJoining(LocalDate.of(2023, 1, 15));
    adminEmp = employees.save(adminEmp);

    Employee emp2 = new Employee();
    emp2.setEmployeeCode("EMP-1002");
    emp2.setName("Priya Sharma");
    emp2.setEmail("priya.sharma@hrgenius.local");
    emp2.setPhone("+91 98111 22334");
    emp2.setDesignation("Senior Software Engineer");
    emp2.setStatus("ACTIVE");
    emp2.setDepartmentId(engDept.getId());
    emp2.setDateOfJoining(LocalDate.of(2023, 6, 1));
    emp2.setManagerId(adminEmp.getId());
    emp2 = employees.save(emp2);

    Employee emp3 = new Employee();
    emp3.setEmployeeCode("EMP-1003");
    emp3.setName("Arun Patel");
    emp3.setEmail("arun.patel@hrgenius.local");
    emp3.setPhone("+91 98222 33445");
    emp3.setDesignation("Product Designer");
    emp3.setStatus("ACTIVE");
    emp3.setDepartmentId(hrDept.getId());
    emp3.setDateOfJoining(LocalDate.of(2024, 2, 10));
    emp3 = employees.save(emp3);
   } else {
    adminEmp = employees.findAll().get(0);
   }

   // Seed Admin User
   if (users.findByUsername("admin").isEmpty()) {
    User u = new User();
    u.setUsername("admin");
    u.setPassword(encoder.encode(System.getenv().getOrDefault("HRGENIUS_ADMIN_PASSWORD", "Admin@12345")));
    u.setRole("ADMIN");
    u.setStatus("ACTIVE");
    if (adminEmp != null) {
     u.setEmployeeId(adminEmp.getId());
    }
    users.save(u);
   }

   // Seed Salary Structures
   if (salaryStructures.count() == 0 && adminEmp != null) {
    SalaryStructure ss = new SalaryStructure();
    ss.setEmployeeId(adminEmp.getId());
    ss.setBasic(new BigDecimal("75000.00"));
    ss.setHra(new BigDecimal("30000.00"));
    ss.setOtherAllowances(new BigDecimal("15000.00"));
    ss.setDeductions(new BigDecimal("9000.00"));
    salaryStructures.save(ss);

    for (Employee emp : employees.findAll()) {
     if (!emp.getId().equals(adminEmp.getId()) && salaryStructures.findByEmployeeId(emp.getId()).isEmpty()) {
      SalaryStructure s = new SalaryStructure();
      s.setEmployeeId(emp.getId());
      s.setBasic(new BigDecimal("60000.00"));
      s.setHra(new BigDecimal("24000.00"));
      s.setOtherAllowances(new BigDecimal("10000.00"));
      s.setDeductions(new BigDecimal("7200.00"));
      salaryStructures.save(s);
     }
    }
   }

   // Seed Job Openings
   JobOpening job1 = null;
   if (jobs.count() == 0) {
    job1 = new JobOpening();
    job1.setTitle("Senior Full Stack Java Engineer");
    job1.setDepartmentId(engDept.getId());
    job1.setDescription("Leading end-to-end development of microservices using Spring Boot, React, and cloud native architectures.");
    job1.setLocation("Bengaluru / Hybrid");
    job1.setVacancies(3);
    job1.setStatus("OPEN");
    job1.setPostedDate(LocalDate.now().minusDays(12));
    job1 = jobs.save(job1);

    JobOpening job2 = new JobOpening();
    job2.setTitle("Talent Acquisition Specialist");
    job2.setDepartmentId(hrDept.getId());
    job2.setDescription("Manage full life-cycle recruiting for engineering, product, and leadership tracks.");
    job2.setLocation("Noida / On-site");
    job2.setVacancies(2);
    job2.setStatus("OPEN");
    job2.setPostedDate(LocalDate.now().minusDays(5));
    jobs.save(job2);
   } else {
    job1 = jobs.findAll().get(0);
   }

   // Seed Candidates & Interviews
   if (candidates.count() == 0 && job1 != null) {
    Candidate c1 = new Candidate();
    c1.setName("Rohan Verma");
    c1.setEmail("rohan.verma@example.com");
    c1.setPhone("+91 99887 76655");
    c1.setJobOpeningId(job1.getId());
    c1.setApplicationStatus("SHORTLISTED");
    c1.setAppliedAt(LocalDate.now().minusDays(6));
    c1 = candidates.save(c1);

    Candidate c2 = new Candidate();
    c2.setName("Sneha Iyer");
    c2.setEmail("sneha.iyer@example.com");
    c2.setPhone("+91 99112 23344");
    c2.setJobOpeningId(job1.getId());
    c2.setApplicationStatus("APPLIED");
    c2.setAppliedAt(LocalDate.now().minusDays(2));
    candidates.save(c2);

    if (interviews.count() == 0 && adminEmp != null) {
     Interview inv = new Interview();
     inv.setCandidateId(c1.getId());
     inv.setInterviewerId(adminEmp.getId());
     inv.setScheduledDate(LocalDateTime.now().plusDays(2).withHour(14).withMinute(0));
     inv.setMode("Google Meet");
     inv.setMeetingLink("https://meet.google.com/hrg-demo-tech");
     inv.setResult("SCHEDULED");
     interviews.save(inv);
    }
   }

   // Seed Performance Cycles & Goals
   if (cycles.count() == 0) {
    PerformanceCycle pc = new PerformanceCycle();
    pc.setName("Annual Performance Review 2026");
    pc.setStartDate(LocalDate.of(2026, 1, 1));
    pc.setEndDate(LocalDate.of(2026, 12, 31));
    pc.setStatus("ACTIVE");
    pc = cycles.save(pc);

    if (adminEmp != null && goals.count() == 0) {
     Goal g = new Goal();
     g.setEmployeeId(adminEmp.getId());
     g.setCycleId(pc.getId());
     g.setTitle("Automate HR Payroll & Onboarding Pipelines");
     g.setDescription("Streamline compensation runs and automate joining document workflows");
     g.setStatus("IN_PROGRESS");
     g.setDueDate(LocalDate.of(2026, 6, 30));
     goals.save(g);
    }
   }

   // Seed Initial Onboarding Tasks
   if (onboardingTasks.count() == 0 && adminEmp != null) {
    OnboardingTask task = new OnboardingTask();
    task.setEmployeeId(adminEmp.getId());
    task.setTaskName("Review and sign IT policy guidelines");
    task.setDescription("Standard information security and workplace compliance");
    task.setStatus("COMPLETED");
    task.setDueDate(LocalDate.now().minusDays(10));
    onboardingTasks.save(task);
   }
  };
 }
}
