import React, { useState } from 'react';
import DateRangePicker from './DateRangePicker';
import RealTimeStats from './RealTimeStats';
import ReportCustomizer from './ReportCustomizer';
import { generateAdminPdfReport, generateRestaurantPdfReport, generateClientSidePdfReport } from '../api/reportService';
import { toast } from 'react-toastify';

const DashboardHeader = ({ 
    title, 
    dateRange, 
    onDateChange, 
    userRole, 
    userId,
    onRefresh,
    isLoading = false 
}) => {
    const [isGeneratingPdf, setIsGeneratingPdf] = useState(false);
    const [showReportCustomizer, setShowReportCustomizer] = useState(false);

    const handleGeneratePdf = async (type = 'server', customSections = null) => {
        setIsGeneratingPdf(true);
        try {
            if (type === 'server') {
                const params = {
                    startDate: dateRange.startDate,
                    endDate: dateRange.endDate,
                    sections: customSections
                };
                
                if (userRole === 'ROLE_ADMIN') {
                    await generateAdminPdfReport(params.startDate, params.endDate, params.sections);
                } else if (userRole === 'ROLE_RESTAURANT') {
                    await generateRestaurantPdfReport(userId, params.startDate, params.endDate, params.sections);
                }
                toast.success('Custom PDF report generated successfully!');
            } else {
                const filename = `${userRole === 'ROLE_ADMIN' ? 'admin' : 'restaurant'}-dashboard-${dateRange.startDate}-to-${dateRange.endDate}.pdf`;
                await generateClientSidePdfReport('dashboard-content', filename);
                toast.success('Dashboard exported as PDF!');
            }
        } catch (error) {
            console.error('Error generating PDF:', error);
            toast.error('Failed to generate PDF report');
        } finally {
            setIsGeneratingPdf(false);
        }
    };

    const handleRefresh = () => {
        if (onRefresh) {
            onRefresh();
            toast.info('Dashboard refreshed');
        }
    };

    return (
        <div className="bg-white shadow-sm border-b border-gray-200 px-6 py-4">
            <div className="flex flex-col lg:flex-row lg:items-center lg:justify-between space-y-4 lg:space-y-0">
                <div className="flex items-center space-x-4">
                    <h1 className="text-2xl font-bold text-gray-900">{title}</h1>
                    {isLoading && (
                        <div className="flex items-center space-x-2 text-blue-600">
                            <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-blue-600"></div>
                            <span className="text-sm">Loading...</span>
                        </div>
                    )}
                    <RealTimeStats />
                </div>

                <div className="flex flex-col sm:flex-row items-start sm:items-center space-y-3 sm:space-y-0 sm:space-x-4">
                    {/* Date Range Picker */}
                    <DateRangePicker
                        startDate={dateRange.startDate}
                        endDate={dateRange.endDate}
                        onDateChange={onDateChange}
                        className="w-full sm:w-auto"
                    />

                    {/* Action Buttons */}
                    <div className="flex items-center space-x-2 w-full sm:w-auto">
                        <button
                            onClick={handleRefresh}
                            disabled={isLoading}
                            className="flex items-center space-x-2 px-3 py-2 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                        >
                            <svg className={`w-4 h-4 ${isLoading ? 'animate-spin' : ''}`} fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
                            </svg>
                            <span className="text-sm font-medium">Refresh</span>
                        </button>

                        {/* PDF Export Dropdown */}
                        <div className="relative group">
                            <button
                                disabled={isGeneratingPdf}
                                className="flex items-center space-x-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                            >
                                <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                                </svg>
                                <span className="text-sm font-medium">
                                    {isGeneratingPdf ? 'Generating...' : 'Export PDF'}
                                </span>
                                <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                                </svg>
                            </button>

                            {/* Dropdown Menu */}
                            <div className="absolute right-0 mt-2 w-48 bg-white rounded-md shadow-lg border border-gray-200 opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-200 z-50">
                                <div className="py-1">
                                    <button
                                        onClick={() => setShowReportCustomizer(true)}
                                        disabled={isGeneratingPdf}
                                        className="block w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 disabled:opacity-50"
                                    >
                                        <div className="flex items-center space-x-2">
                                            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                                            </svg>
                                            <span>Custom Report</span>
                                        </div>
                                        <p className="text-xs text-gray-500 ml-6">Choose sections to include</p>
                                    </button>

                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <ReportCustomizer
                isOpen={showReportCustomizer}
                onClose={() => setShowReportCustomizer(false)}
                onGenerate={(sections) => handleGeneratePdf('server', sections)}
                userRole={userRole}
            />
        </div>
    );
};

export default DashboardHeader;