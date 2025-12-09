package com.princz_mia.viaual04_gourmetgo_backend.data.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "request_logs")
public class RequestLog {
    
    @Id
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    private UUID id;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @Column(nullable = false)
    private String endpoint;
    
    @Column(nullable = false)
    private String method;
    
    @Column(nullable = false)
    private Integer statusCode;
    
    @Column(nullable = false)
    private Long responseTime;
    
    private String userAgent;
    
    private String ipAddress;
}