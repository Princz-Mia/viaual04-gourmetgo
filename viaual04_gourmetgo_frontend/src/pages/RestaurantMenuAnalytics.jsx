import React, { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { getRestaurantDashboard } from '../api/statisticsService';
import { toast } from 'react-toastify';
import { useNavigate } from 'react-router-dom';
import LoadingSpinner from '../components/LoadingSpinner';
import StatCard from '../components/charts/StatCard';
import LineChart from '../components/charts/LineChart';
import PieChart from '../components/charts/PieChart';
import DateRangePicker from '../components/DateRangePicker';

const RestaurantMenuAnalytics = () => {
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
        fetchMenuAnalytics();
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

    const fetchMenuAnalytics = async () => {
        try {
            setLoading(true);
            const data = await getRestaurantDashboard(user.id, dateRange.startDate, dateRange.endDate);
            setAnalytics({
                topProducts: data.topProducts || [],
                topCategories: data.topCategories || [],
                totalProducts: data.topProducts?.length || 0,
                totalCategories: data.topCategories?.length || 0,
                totalOrders: data.orderStats?.totalOrders || 0,
                averageOrderValue: data.revenueStats?.dailyRevenue / Math.max(data.orderStats?.totalOrders, 1) || 0,
                cancellationRate: data.orderStats?.cancellationRate || 0,
                dailyOrders: data.orderStats?.dailyOrders || {},
                dailyRevenue: data.revenueStats?.dailyRevenueChart || {}
            });
        } catch (error) {
            console.error('Menu analytics error:', error);
            toast.error('Failed to fetch menu analytics');
        } finally {
            setLoading(false);
        }
    };

    if (loading) {
        return <LoadingSpinner text="Loading menu analytics..." />;
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
                        <h1 className="text-3xl font-bold text-gray-900">Menu Performance</h1>
                    </div>
                    <div className="flex items-center space-x-4">
                        <DateRangePicker
                            startDate={dateRange.startDate}
                            endDate={dateRange.endDate}
                            onDateChange={handleDateRangeChange}
                        />
                        <button
                            onClick={fetchMenuAnalytics}
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
                
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-6 mb-8">
                    <StatCard
                        title="Total Products"
                        value={analytics.totalProducts}
                        subtitle="In menu"
                        color="blue"
                    />
                    <StatCard
                        title="Active Categories"
                        value={analytics.totalCategories}
                        subtitle="Categories sold"
                        color="indigo"
                    />
                    <StatCard
                        title="Total Orders"
                        value={analytics.totalOrders}
                        subtitle="Selected period"
                        color="green"
                    />
                    <StatCard
                        title="Avg Order Value"
                        value={`$${analytics.averageOrderValue?.toFixed(2) || '0.00'}`}
                        subtitle="Per order"
                        color="purple"
                    />
                    <StatCard
                        title="Cancellation Rate"
                        value={`${analytics.cancellationRate?.toFixed(1) || '0.0'}%`}
                        subtitle="Order cancellations"
                        color="red"
                    />
                </div>

                <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
                    <div className="bg-white p-6 rounded-lg shadow">
                        <h3 className="text-lg font-semibold mb-4">Top Performing Products</h3>
                        <div className="space-y-3">
                            {analytics.topProducts?.slice(0, 5).map((product, index) => (
                                <div key={index} className="flex justify-between items-center p-3 bg-gray-50 rounded">
                                    <div>
                                        <span className="font-medium">{product.productName}</span>
                                        <div className="text-sm text-gray-500">#{index + 1} Best Seller</div>
                                    </div>
                                    <span className="text-blue-600 font-semibold">{product.orderCount} orders</span>
                                </div>
                            )) || <div className="text-gray-500 text-center py-4">No data available</div>}
                        </div>
                    </div>
                    
                    <PieChart
                        data={analytics.topProducts?.slice(0, 5).map(p => ({
                            label: p.productName,
                            value: p.orderCount
                        })) || []}
                        title="Product Performance Distribution"
                    />
                </div>
                
                <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
                    <LineChart
                        data={analytics.dailyOrders ? 
                            Object.entries(analytics.dailyOrders)
                                .map(([date, orders]) => ({ date, orders }))
                                .sort((a, b) => new Date(a.date) - new Date(b.date)) : []}
                        title={getTimeScale() === 'hours' ? 'Hourly Orders' : 'Daily Orders'}
                        xKey="date"
                        yKey="orders"
                        color="#3B82F6"
                        timeScale={getTimeScale()}
                    />
                    <LineChart
                        data={analytics.dailyRevenue ? 
                            Object.entries(analytics.dailyRevenue)
                                .map(([date, revenue]) => ({ date, revenue: parseFloat(revenue) || 0 }))
                                .sort((a, b) => new Date(a.date) - new Date(b.date)) : []}
                        title={getTimeScale() === 'hours' ? 'Hourly Revenue' : 'Daily Revenue'}
                        xKey="date"
                        yKey="revenue"
                        color="#10B981"
                        timeScale={getTimeScale()}
                    />
                </div>
                
                <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                    <div className="bg-white p-6 rounded-lg shadow">
                        <h3 className="text-lg font-semibold mb-4">Top Performing Categories</h3>
                        <div className="space-y-3">
                            {analytics.topCategories?.slice(0, 5).map((category, index) => (
                                <div key={index} className="flex justify-between items-center p-3 bg-gray-50 rounded">
                                    <div>
                                        <span className="font-medium">{category.categoryName}</span>
                                        <div className="text-sm text-gray-500">#{index + 1} Category</div>
                                    </div>
                                    <span className="text-green-600 font-semibold">{category.orderCount} orders</span>
                                </div>
                            )) || <div className="text-gray-500 text-center py-4">No data available</div>}
                        </div>
                    </div>
                    
                    <PieChart
                        data={analytics.topCategories?.slice(0, 5).map(c => ({
                            label: c.categoryName,
                            value: c.orderCount
                        })) || []}
                        title="Category Performance Distribution"
                    />
                </div>
            </div>
        </div>
    );
};

export default RestaurantMenuAnalytics;