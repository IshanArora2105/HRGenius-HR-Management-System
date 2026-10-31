package com.hrgenius.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.*;
import java.math.BigDecimal;
@Entity
@Table(name="employee")
@Getter @Setter @NoArgsConstructor
public class Employee {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,unique=true,length=20) private String employeeCode; @Column(nullable=false,length=150) private String name; @Column(nullable=false,unique=true,length=150) private String email; @Column(length=20) private String phone; @Column(nullable=false) private LocalDate dateOfJoining; @Column(nullable=false,length=100) private String designation; @Column(nullable=false,length=20) private String status; @Column(nullable=false) private Long departmentId; private Long managerId;
}
