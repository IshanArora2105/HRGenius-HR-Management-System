package com.hrgenius.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name="goal")
@Getter @Setter @NoArgsConstructor
public class Goal {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false) private Long employeeId;
 private Long cycleId;
 @Column(nullable=false,length=150) private String title;
 @Column(columnDefinition="TEXT") private String description;
 @Column(nullable=false,length=30) private String status = "IN_PROGRESS";
 private Long setBy;
 private LocalDate dueDate;
}
