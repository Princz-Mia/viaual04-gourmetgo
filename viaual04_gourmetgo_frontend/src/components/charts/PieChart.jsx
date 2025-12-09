import React from 'react';

const PieChart = ({ data, title, colors = ['#3B82F6', '#EF4444', '#10B981', '#F59E0B', '#8B5CF6'] }) => {
    if (!data || data.length === 0) {
        return (
            <div className="bg-white p-6 rounded-lg shadow">
                <h3 className="text-lg font-semibold mb-4">{title}</h3>
                <div className="text-gray-500 text-center py-8">No data available</div>
            </div>
        );
    }

    const total = data.reduce((sum, item) => sum + item.value, 0);
    let currentAngle = 0;

    const slices = data.map((item, index) => {
        const percentage = (item.value / total) * 100;
        const angle = (item.value / total) * 360;
        const startAngle = currentAngle;
        const endAngle = currentAngle + angle;
        
        let pathData;
        
        if (data.length === 1 || angle >= 359.9) {
            // Full circle for single item or 100%
            pathData = `M 100 20 A 80 80 0 1 1 99.9 20 Z`;
        } else {
            const x1 = 100 + 80 * Math.cos((startAngle - 90) * Math.PI / 180);
            const y1 = 100 + 80 * Math.sin((startAngle - 90) * Math.PI / 180);
            const x2 = 100 + 80 * Math.cos((endAngle - 90) * Math.PI / 180);
            const y2 = 100 + 80 * Math.sin((endAngle - 90) * Math.PI / 180);
            
            const largeArcFlag = angle > 180 ? 1 : 0;
            
            pathData = [
                `M 100 100`,
                `L ${x1} ${y1}`,
                `A 80 80 0 ${largeArcFlag} 1 ${x2} ${y2}`,
                'Z'
            ].join(' ');
        }
        
        currentAngle += angle;
        
        return {
            path: pathData,
            color: colors[index % colors.length],
            label: item.label,
            value: item.value,
            percentage: percentage.toFixed(1)
        };
    });

    return (
        <div className="bg-white p-6 rounded-lg shadow">
            <h3 className="text-lg font-semibold mb-4">{title}</h3>
            <div className="flex items-center justify-between">
                <div className="relative">
                    <svg width="200" height="200" viewBox="0 0 200 200">
                        {slices.map((slice, index) => (
                            <path
                                key={index}
                                d={slice.path}
                                fill={slice.color}
                                stroke="white"
                                strokeWidth="2"
                            />
                        ))}
                    </svg>
                </div>
                <div className="ml-6 space-y-2">
                    {slices.map((slice, index) => (
                        <div key={index} className="flex items-center space-x-2">
                            <div 
                                className="w-4 h-4 rounded"
                                style={{ backgroundColor: slice.color }}
                            ></div>
                            <span className="text-sm text-gray-700">
                                {slice.label}: {slice.value} ({slice.percentage}%)
                            </span>
                        </div>
                    ))}
                </div>
            </div>
        </div>
    );
};

export default PieChart;