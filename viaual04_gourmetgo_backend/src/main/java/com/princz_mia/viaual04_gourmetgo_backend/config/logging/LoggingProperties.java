package com.princz_mia.viaual04_gourmetgo_backend.config.logging;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.logging")
public class LoggingProperties {
    
    private String correlationIdHeader = "X-Correlation-ID";
    private boolean enableRequestLogging = true;
    private boolean enablePerformanceLogging = true;
    private boolean maskSensitiveData = true;
    private long slowOperationThresholdMs = 1000;
    private boolean enableSecurityEventLogging = true;
    private boolean enableBusinessEventLogging = true;
}