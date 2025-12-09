package com.princz_mia.viaual04_gourmetgo_backend.data.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "visit_statistics")
public class VisitStatistics {
    
    @Id
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    private UUID id;
    
    @Column(nullable = false)
    private LocalDate date;
    
    @Column(nullable = false)
    private Long totalVisits = 0L;
    
    @Column(nullable = false)
    private Long uniqueVisitors = 0L;
    
    @Column(nullable = false)
    private Long authenticatedUsers = 0L;
    
    @Column(nullable = false)
    private Long anonymousUsers = 0L;
}