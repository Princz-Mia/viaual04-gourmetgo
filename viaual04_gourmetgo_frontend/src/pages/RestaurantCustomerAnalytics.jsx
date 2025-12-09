import React, { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { getRestaurantDashboard } from '../api/statisticsService';
import { fetchReviewsByRestaurant } from '../api/reviewService';
import { toast } from 'react-toastify';
import { useNavigate } from 'react-router-dom';
import LoadingSpinner from '../components/LoadingSpinner';
import StatCard from '../components/charts/StatCard';
import LineChart from '../components/charts/LineChart';
import PieChart from '../components/charts/PieChart';
import DateRangePicker from '../components/DateRangePicker';

const RestaurantCustomerAnalytics = () => {
    const { user } = useAuth();
    const navigate = useNavigate();
    const [analytics, setAnalytics] = useState({});
    const [loading, setLoading] = useState(true);
    const [dateRange, setDateRange] = useState({
        startDate: new Date(Date.now() - 7 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
        endDate: new Date().toISOString().split('T')[0]
    });

    useEffect(() => {
        const userRole = user?.role?.authority || user?.role;
        if (userRole !== 'ROLE_RESTAURANT') {
            toast.error('Access denied. Restaurant role required.');
            return;
        }
        fetchCustomerAnalytics();
    }, [user, dateRange.startDate, dateRange.endDate]);

    const getTimeScale = () => {
        const start = new Date(dateRange.startDate);
        const end = new Date(dateRange.endDate);
        const diffDays = Math.ceil((end - start) / (1000 * 60 * 60 * 24));
        return diffDays <= 1 ? 'hours' : 'days';
    };

    const handleDateRangeChange = (field, value) => {
        setDateRange(prev => ({
            ...prev,
            [field]: value
        }));
    };

    const fetchCustomerAnalytics = async () => {
        try {
            setLoading(true);
            const [dashboardData, reviewsData] = await Promise.all([
                getRestaurantDashboard(user.id, dateRange.startDate, dateRange.endDate),
                fetchReviewsByRestaurant(user.id)
            ]);
            
            const ratingDistribution = [1,2,3,4,5].map(rating => ({
                label: `${rating} Star`,
                value: reviewsData?.filter(r => r.ratingValue === rating).length || 0
            }));
            
            setAnalytics({
                totalCustomers: dashboardData.customerStats?.totalCustomers || 0,
                repeatCustomers: dashboardData.customerStats?.repeatCustomers || 0,
                totalReviews: reviewsData?.length || 0,
                averageRating: reviewsData?.length ? (reviewsData.reduce((sum, r) => sum + r.ratingValue, 0) / reviewsData.length) : 0,
                dailyOrders: dashboardData.orderStats?.dailyOrders || {},
                ratingDistribution
            });
        } catch (error) {
            console.error('Customer analytics error:', error);
            toast.error('Failed to fetch customer analytics');
        } finally {
            setLoading(false);
        }
    };

    const ordersChartData = analytics.dailyOrders ? 
        Object.entries(analytics.dailyOrders)
            .map(([date, orders]) => ({ date, orders }))
            .filter(item => item.orders > 0)
            .sort((a, b) => new Date(a.date) - new Date(b.date)) : [];

    if (loading) {
        return <LoadingSpinner text="Loading customer analytics..." />;
    }

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
                        <h1 className="text-3xl font-bold text-gray-900">Customer Insights</h1>
                    </div>
                    <div className="flex items-center space-x-4">
                        <DateRangePicker
                            startDate={dateRange.startDate}
                            endDate={dateRange.endDate}
                            onDateChange={handleDateRangeChange}
                        />
                        <button
                            onClick={fetchCustomerAnalytics}
                            disabled={loading}
                            className="flex items-center space-x-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                        >
                            <svg className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
                            </svg>
                            <span className="text-sm font-medium">Refresh</span>
                        </button>
                    </div>
                </div>
                
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
                    <StatCard
                        title="Total Customers"
                        value={analytics.totalCustomers}
                        subtitle="Unique customers"
                        color="blue"
                    />
                    <StatCard
                        title="Repeat Customers"
                        value={analytics.repeatCustomers}
                        subtitle="Returning customers"
                        color="green"
                    />
                    <StatCard
                        title="Total Reviews"
                        value={analytics.totalReviews}
                        subtitle="Customer feedback"
                        color="purple"
                    />
                    <StatCard
                        title="Average Rating"
                        value={analytics.averageRating?.toFixed(1) || '0.0'}
                        subtitle="Customer satisfaction"
                        color="yellow"
                    />
                </div>

                <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                    <LineChart
                        data={ordersChartData}
                        title={getTimeScale() === 'hours' ? 'Hourly Customer Orders' : 'Daily Customer Orders'}
                        xKey="date"
                        yKey="orders"
                        color="#10B981"
                        timeScale={getTimeScale()}
                    />
                    
                    <PieChart
                        data={analytics.ratingDistribution || []}
                        title="Customer Rating Distribution"
                    />
                </div>
            </div>
        </div>
    );
};

export default RestaurantCustomerAnalytics;