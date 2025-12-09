import React from 'react';

const LineChart = ({ data, title, xKey, yKey, color = '#3B82F6', timeScale = 'days' }) => {
    if (!data || data.length === 0) {
        return (
            <div className="bg-white p-6 rounded-lg shadow">
                <h3 className="text-lg font-semibold mb-4">{title}</h3>
                <div className="text-gray-500 text-center py-8">No data available</div>
            </div>
        );
    }

    // Filter out invalid data and ensure numeric values
    const validData = data.filter(item => 
        item[yKey] !== null && 
        item[yKey] !== undefined && 
        !isNaN(Number(item[yKey]))
    ).map(item => ({
        ...item,
        [yKey]: Number(item[yKey])
    }));
    
    if (validData.length === 0) {
        return (
            <div className="bg-white p-6 rounded-lg shadow">
                <h3 className="text-lg font-semibold mb-4">{title}</h3>
                <div className="text-gray-500 text-center py-8">No valid data available</div>
            </div>
        );
    }
    
    const maxValue = Math.max(...validData.map(item => item[yKey]));
    const minValue = Math.min(...validData.map(item => item[yKey]));
    const range = maxValue - minValue || 1;

    return (
        <div className="bg-white p-6 rounded-lg shadow">
            <h3 className="text-lg font-semibold mb-4">{title}</h3>
            <div className="relative h-64">
                <svg className="w-full h-full" viewBox="0 0 400 200">
                    {/* Grid lines */}
                    {[0, 1, 2, 3, 4].map(i => (
                        <line
                            key={i}
                            x1="40"
                            y1={40 + (i * 32)}
                            x2="380"
                            y2={40 + (i * 32)}
                            stroke="#E5E7EB"
                            strokeWidth="1"
                        />
                    ))}
                    
                    {/* Y-axis labels */}
                    {[0, 1, 2, 3, 4].map(i => {
                        const value = maxValue - (i * range / 4);
                        return (
                            <text
                                key={i}
                                x="35"
                                y={45 + (i * 32)}
                                textAnchor="end"
                                fontSize="12"
                                fill="#6B7280"
                            >
                                {Math.round(value)}
                            </text>
                        );
                    })}
                    
                    {/* Line path */}
                    <path
                        d={validData.map((item, index) => {
                            const x = 40 + (index * (340 / Math.max(validData.length - 1, 1)));
                            const y = 40 + ((maxValue - item[yKey]) / range) * 128;
                            return `${index === 0 ? 'M' : 'L'} ${x} ${y}`;
                        }).join(' ')}
                        fill="none"
                        stroke={color}
                        strokeWidth="2"
                    />
                    
                    {/* Data points */}
                    {validData.map((item, index) => {
                        const x = 40 + (index * (340 / Math.max(validData.length - 1, 1)));
                        const y = 40 + ((maxValue - item[yKey]) / range) * 128;
                        return (
                            <circle
                                key={index}
                                cx={x}
                                cy={y}
                                r="4"
                                fill={color}
                            />
                        );
                    })}
                </svg>
                
                {/* X-axis labels */}
                <div className="flex justify-between mt-2 px-10">
                    {validData.length <= 7 ? 
                        validData.map((item, index) => {
                            const date = new Date(item[xKey]);
                            const isHourly = item[xKey].includes('T');
                            return (
                                <span key={index} className="text-xs text-gray-600 truncate max-w-16">
                                    {isHourly 
                                        ? date.toLocaleTimeString('en-US', { hour: 'numeric', hour12: true })
                                        : date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' })
                                    }
                                </span>
                            );
                        }) :
                        [0, Math.floor(validData.length / 4), Math.floor(validData.length / 2), Math.floor(3 * validData.length / 4), validData.length - 1]
                            .map(index => validData[index])
                            .filter(Boolean)
                            .map((item, index) => {
                                const date = new Date(item[xKey]);
                                const isHourly = item[xKey].includes('T');
                                return (
                                    <span key={index} className="text-xs text-gray-600 truncate max-w-16">
                                        {isHourly 
                                            ? date.toLocaleTimeString('en-US', { hour: 'numeric', hour12: true })
                                            : date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' })
                                        }
                                    </span>
                                );
                            })
                    }
                </div>
            </div>
        </div>
    );
};

export default LineChart;