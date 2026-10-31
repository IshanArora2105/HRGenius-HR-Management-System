package com.hrgenius.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.*;

@Entity
@Table(name="performanceCycle")
@Getter @Setter @NoArgsConstructor
public class PerformanceCycle {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,length=100) private String name;
 @Column(nullable=false) private LocalDate startDate;
 @Column(nullable=false) private LocalDate endDate;
 @Column(length=20) private String status = "ACTIVE";
}
