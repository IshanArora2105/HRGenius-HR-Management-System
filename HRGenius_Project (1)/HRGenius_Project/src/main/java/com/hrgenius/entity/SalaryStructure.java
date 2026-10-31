package com.hrgenius.entity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name="salaryStructure")
@Getter @Setter @NoArgsConstructor
public class SalaryStructure {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,unique=true) private Long employeeId;
 @Column(nullable=false,precision=12,scale=2) private BigDecimal basicPay = BigDecimal.ZERO;
 @Column(nullable=false,precision=12,scale=2) private BigDecimal hra = BigDecimal.ZERO;
 @Column(nullable=false,precision=12,scale=2) private BigDecimal allowances = BigDecimal.ZERO;
 @Column(nullable=false,precision=12,scale=2) private BigDecimal deductions = BigDecimal.ZERO;

 public BigDecimal getBasic() { return basicPay; }
 public void setBasic(BigDecimal b) { this.basicPay = b; }
 public BigDecimal getOtherAllowances() { return allowances; }
 public void setOtherAllowances(BigDecimal a) { this.allowances = a; }
}
