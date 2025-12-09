import React from 'react';

const QuickActions = ({ userRole, onAction }) => {
    const adminActions = [
        {
            id: 'system-health',
            title: 'System Health',
            description: 'View logs, errors & performance',
            icon: (
                <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
            ),
            color: 'green'
        },
        {
            id: 'user-analytics',
            title: 'User Analytics',
            description: 'Registrations, logins & activity',
            icon: (
                <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
                </svg>
            ),
            color: 'purple'
        },
        {
            id: 'business-insights',
            title: 'Business Insights',
            description: 'Revenue trends & forecasts',
            icon: (
                <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6" />
                </svg>
            ),
            color: 'blue'
        }
    ];

    const restaurantActions = [
        {
            id: 'menu-performance',
            title: 'Menu Analytics',
            description: 'Top items & optimization tips',
            icon: (
                <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
                </svg>
            ),
            color: 'blue'
        },
        {
            id: 'customer-insights',
            title: 'Customer Analytics',
            description: 'Orders, reviews & preferences',
            icon: (
                <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                </svg>
            ),
            color: 'purple'
        }
    ];

    const actions = userRole === 'ROLE_ADMIN' ? adminActions : restaurantActions;

    const getColorClasses = (color) => {
        const colors = {
            blue: 'bg-blue-50 hover:bg-blue-100 text-blue-700 border-blue-200',
            green: 'bg-green-50 hover:bg-green-100 text-green-700 border-green-200',
            purple: 'bg-purple-50 hover:bg-purple-100 text-purple-700 border-purple-200',
            red: 'bg-red-50 hover:bg-red-100 text-red-700 border-red-200'
        };
        return colors[color] || colors.blue;
    };

    return (
        <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
            <h3 className="text-lg font-semibold text-gray-900 mb-4">Analytics Hub</h3>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                {actions.map((action) => (
                    <button
                        key={action.id}
                        onClick={() => onAction && onAction(action.id)}
                        className={`p-4 rounded-lg border transition-all duration-200 hover:shadow-md ${getColorClasses(action.color)}`}
                    >
                        <div className="flex items-center space-x-3">
                            <div className="flex-shrink-0">
                                {action.icon}
                            </div>
                            <div className="flex-1 text-left">
                                <h4 className="font-medium text-sm">{action.title}</h4>
                                <p className="text-xs opacity-75 mt-1">{action.description}</p>
                            </div>
                        </div>
                    </button>
                ))}
            </div>
        </div>
    );
};

export default QuickActions;