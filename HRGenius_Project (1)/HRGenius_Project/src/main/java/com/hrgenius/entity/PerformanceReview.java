package com.hrgenius.entity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="performanceReview")
@Getter @Setter @NoArgsConstructor
public class PerformanceReview {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false) private Long employeeId;
 private Long reviewerId;
 @Column(nullable=false) private Long cycleId;
 @Column(columnDefinition="TEXT") private String goals;
 private Integer rating;
 @Column(columnDefinition="TEXT") private String comments;
 @Column(columnDefinition="TEXT") private String selfAssessment;
 @Column(length=20) private String status = "SUBMITTED";
}
