package com.hrgenius.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.*;

@Entity
@Table(name="interview")
@Getter @Setter @NoArgsConstructor
public class Interview {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false) private Long candidateId;
 @Column(nullable=false) private Long interviewerId;
 @Column(nullable=false) private LocalDateTime scheduledDate = LocalDateTime.now();
 @Column(columnDefinition="TEXT") private String feedback;
 @Column(length=30) private String result = "SCHEDULED";
 @Column(length=50) private String mode = "Virtual";
 @Column(length=255) private String meetingLink;

 public LocalDateTime getScheduledAt() { return scheduledDate; }
 public void setScheduledAt(LocalDateTime scheduledAt) { this.scheduledDate = scheduledAt; }
 public String getStatus() { return result; }
 public void setStatus(String status) { this.result = status; }
}
