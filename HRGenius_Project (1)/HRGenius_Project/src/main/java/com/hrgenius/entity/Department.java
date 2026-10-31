package com.hrgenius.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.*;
import java.math.BigDecimal;
@Entity
@Table(name="department")
@Getter @Setter @NoArgsConstructor
public class Department {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false, unique=true, length=100) private String name; @Column(length=500) private String description;
}
