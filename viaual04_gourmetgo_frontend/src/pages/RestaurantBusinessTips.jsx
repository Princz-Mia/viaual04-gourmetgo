import React, { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { getRestaurantDashboard } from '../api/statisticsService';
import { toast } from 'react-toastify';
import { useNavigate } from 'react-router-dom';
import StatCard from '../components/charts/StatCard';

const RestaurantBusinessTips = () => {
    const { user } = useAuth();
    const navigate = useNavigate();
    const [analytics, setAnalytics] = useState({});
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const userRole = user?.role?.authority || user?.role;
        if (userRole !== 'ROLE_RESTAURANT') {
            toast.error('Access denied. Restaurant role required.');
            return;
        }
        fetchBusinessData();
    }, [user]);

    const fetchBusinessData = async () => {
        try {
            setLoading(true);
            const data = await getRestaurantDashboard(user.id);
            setAnalytics({
                cancellationRate: data.orderStats?.cancellationRate || 0,
                averagePreparationTime: data.orderStats?.averagePreparationTime || 0,
                monthlyRevenue: data.revenueStats?.monthlyRevenue || 0,
                totalOrders: data.orderStats?.totalOrders || 0
            });
        } catch (error) {
            toast.error('Failed to fetch business data');
        } finally {
            setLoading(false);
        }
    };

    const generateTips = () => {
        const tips = [];
        
        if (analytics.cancellationRate > 10) {
            tips.push({
                type: 'warning',
                title: 'High Cancellation Rate',
                description: 'Your cancellation rate is above 10%. Consider improving order accuracy and preparation time.',
                action: 'Review order process and staff training'
            });
        }

        if (analytics.averagePreparationTime > 30) {
            tips.push({
                type: 'info',
                title: 'Preparation Time Optimization',
                description: 'Average preparation time is over 30 minutes. Streamlining kitchen operations could improve customer satisfaction.',
                action: 'Optimize kitchen workflow and menu complexity'
            });
        }

        if (analytics.totalOrders < 50) {
            tips.push({
                type: 'success',
                title: 'Marketing Opportunity',
                description: 'Increase visibility with promotions and social media marketing to attract more customers.',
                action: 'Create promotional campaigns and improve online presence'
            });
        }

        return tips;
    };

    if (loading) {
        return (
            <div className="min-h-screen bg-gray-50 flex items-center justify-center">
                <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-blue-600"></div>
            </div>
        );
    }

    const tips = generateTips();

    return (
        <div className="min-h-screen bg-gray-50 p-6">
            <div className="max-w-7xl mx-auto">
                <div className="flex items-center justify-between mb-8">
                    <div className="flex items-center">
                        <button
                            onClick={() => navigate('/restaurant/dashboard')}
                            className="mr-4 p-2 text-gray-600 hover:text-gray-900 hover:bg-gray-100 rounded-lg transition-colors cursor-pointer"
                        >
                            <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
                            </svg>
                        </button>
                        <h1 className="text-3xl font-bold text-gray-900">Business Optimization</h1>
                    </div>
                    <button
                        onClick={fetchBusinessData}
                        disabled={loading}
                        className="btn btn-primary btn-sm"
                    >
                        Refresh
                    </button>
                </div>
                
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
                    <StatCard
                        title="Cancellation Rate"
                        value={`${analytics.cancellationRate?.toFixed(1)}%`}
                        subtitle="Of total orders"
                        color={analytics.cancellationRate > 10 ? 'red' : 'green'}
                    />
                    <StatCard
                        title="Prep Time"
                        value={`${analytics.averagePreparationTime?.toFixed(0)} min`}
                        subtitle="Average per order"
                        color={analytics.averagePreparationTime > 30 ? 'yellow' : 'green'}
                    />
                    <StatCard
                        title="Monthly Revenue"
                        value={`$${analytics.monthlyRevenue?.toLocaleString()}`}
                        subtitle="Last 30 days"
                        color="blue"
                    />
                    <StatCard
                        title="Total Orders"
                        value={analytics.totalOrders}
                        subtitle="In period"
                        color="purple"
                    />
                </div>

                <div className="space-y-6">
                    <h2 className="text-2xl font-bold text-gray-900">Personalized Business Tips</h2>
                    
                    {tips.length > 0 ? (
                        <div className="grid gap-6">
                            {tips.map((tip, index) => (
                                <div key={index} className={`p-6 rounded-lg border-l-4 ${
                                    tip.type === 'warning' ? 'bg-red-50 border-red-400' :
                                    tip.type === 'info' ? 'bg-blue-50 border-blue-400' :
                                    'bg-green-50 border-green-400'
                                }`}>
                                    <h3 className="text-lg font-semibold mb-2">{tip.title}</h3>
                                    <p className="text-gray-700 mb-3">{tip.description}</p>
                                    <p className="text-sm font-medium text-gray-900">Recommended Action: {tip.action}</p>
                                </div>
                            ))}
                        </div>
                    ) : (
                        <div className="bg-green-50 border-l-4 border-green-400 p-6 rounded-lg">
                            <h3 className="text-lg font-semibold text-green-800 mb-2">Great Performance!</h3>
                            <p className="text-green-700">Your restaurant is performing well across all key metrics. Keep up the excellent work!</p>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default RestaurantBusinessTips;