package com.hrgenius.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.*;

@Entity
@Table(name="onboardingTask")
@Getter @Setter @NoArgsConstructor
public class OnboardingTask {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false) private Long employeeId;
 @Column(nullable=false,length=150) private String taskName;
 @Column(length=500) private String description;
 @Column(nullable=false,length=30) private String status = "PENDING";
 private LocalDate dueDate;

 public String getTitle() { return taskName; }
 public void setTitle(String title) { this.taskName = title; }
}
