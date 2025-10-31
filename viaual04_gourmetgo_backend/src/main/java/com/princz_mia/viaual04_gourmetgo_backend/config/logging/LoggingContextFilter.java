package com.princz_mia.viaual04_gourmetgo_backend.config.logging;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(2)
public class LoggingContextFilter implements Filter {

    private static final String USER_ID_MDC_KEY = "userId";
    private static final String SESSION_ID_MDC_KEY = "sessionId";
    private static final String REQUEST_URI_MDC_KEY = "requestUri";
    private static final String HTTP_METHOD_MDC_KEY = "httpMethod";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        
        try {
            setUserContext();
            setRequestContext(httpRequest);
            
            chain.doFilter(request, response);
        } finally {
            clearContext();
        }
    }

    private void setUserContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && 
            !"anonymousUser".equals(authentication.getName())) {
            MDC.put(USER_ID_MDC_KEY, authentication.getName());
        }
    }

    private void setRequestContext(HttpServletRequest request) {
        MDC.put(REQUEST_URI_MDC_KEY, request.getRequestURI());
        MDC.put(HTTP_METHOD_MDC_KEY, request.getMethod());
        
        String sessionId = request.getSession(false) != null ? 
            request.getSession().getId() : "no-session";
        MDC.put(SESSION_ID_MDC_KEY, sessionId);
    }

    private void clearContext() {
        MDC.remove(USER_ID_MDC_KEY);
        MDC.remove(SESSION_ID_MDC_KEY);
        MDC.remove(REQUEST_URI_MDC_KEY);
        MDC.remove(HTTP_METHOD_MDC_KEY);
    }
}