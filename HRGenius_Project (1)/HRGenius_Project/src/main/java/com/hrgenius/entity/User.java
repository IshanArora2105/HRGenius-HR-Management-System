package com.hrgenius.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.*;
import java.math.BigDecimal;

@Entity
@Table(name="users")
@Getter @Setter @NoArgsConstructor
public class User {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,unique=true,length=100) private String username;
 @Column(nullable=false,length=255) private String password;
 @Column(nullable=false,length=30) private String role;
 @Column(nullable=false,length=20) private String status;
 private Long employeeId;
 private Long candidateId;
}
