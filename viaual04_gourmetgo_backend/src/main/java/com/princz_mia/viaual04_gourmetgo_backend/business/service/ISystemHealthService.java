package com.princz_mia.viaual04_gourmetgo_backend.business.service;

import java.util.List;
import java.util.Map;

public interface ISystemHealthService {
    
    Map<String, Object> getSystemMetrics();
    
    List<Map<String, Object>> getRecentLogs(int limit);
    
    List<String> getLogFiles();
    
    String getLogFileContent(String filename);
    
    Map<String, Object> getUserAnalytics();
    
    Map<String, Object> getBusinessInsights();
}