import React, { useState, useEffect } from 'react';

const StatCard = ({ title, value, subtitle, icon, color = 'blue', trend, previousValue }) => {
    const [animatedValue, setAnimatedValue] = useState(previousValue || value);
    const [isUpdating, setIsUpdating] = useState(false);
    const colorClasses = {
        blue: 'bg-blue-50 text-blue-600 border-blue-200',
        green: 'bg-green-50 text-green-600 border-green-200',
        red: 'bg-red-50 text-red-600 border-red-200',
        yellow: 'bg-yellow-50 text-yellow-600 border-yellow-200',
        purple: 'bg-purple-50 text-purple-600 border-purple-200'
    };

    useEffect(() => {
        if (previousValue !== undefined && previousValue !== value) {
            setIsUpdating(true);
            
            const startValue = typeof previousValue === 'number' ? previousValue : 0;
            const endValue = typeof value === 'number' ? value : 0;
            const duration = 1000;
            const startTime = Date.now();
            
            const animate = () => {
                const elapsed = Date.now() - startTime;
                const progress = Math.min(elapsed / duration, 1);
                
                const currentValue = startValue + (endValue - startValue) * progress;
                setAnimatedValue(typeof value === 'number' ? Math.round(currentValue) : value);
                
                if (progress < 1) {
                    requestAnimationFrame(animate);
                } else {
                    setAnimatedValue(value);
                    setIsUpdating(false);
                }
            };
            
            requestAnimationFrame(animate);
        } else {
            setAnimatedValue(value);
        }
    }, [value, previousValue]);

    return (
        <div className={`bg-white p-6 rounded-lg shadow border-l-4 ${colorClasses[color]} transition-all duration-300 ${isUpdating ? 'ring-2 ring-blue-200 ring-opacity-50' : ''}`}>
            <div className="flex items-center justify-between">
                <div>
                    <p className="text-sm font-medium text-gray-600">{title}</p>
                    <p className={`text-2xl font-bold text-gray-900 transition-all duration-300 ${isUpdating ? 'scale-110' : ''}`}>{animatedValue}</p>
                    {subtitle && (
                        <p className="text-sm text-gray-500">{subtitle}</p>
                    )}
                    {trend && (
                        <div className={`flex items-center mt-2 text-sm ${
                            trend.direction === 'up' ? 'text-green-600' : 
                            trend.direction === 'down' ? 'text-red-600' : 'text-gray-600'
                        }`}>
                            {trend.direction === 'up' && (
                                <svg className="w-4 h-4 mr-1" fill="currentColor" viewBox="0 0 20 20">
                                    <path fillRule="evenodd" d="M3.293 9.707a1 1 0 010-1.414l6-6a1 1 0 011.414 0l6 6a1 1 0 01-1.414 1.414L11 5.414V17a1 1 0 11-2 0V5.414L4.707 9.707a1 1 0 01-1.414 0z" clipRule="evenodd" />
                                </svg>
                            )}
                            {trend.direction === 'down' && (
                                <svg className="w-4 h-4 mr-1" fill="currentColor" viewBox="0 0 20 20">
                                    <path fillRule="evenodd" d="M16.707 10.293a1 1 0 010 1.414l-6 6a1 1 0 01-1.414 0l-6-6a1 1 0 111.414-1.414L9 14.586V3a1 1 0 012 0v11.586l4.293-4.293a1 1 0 011.414 0z" clipRule="evenodd" />
                                </svg>
                            )}
                            {trend.value}
                        </div>
                    )}
                </div>
                {icon && (
                    <div className={`p-3 rounded-full ${colorClasses[color]} ${isUpdating ? 'animate-pulse' : ''}`}>
                        {icon}
                    </div>
                )}
            </div>
            {isUpdating && (
                <div className="mt-2">
                    <div className="w-full bg-gray-200 rounded-full h-1">
                        <div className="bg-blue-600 h-1 rounded-full animate-pulse" style={{ width: '100%' }}></div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default StatCard;