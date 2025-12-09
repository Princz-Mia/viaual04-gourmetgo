import React, { useState } from 'react';

const ReportCustomizer = ({ isOpen, onClose, onGenerate, userRole }) => {
    const [selectedSections, setSelectedSections] = useState({
        systemHealth: true,
        userAnalytics: true,
        businessInsights: true,
        topProducts: true,
        performanceMetrics: true
    });

    const adminSections = [
        { key: 'systemHealth', label: 'System Health Metrics', description: 'CPU, memory, uptime, error rates' },
        { key: 'userAnalytics', label: 'User Analytics', description: 'Registration stats, user counts' },
        { key: 'businessInsights', label: 'Business Insights', description: 'Revenue, orders, growth metrics' },
        { key: 'topProducts', label: 'Top Products', description: 'Most popular items across platform' },
        { key: 'performanceMetrics', label: 'Performance Metrics', description: 'Response times, endpoint usage' }
    ];

    const restaurantSections = [
        { key: 'businessInsights', label: 'Revenue Analytics', description: 'Daily, weekly, monthly revenue' },
        { key: 'topProducts', label: 'Menu Performance', description: 'Best selling items' },
        { key: 'userAnalytics', label: 'Customer Insights', description: 'Order patterns, customer data' }
    ];

    const sections = userRole === 'ROLE_ADMIN' ? adminSections : restaurantSections;

    const handleSectionToggle = (key) => {
        setSelectedSections(prev => ({
            ...prev,
            [key]: !prev[key]
        }));
    };

    const handleGenerate = () => {
        onGenerate(selectedSections);
        onClose();
    };

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
            <div className="bg-white rounded-lg shadow-xl w-full max-w-2xl">
                <div className="flex items-center justify-between p-6 border-b">
                    <h3 className="text-lg font-semibold text-gray-900">Customize Report</h3>
                    <button
                        onClick={onClose}
                        className="text-gray-400 hover:text-gray-600"
                    >
                        <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                        </svg>
                    </button>
                </div>

                <div className="p-6">
                    <p className="text-sm text-gray-600 mb-4">Select which sections to include in your detailed report:</p>
                    
                    <div className="space-y-3">
                        {sections.map((section) => (
                            <label key={section.key} className="flex items-start space-x-3 cursor-pointer">
                                <input
                                    type="checkbox"
                                    checked={selectedSections[section.key]}
                                    onChange={() => handleSectionToggle(section.key)}
                                    className="mt-1 h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                                />
                                <div className="flex-1">
                                    <div className="text-sm font-medium text-gray-900">{section.label}</div>
                                    <div className="text-xs text-gray-500">{section.description}</div>
                                </div>
                            </label>
                        ))}
                    </div>
                </div>

                <div className="flex justify-end space-x-3 p-6 border-t bg-gray-50">
                    <button
                        onClick={onClose}
                        className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50"
                    >
                        Cancel
                    </button>
                    <button
                        onClick={handleGenerate}
                        disabled={Object.values(selectedSections).every(v => !v)}
                        className="px-4 py-2 text-sm font-medium text-white bg-blue-600 border border-transparent rounded-md hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                        Generate Report
                    </button>
                </div>
            </div>
        </div>
    );
};

export default ReportCustomizer;