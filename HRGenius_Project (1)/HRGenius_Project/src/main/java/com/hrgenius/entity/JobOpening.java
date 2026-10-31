package com.hrgenius.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.*;

@Entity
@Table(name="jobOpening")
@Getter @Setter @NoArgsConstructor
public class JobOpening {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,length=150) private String title;
 private Long departmentId;
 @Column(columnDefinition="TEXT") private String description;
 @Column(nullable=false) private Integer vacancies = 1;
 @Column(nullable=false,length=20) private String status = "OPEN";
 @Column(length=100) private String location = "Hybrid";
 @Column(nullable=false) private LocalDate postedDate = LocalDate.now();
}
