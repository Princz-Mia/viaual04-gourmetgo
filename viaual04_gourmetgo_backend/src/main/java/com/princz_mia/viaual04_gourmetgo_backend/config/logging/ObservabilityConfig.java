package com.princz_mia.viaual04_gourmetgo_backend.config.logging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

@Configuration
@Slf4j
public class ObservabilityConfig {

    @Bean
    public CommonsRequestLoggingFilter requestLoggingFilter() {
        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
        filter.setIncludeQueryString(true);
        filter.setIncludePayload(false); // Security: Don't log request body
        filter.setIncludeHeaders(false); // Security: Don't log headers
        filter.setMaxPayloadLength(0);
        filter.setAfterMessagePrefix("REQUEST: ");
        return filter;
    }
}