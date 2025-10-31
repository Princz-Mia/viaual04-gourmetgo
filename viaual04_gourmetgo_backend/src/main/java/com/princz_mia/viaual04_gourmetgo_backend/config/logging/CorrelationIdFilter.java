package com.princz_mia.viaual04_gourmetgo_backend.config.logging;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(1)
public class CorrelationIdFilter implements Filter {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String CORRELATION_ID_MDC_KEY = "correlationId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        try {
            String correlationId = extractCorrelationId(httpRequest);
            MDC.put(CORRELATION_ID_MDC_KEY, correlationId);
            httpResponse.setHeader(CORRELATION_ID_HEADER, correlationId);
            
            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }

    private String extractCorrelationId(HttpServletRequest request) {
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        return correlationId != null ? correlationId : UUID.randomUUID().toString();
    }
}