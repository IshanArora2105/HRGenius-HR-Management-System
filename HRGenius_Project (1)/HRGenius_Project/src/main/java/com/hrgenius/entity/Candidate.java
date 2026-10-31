package com.hrgenius.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.*;

@Entity
@Table(name="candidate")
@Getter @Setter @NoArgsConstructor
public class Candidate {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,length=150) private String name;
 @Column(nullable=false,length=150) private String email;
 @Column(length=20) private String phone;
 @Column(length=500) private String resume;
 @Column(nullable=false) private Long jobOpeningId;
 @Column(nullable=false,length=30) private String applicationStatus = "APPLIED";
 private LocalDate appliedAt = LocalDate.now();

 public String getStatus() { return applicationStatus; }
 public void setStatus(String status) { this.applicationStatus = status; }
 public String getCandidateName() { return name; }
}
