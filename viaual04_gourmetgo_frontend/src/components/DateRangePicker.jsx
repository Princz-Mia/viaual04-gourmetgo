import React, { useState } from 'react';
import DatePicker from 'react-datepicker';
import 'react-datepicker/dist/react-datepicker.css';
import '../styles/datepicker.css';

const DateRangePicker = ({ startDate, endDate, onDateChange, className = '' }) => {
    const [isOpen, setIsOpen] = useState(false);

    const presetRanges = [
        {
            label: 'Today',
            getValue: () => ({
                startDate: new Date(),
                endDate: new Date()
            })
        },
        {
            label: 'Yesterday',
            getValue: () => {
                const yesterday = new Date();
                yesterday.setDate(yesterday.getDate() - 1);
                return {
                    startDate: yesterday,
                    endDate: yesterday
                };
            }
        },
        {
            label: 'Last 7 days',
            getValue: () => ({
                startDate: new Date(Date.now() - 7 * 24 * 60 * 60 * 1000),
                endDate: new Date()
            })
        },
        {
            label: 'Last 30 days',
            getValue: () => ({
                startDate: new Date(Date.now() - 30 * 24 * 60 * 60 * 1000),
                endDate: new Date()
            })
        },
        {
            label: 'This month',
            getValue: () => {
                const now = new Date();
                return {
                    startDate: new Date(now.getFullYear(), now.getMonth(), 1),
                    endDate: new Date()
                };
            }
        },
        {
            label: 'Last month',
            getValue: () => {
                const now = new Date();
                const lastMonth = new Date(now.getFullYear(), now.getMonth() - 1, 1);
                const lastDayOfLastMonth = new Date(now.getFullYear(), now.getMonth(), 0);
                return {
                    startDate: lastMonth,
                    endDate: lastDayOfLastMonth
                };
            }
        }
    ];

    const handlePresetClick = (preset) => {
        const range = preset.getValue();
        onDateChange('startDate', range.startDate.toISOString().split('T')[0]);
        onDateChange('endDate', range.endDate.toISOString().split('T')[0]);
        setIsOpen(false);
    };

    const formatDateRange = () => {
        if (!startDate || !endDate) return 'Select date range';
        
        const start = new Date(startDate);
        const end = new Date(endDate);
        
        if (start.toDateString() === end.toDateString()) {
            return start.toLocaleDateString('en-US', { 
                month: 'short', 
                day: 'numeric', 
                year: 'numeric' 
            });
        }
        
        return `${start.toLocaleDateString('en-US', { 
            month: 'short', 
            day: 'numeric' 
        })} - ${end.toLocaleDateString('en-US', { 
            month: 'short', 
            day: 'numeric', 
            year: 'numeric' 
        })}`;
    };

    return (
        <div className={`relative ${className}`}>
            <button
                onClick={() => setIsOpen(!isOpen)}
                className="flex items-center space-x-2 px-4 py-2 bg-white border border-gray-300 rounded-lg shadow-sm hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
            >
                <svg className="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                </svg>
                <span className="text-sm font-medium text-gray-700">{formatDateRange()}</span>
                <svg className={`w-4 h-4 text-gray-400 transition-transform ${isOpen ? 'rotate-180' : ''}`} fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                </svg>
            </button>

            {isOpen && (
                <div className="absolute top-full left-0 mt-2 bg-white border border-gray-200 rounded-lg shadow-lg z-50 min-w-max">
                    <div className="flex">
                        {/* Preset Ranges */}
                        <div className="p-3 border-r border-gray-200">
                            <h4 className="text-sm font-medium text-gray-900 mb-2">Quick Select</h4>
                            <div className="space-y-1">
                                {presetRanges.map((preset, index) => (
                                    <button
                                        key={index}
                                        onClick={() => handlePresetClick(preset)}
                                        className="block w-full text-left px-3 py-2 text-sm text-gray-700 hover:bg-gray-100 rounded"
                                    >
                                        {preset.label}
                                    </button>
                                ))}
                            </div>
                        </div>

                        {/* Date Pickers */}
                        <div className="p-3">
                            <div className="flex space-x-4">
                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-1">Start Date</label>
                                    <DatePicker
                                        selected={startDate ? new Date(startDate) : null}
                                        onChange={(date) => onDateChange('startDate', date.toISOString().split('T')[0])}
                                        selectsStart
                                        startDate={startDate ? new Date(startDate) : null}
                                        endDate={endDate ? new Date(endDate) : null}
                                        maxDate={new Date()}
                                        className="px-3 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                                        placeholderText="Select start date"
                                        inline
                                    />
                                </div>
                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-1">End Date</label>
                                    <DatePicker
                                        selected={endDate ? new Date(endDate) : null}
                                        onChange={(date) => onDateChange('endDate', date.toISOString().split('T')[0])}
                                        selectsEnd
                                        startDate={startDate ? new Date(startDate) : null}
                                        endDate={endDate ? new Date(endDate) : null}
                                        minDate={startDate ? new Date(startDate) : null}
                                        maxDate={new Date()}
                                        className="px-3 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                                        placeholderText="Select end date"
                                        inline
                                    />
                                </div>
                            </div>
                            <div className="flex justify-end space-x-2 mt-4 pt-3 border-t border-gray-200">
                                <button
                                    onClick={() => setIsOpen(false)}
                                    className="px-4 py-2 text-sm text-gray-600 hover:text-gray-800"
                                >
                                    Cancel
                                </button>
                                <button
                                    onClick={() => setIsOpen(false)}
                                    className="px-4 py-2 bg-blue-600 text-white text-sm rounded-md hover:bg-blue-700"
                                >
                                    Apply
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            )}

            {/* Overlay */}
            {isOpen && (
                <div 
                    className="fixed inset-0 z-40" 
                    onClick={() => setIsOpen(false)}
                />
            )}
        </div>
    );
};

export default DateRangePicker;