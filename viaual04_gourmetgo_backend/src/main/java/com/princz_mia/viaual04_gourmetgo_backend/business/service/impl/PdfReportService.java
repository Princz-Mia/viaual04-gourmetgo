package com.princz_mia.viaual04_gourmetgo_backend.business.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.princz_mia.viaual04_gourmetgo_backend.business.service.IPdfReportService;
import com.princz_mia.viaual04_gourmetgo_backend.business.service.IStatisticsService;
import com.princz_mia.viaual04_gourmetgo_backend.business.service.ISystemHealthService;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.AdminDashboardDto;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.RestaurantDashboardDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PdfReportService implements IPdfReportService {
    
    private final IStatisticsService statisticsService;
    private final ISystemHealthService systemHealthService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private boolean isSectionSelected(String sections, String sectionKey) {
        if (sections == null || sections.isEmpty()) {
            return true;
        }
        try {
            JsonNode sectionsJson = objectMapper.readTree(sections);
            return sectionsJson.has(sectionKey) && sectionsJson.get(sectionKey).asBoolean();
        } catch (Exception e) {
            return true;
        }
    }
    
    @Override
    public byte[] generateAdminReport(LocalDate startDate, LocalDate endDate, String sections) {
        try {
            AdminDashboardDto data = statisticsService.getAdminDashboard(startDate, endDate);
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            
            // Title
            document.add(new Paragraph("GourmetGo Admin Dashboard Report")
                    .setFontSize(20)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold());
            
            document.add(new Paragraph(String.format("Period: %s to %s", 
                    startDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                    endDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20));
            
            // System Health - complete system metrics
            if (isSectionSelected(sections, "systemHealth")) {
                Map<String, Object> systemMetrics = systemHealthService.getSystemMetrics();
                document.add(new Paragraph("System Health").setFontSize(16).setBold());
                Table systemTable = new Table(UnitValue.createPercentArray(2)).useAllAvailableWidth();
                
                systemTable.addCell(new Cell().add(new Paragraph("Uptime")));
                systemTable.addCell(new Cell().add(new Paragraph(String.valueOf(systemMetrics.get("uptime")))));
                
                systemTable.addCell(new Cell().add(new Paragraph("Memory Usage")));
                systemTable.addCell(new Cell().add(new Paragraph(String.valueOf(systemMetrics.get("memoryUsage")))));
                
                systemTable.addCell(new Cell().add(new Paragraph("CPU Usage")));
                systemTable.addCell(new Cell().add(new Paragraph(String.valueOf(systemMetrics.get("cpuUsage")))));
                
                systemTable.addCell(new Cell().add(new Paragraph("Error Rate")));
                systemTable.addCell(new Cell().add(new Paragraph(String.valueOf(systemMetrics.get("errorRate")))));
                
                document.add(systemTable);
            }
            
            // Business Insights - complete business data
            if (isSectionSelected(sections, "businessInsights")) {
                Map<String, Object> businessInsights = systemHealthService.getBusinessInsights();
                document.add(new Paragraph("Business Insights").setFontSize(16).setBold().setMarginTop(20));
                Table businessTable = new Table(UnitValue.createPercentArray(2)).useAllAvailableWidth();
                
                businessTable.addCell(new Cell().add(new Paragraph("Total Revenue")));
                businessTable.addCell(new Cell().add(new Paragraph(String.format("$%.2f", businessInsights.get("totalRevenue")))));
                
                businessTable.addCell(new Cell().add(new Paragraph("Total Orders")));
                businessTable.addCell(new Cell().add(new Paragraph(String.valueOf(businessInsights.get("totalOrders")))));
                
                businessTable.addCell(new Cell().add(new Paragraph("Average Order Value")));
                businessTable.addCell(new Cell().add(new Paragraph(String.format("$%.2f", businessInsights.get("averageOrderValue")))));
                
                businessTable.addCell(new Cell().add(new Paragraph("Orders Today")));
                businessTable.addCell(new Cell().add(new Paragraph(String.valueOf(businessInsights.get("ordersToday")))));
                
                businessTable.addCell(new Cell().add(new Paragraph("Monthly Growth")));
                businessTable.addCell(new Cell().add(new Paragraph(String.format("%.1f%%", businessInsights.get("monthlyGrowth")))));
                
                businessTable.addCell(new Cell().add(new Paragraph("Active Restaurants")));
                businessTable.addCell(new Cell().add(new Paragraph(String.valueOf(businessInsights.get("activeRestaurants")))));
                
                document.add(businessTable);
                
                // Order Status Distribution
                if (data.getOrderStats() != null && data.getOrderStats().getOrdersByStatus() != null) {
                    document.add(new Paragraph("Order Status Distribution").setFontSize(14).setBold().setMarginTop(15));
                    Table statusTable = new Table(UnitValue.createPercentArray(2)).useAllAvailableWidth();
                    statusTable.addHeaderCell(new Cell().add(new Paragraph("Status").setBold()));
                    statusTable.addHeaderCell(new Cell().add(new Paragraph("Count").setBold()));
                    
                    for (Map.Entry<String, Long> entry : data.getOrderStats().getOrdersByStatus().entrySet()) {
                        statusTable.addCell(new Cell().add(new Paragraph(entry.getKey().replace("_", " "))));
                        statusTable.addCell(new Cell().add(new Paragraph(String.valueOf(entry.getValue()))));
                    }
                    document.add(statusTable);
                }
            }
            
            // User Analytics - complete user data
            if (isSectionSelected(sections, "userAnalytics")) {
                Map<String, Object> userAnalytics = systemHealthService.getUserAnalytics();
                document.add(new Paragraph("User Analytics").setFontSize(16).setBold().setMarginTop(20));
                Table userTable = new Table(UnitValue.createPercentArray(2)).useAllAvailableWidth();
                
                userTable.addCell(new Cell().add(new Paragraph("Total Users")));
                userTable.addCell(new Cell().add(new Paragraph(String.valueOf(userAnalytics.get("totalUsers")))));
                
                userTable.addCell(new Cell().add(new Paragraph("Customers")));
                userTable.addCell(new Cell().add(new Paragraph(String.valueOf(userAnalytics.get("customerCount")))));
                
                userTable.addCell(new Cell().add(new Paragraph("Restaurants")));
                userTable.addCell(new Cell().add(new Paragraph(String.valueOf(userAnalytics.get("restaurantCount")))));
                
                userTable.addCell(new Cell().add(new Paragraph("New Registrations Today")));
                userTable.addCell(new Cell().add(new Paragraph(String.valueOf(userAnalytics.get("newRegistrationsToday")))));
                
                userTable.addCell(new Cell().add(new Paragraph("Active Users This Week")));
                userTable.addCell(new Cell().add(new Paragraph(String.valueOf(userAnalytics.get("activeUsersThisWeek")))));
                
                userTable.addCell(new Cell().add(new Paragraph("Login Attempts")));
                userTable.addCell(new Cell().add(new Paragraph(String.valueOf(userAnalytics.get("loginAttempts")))));
                
                userTable.addCell(new Cell().add(new Paragraph("Failed Logins")));
                userTable.addCell(new Cell().add(new Paragraph(String.valueOf(userAnalytics.get("failedLogins")))));
                
                document.add(userTable);
            }
            
            // Top Products - only if topProducts is selected
            if (isSectionSelected(sections, "topProducts")) {
                document.add(new Paragraph("Top Products").setFontSize(16).setBold().setMarginTop(20));
                if (data.getTopProducts() != null && !data.getTopProducts().isEmpty()) {
                    Table productsTable = new Table(UnitValue.createPercentArray(2)).useAllAvailableWidth();
                    productsTable.addHeaderCell(new Cell().add(new Paragraph("Product").setBold()));
                    productsTable.addHeaderCell(new Cell().add(new Paragraph("Orders").setBold()));
                    
                    data.getTopProducts().stream().limit(10).forEach(product -> {
                        productsTable.addCell(new Cell().add(new Paragraph(product.getProductName())));
                        productsTable.addCell(new Cell().add(new Paragraph(String.valueOf(product.getOrderCount()))));
                    });
                    document.add(productsTable);
                } else {
                    document.add(new Paragraph("No product data available for the selected period."));
                }
            }
            
            // Performance Metrics - only if performanceMetrics is selected
            if (isSectionSelected(sections, "performanceMetrics")) {
                document.add(new Paragraph("Performance Metrics").setFontSize(16).setBold().setMarginTop(20));
                Table perfTable = new Table(UnitValue.createPercentArray(2)).useAllAvailableWidth();
                
                if (data.getPerformanceStats() != null) {
                    perfTable.addCell(new Cell().add(new Paragraph("Average Response Time")));
                    perfTable.addCell(new Cell().add(new Paragraph(String.format("%.0f ms", data.getPerformanceStats().getAverageResponseTime()))));
                    
                    perfTable.addCell(new Cell().add(new Paragraph("Error Rate")));
                    perfTable.addCell(new Cell().add(new Paragraph(String.format("%.1f%%", data.getPerformanceStats().getErrorRate()))));
                    

                }
                
                if (data.getTrafficStats() != null) {
                    perfTable.addCell(new Cell().add(new Paragraph("Weekly Visits")));
                    perfTable.addCell(new Cell().add(new Paragraph(String.valueOf(data.getTrafficStats().getWeeklyVisits()))));
                    
                    perfTable.addCell(new Cell().add(new Paragraph("Daily Visits")));
                    perfTable.addCell(new Cell().add(new Paragraph(String.valueOf(data.getTrafficStats().getDailyVisits()))));
                }
                
                document.add(perfTable);
            }
            
            // Charts - only if charts is selected
            if (isSectionSelected(sections, "charts")) {
                document.add(new Paragraph("Charts & Data Visualization").setFontSize(16).setBold().setMarginTop(20));
                document.add(new Paragraph("Chart data summary for the selected period:"));
                
                Table chartTable = new Table(UnitValue.createPercentArray(2)).useAllAvailableWidth();
                
                if (data.getOrderStats() != null) {
                    chartTable.addCell(new Cell().add(new Paragraph("Total Orders")));
                    chartTable.addCell(new Cell().add(new Paragraph(String.valueOf(data.getOrderStats().getTotalOrders()))));
                    

                }
                

                
                document.add(chartTable);
                document.add(new Paragraph("Note: Visual charts are available in the web dashboard.").setFontSize(10).setItalic());
            }
            
            document.close();
            return baos.toByteArray();
            
        } catch (Exception e) {
            log.error("Error generating admin PDF report", e);
            throw new RuntimeException("Failed to generate PDF report", e);
        }
    }
    
    @Override
    public byte[] generateRestaurantReport(UUID restaurantId, LocalDate startDate, LocalDate endDate, String sections) {
        try {
            RestaurantDashboardDto data = statisticsService.getRestaurantDashboard(restaurantId, startDate, endDate);
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            
            // Title
            document.add(new Paragraph("GourmetGo Restaurant Dashboard Report")
                    .setFontSize(20)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold());
            
            document.add(new Paragraph(String.format("Period: %s to %s", 
                    startDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                    endDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20));
            
            // Key Metrics - only if systemHealth is selected
            if (isSectionSelected(sections, "systemHealth")) {
                document.add(new Paragraph("Key Metrics").setFontSize(16).setBold());
                Table metricsTable = new Table(UnitValue.createPercentArray(2)).useAllAvailableWidth();
                
                if (data.getOrderStats() != null) {
                    metricsTable.addCell(new Cell().add(new Paragraph("Total Orders")));
                    metricsTable.addCell(new Cell().add(new Paragraph(String.valueOf(data.getOrderStats().getTotalOrders()))));
                    
                    metricsTable.addCell(new Cell().add(new Paragraph("Cancellation Rate")));
                    metricsTable.addCell(new Cell().add(new Paragraph(String.format("%.1f%%", data.getOrderStats().getCancellationRate()))));
                    
                    metricsTable.addCell(new Cell().add(new Paragraph("Avg Prep Time")));
                    metricsTable.addCell(new Cell().add(new Paragraph(String.format("%.0f min", data.getOrderStats().getAveragePreparationTime()))));
                }
                
                if (data.getRevenueStats() != null) {
                    metricsTable.addCell(new Cell().add(new Paragraph("Monthly Revenue")));
                    metricsTable.addCell(new Cell().add(new Paragraph(String.format("$%.2f", data.getRevenueStats().getMonthlyRevenue()))));
                    
                    metricsTable.addCell(new Cell().add(new Paragraph("Daily Revenue")));
                    metricsTable.addCell(new Cell().add(new Paragraph(String.format("$%.2f", data.getRevenueStats().getDailyRevenue()))));
                }
                
                document.add(metricsTable);
            }
            
            // Revenue Breakdown - only if businessInsights is selected
            if (isSectionSelected(sections, "businessInsights") && data.getRevenueStats() != null) {
                document.add(new Paragraph("Revenue Breakdown").setFontSize(16).setBold().setMarginTop(20));
                Table revenueTable = new Table(UnitValue.createPercentArray(2)).useAllAvailableWidth();
                
                revenueTable.addCell(new Cell().add(new Paragraph("Daily Revenue")));
                revenueTable.addCell(new Cell().add(new Paragraph(String.format("$%.2f", data.getRevenueStats().getDailyRevenue()))));
                
                revenueTable.addCell(new Cell().add(new Paragraph("Weekly Revenue")));
                revenueTable.addCell(new Cell().add(new Paragraph(String.format("$%.2f", data.getRevenueStats().getWeeklyRevenue()))));
                
                revenueTable.addCell(new Cell().add(new Paragraph("Monthly Revenue")));
                revenueTable.addCell(new Cell().add(new Paragraph(String.format("$%.2f", data.getRevenueStats().getMonthlyRevenue()))));
                
                revenueTable.addCell(new Cell().add(new Paragraph("Yearly Revenue")));
                revenueTable.addCell(new Cell().add(new Paragraph(String.format("$%.2f", data.getRevenueStats().getYearlyRevenue()))));
                
                document.add(revenueTable);
            }
            
            // Top Products - only if businessInsights is selected
            if (isSectionSelected(sections, "businessInsights") && data.getTopProducts() != null && !data.getTopProducts().isEmpty()) {
                document.add(new Paragraph("Top Products").setFontSize(16).setBold().setMarginTop(20));
                Table productsTable = new Table(UnitValue.createPercentArray(2)).useAllAvailableWidth();
                productsTable.addHeaderCell(new Cell().add(new Paragraph("Product").setBold()));
                productsTable.addHeaderCell(new Cell().add(new Paragraph("Orders").setBold()));
                
                data.getTopProducts().stream().limit(10).forEach(product -> {
                    productsTable.addCell(new Cell().add(new Paragraph(product.getProductName())));
                    productsTable.addCell(new Cell().add(new Paragraph(String.valueOf(product.getOrderCount()))));
                });
                document.add(productsTable);
            }
            
            document.close();
            return baos.toByteArray();
            
        } catch (Exception e) {
            log.error("Error generating restaurant PDF report", e);
            throw new RuntimeException("Failed to generate PDF report", e);
        }
    }
}