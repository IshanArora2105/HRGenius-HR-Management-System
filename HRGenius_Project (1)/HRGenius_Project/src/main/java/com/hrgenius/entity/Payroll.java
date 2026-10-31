package com.hrgenius.entity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name="payroll", uniqueConstraints=@UniqueConstraint(columnNames={"employee_id","payroll_month","payroll_year"}))
@Getter @Setter @NoArgsConstructor
public class Payroll {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(name="employee_id", nullable=false) private Long employeeId;
 @Column(name="payroll_month", nullable=false) private Integer month;
 @Column(name="payroll_year", nullable=false) private Integer year;
 @Column(nullable=false,precision=12,scale=2) private BigDecimal grossPay;
 @Column(nullable=false,precision=12,scale=2) private BigDecimal pf;
 @Column(nullable=false,precision=12,scale=2) private BigDecimal tds;
 @Column(nullable=false,precision=12,scale=2) private BigDecimal otherDeductions;
 @Column(nullable=false,precision=12,scale=2) private BigDecimal totalDeductions;
 @Column(nullable=false,precision=12,scale=2) private BigDecimal netPay;
 @Column(nullable=false,length=20) private String status = "PROCESSED";

 public BigDecimal getGrossSalary() { return grossPay; }
}
