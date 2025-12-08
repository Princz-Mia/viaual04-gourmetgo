import React, { useState, useEffect, useCallback } from 'react';
import { getRestaurantDashboard } from '../api/statisticsService';
import { generateRestaurantPdfReport } from '../api/reportService';
import LineChart from '../components/charts/LineChart';
import PieChart from '../components/charts/PieChart';
import StatCard from '../components/charts/StatCard';
import DashboardHeader from '../components/DashboardHeader';
import QuickActions from '../components/QuickActions';
import { useAuth } from '../contexts/AuthContext';
import { toast } from 'react-toastify';

const RestaurantDashboard = () => {
    const { user } = useAuth();
    const [dashboardData, setDashboardData] = useState(null);
    const [orderStats, setOrderStats] = useState(null);
    const [revenueStats, setRevenueStats] = useState(null);
    const [topProducts, setTopProducts] = useState(null);
    const [loading, setLoading] = useState(true);
    const [lastRefresh, setLastRefresh] = useState(0);
    const [dateRange, setDateRange] = useState({
        startDate: new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
        endDate: new Date().toISOString().split('T')[0]
    });

    useEffect(() => {
        const userRole = user?.role?.authority || user?.role;
        if (userRole !== 'ROLE_RESTAURANT') {
            toast.error('Access denied. Restaurant role required.');
            return;
        }
        fetchDashboardData();
        
        // Set up WebSocket for real-time updates
        import('../services/websocketService').then(({ default: websocketService }) => {
            websocketService.connect().then(() => {
                console.log('Restaurant Dashboard WebSocket connected');
                websocketService.subscribe('/topic/restaurant/statistics', (message) => {
                    console.log('Restaurant Dashboard received WebSocket message:', message);
                    if (message.restaurantId === user.id || message.restaurantId === 'ALL') {
                        if (message.type === 'ORDER_UPDATE') {
                            console.log('Updating order stats:', message.data);
                            setOrderStats(prev => ({ 
                                ...dashboardData?.orderStats, 
                                ...message.data,
                                dailyOrders: message.data?.dailyOrders || dashboardData?.orderStats?.dailyOrders || prev?.dailyOrders
                            }));
                            // Also update revenue when orders change
                            if (message.data?.dailyRevenue !== undefined) {
                                setRevenueStats(prev => ({
                                    ...prev,
                                    dailyRevenue: message.data.dailyRevenue,
                                    dailyRevenueChart: dashboardData?.revenueStats?.dailyRevenueChart || prev?.dailyRevenueChart
                                }));
                            }
                        } else if (message.type === 'REVENUE_UPDATE') {
                            console.log('Updating revenue stats:', message.data);
                            setRevenueStats(prev => ({ 
                                ...dashboardData?.revenueStats, 
                                ...message.data,
                                dailyRevenueChart: message.data?.dailyRevenueChart || dashboardData?.revenueStats?.dailyRevenueChart || prev?.dailyRevenueChart
                            }));
                        }
                    }
                });
            }).catch(console.error);
        });
        
        return () => {
            // Cleanup handled by websocket service
        };
    }, [user, dateRange]);

    const fetchDashboardData = useCallback(async () => {
        try {
            setLoading(true);
            const data = await getRestaurantDashboard(user.id, dateRange.startDate, dateRange.endDate);
            setDashboardData(data);
            // Reset WebSocket state to use fresh data
            setOrderStats(null);
            setRevenueStats(null);
            setTopProducts(null);
        } catch (error) {
            console.error('Error fetching dashboard data:', error);
            toast.error('Failed to load dashboard data');
        } finally {
            setLoading(false);
        }
    }, [user.id, dateRange.startDate, dateRange.endDate]);

    const handleDateRangeChange = (field, value) => {
        setDateRange(prev => ({
            ...prev,
            [field]: value
        }));
    };

    const handleGeneratePdf = async (type = 'server') => {
        try {
            await generateRestaurantPdfReport(user.id, dateRange.startDate, dateRange.endDate);
            toast.success('PDF report generated successfully!');
        } catch (error) {
            toast.error('Failed to generate PDF report');
        }
    };

    const userRole = user?.role?.authority || user?.role;
    if (userRole !== 'ROLE_RESTAURANT') {
        return (
            <div className="container mx-auto px-4 py-8">
                <div className="text-center">
                    <h1 className="text-2xl font-bold text-red-600">Access Denied</h1>
                    <p className="text-gray-600 mt-2">You need restaurant privileges to access this page.</p>
                </div>
            </div>
        );
    }

    if (loading) {
        return (
            <div className="container mx-auto px-4 py-8">
                <div className="text-center">
                    <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-blue-600 mx-auto"></div>
                    <p className="mt-4 text-gray-600">Loading dashboard...</p>
                </div>
            </div>
        );
    }

    const currentOrderStats = orderStats || dashboardData?.orderStats;
    const currentRevenueStats = revenueStats || dashboardData?.revenueStats;
    const currentTopProducts = topProducts || dashboardData?.topProducts;

    const getTimeScale = () => {
        const start = new Date(dateRange.startDate);
        const end = new Date(dateRange.endDate);
        const diffDays = Math.ceil((end - start) / (1000 * 60 * 60 * 24));
        return diffDays <= 1 ? 'hours' : 'days';
    };

    const ordersChartData = currentOrderStats?.dailyOrders ?
        Object.entries(currentOrderStats.dailyOrders)
            .map(([date, orders]) => ({ date, orders }))
            .sort((a, b) => new Date(a.date) - new Date(b.date)) : [];

    const revenueChartData = currentRevenueStats?.dailyRevenueChart ?
        Object.entries(currentRevenueStats.dailyRevenueChart)
            .map(([date, revenue]) => ({ date, revenue: parseFloat(revenue) || 0 }))
            .filter(item => !isNaN(item.revenue))
            .sort((a, b) => new Date(a.date) - new Date(b.date)) : [];

    const orderStatusData = currentOrderStats?.ordersByStatus ?
        Object.entries(currentOrderStats.ordersByStatus).map(([status, count]) => ({
            label: status.replace('_', ' '),
            value: count
        })) : [];

    const formatCurrency = (amount) => {
        return new Intl.NumberFormat('en-US', {
            style: 'currency',
            currency: 'USD'
        }).format(amount || 0);
    };

    return (
        <div className="min-h-screen bg-gray-50">
            <DashboardHeader
                title="Restaurant Dashboard"
                dateRange={dateRange}
                onDateChange={handleDateRangeChange}
                userRole={user?.role?.authority || user?.role}
                userId={user?.id}
                onRefresh={fetchDashboardData}
                isLoading={loading}
            />
            
            <div id="dashboard-content" className="container mx-auto px-6 py-8">

            {/* Key Metrics */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
                <StatCard
                    title="Total Orders"
                    value={currentOrderStats?.totalOrders || 0}
                    subtitle="In selected period"
                    color="blue"
                    icon={
                        <svg className="w-6 h-6" fill="currentColor" viewBox="0 0 20 20">
                            <path fillRule="evenodd" d="M3 4a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1zm0 4a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1zm0 4a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1z" clipRule="evenodd" />
                        </svg>
                    }
                />
                <StatCard
                    title="Monthly Revenue"
                    value={formatCurrency(currentRevenueStats?.monthlyRevenue)}
                    subtitle="Last 30 days"
                    color="green"
                    icon={
                        <svg className="w-6 h-6" fill="currentColor" viewBox="0 0 20 20">
                            <path d="M8.433 7.418c.155-.103.346-.196.567-.267v1.698a2.305 2.305 0 01-.567-.267C8.07 8.34 8 8.114 8 8c0-.114.07-.34.433-.582zM11 12.849v-1.698c.22.071.412.164.567.267.364.243.433.468.433.582 0 .114-.07.34-.433.582a2.305 2.305 0 01-.567.267z" />
                            <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm1-13a1 1 0 10-2 0v.092a4.535 4.535 0 00-1.676.662C6.602 6.234 6 7.009 6 8c0 .99.602 1.765 1.324 2.246.48.32 1.054.545 1.676.662v1.941c-.391-.127-.68-.317-.843-.504a1 1 0 10-1.51 1.31c.562.649 1.413 1.076 2.353 1.253V15a1 1 0 102 0v-.092a4.535 4.535 0 001.676-.662C13.398 13.766 14 12.991 14 12c0-.99-.602-1.765-1.324-2.246A4.535 4.535 0 0011 9.092V7.151c.391.127.68.317.843.504a1 1 0 101.511-1.31c-.563-.649-1.413-1.076-2.354-1.253V5z" clipRule="evenodd" />
                        </svg>
                    }
                />
                <StatCard
                    title="Cancellation Rate"
                    value={`${(currentOrderStats?.cancellationRate || 0).toFixed(1)}%`}
                    subtitle="Of total orders"
                    color={currentOrderStats?.cancellationRate > 10 ? 'red' : 'green'}
                />
                <StatCard
                    title="Avg Prep Time"
                    value={`${(currentOrderStats?.averagePreparationTime || 0).toFixed(0)} min`}
                    subtitle="Per order"
                    color="purple"
                />
            </div>

            {/* Revenue Overview */}
            <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-8">
                <div className="bg-white p-4 rounded-lg shadow text-center">
                    <p className="text-sm text-gray-600">Daily Revenue</p>
                    <p className="text-xl font-bold text-green-600">
                        {formatCurrency(currentRevenueStats?.dailyRevenue)}
                    </p>
                </div>
                <div className="bg-white p-4 rounded-lg shadow text-center">
                    <p className="text-sm text-gray-600">Weekly Revenue</p>
                    <p className="text-xl font-bold text-green-600">
                        {formatCurrency(currentRevenueStats?.weeklyRevenue)}
                    </p>
                </div>
                <div className="bg-white p-4 rounded-lg shadow text-center">
                    <p className="text-sm text-gray-600">Monthly Revenue</p>
                    <p className="text-xl font-bold text-green-600">
                        {formatCurrency(currentRevenueStats?.monthlyRevenue)}
                    </p>
                </div>
                <div className="bg-white p-4 rounded-lg shadow text-center">
                    <p className="text-sm text-gray-600">Yearly Revenue</p>
                    <p className="text-xl font-bold text-green-600">
                        {formatCurrency(currentRevenueStats?.yearlyRevenue)}
                    </p>
                </div>
            </div>

            {/* Charts Row 1 */}
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
                <LineChart
                    data={ordersChartData}
                    title={getTimeScale() === 'hours' ? 'Hourly Orders' : 'Daily Orders'}
                    xKey="date"
                    yKey="orders"
                    color="#3B82F6"
                    timeScale={getTimeScale()}
                />
                <LineChart
                    data={revenueChartData}
                    title={getTimeScale() === 'hours' ? 'Hourly Revenue' : 'Daily Revenue'}
                    xKey="date"
                    yKey="revenue"
                    color="#10B981"
                    timeScale={getTimeScale()}
                />
            </div>

            {/* Charts Row 2 */}
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
                <PieChart
                    data={orderStatusData}
                    title="Orders by Status"
                />
                
                {/* Top Products */}
                <div className="bg-white p-6 rounded-lg shadow">
                    <h3 className="text-lg font-semibold mb-4">Top Products</h3>
                    <div className="space-y-3">
                        {currentTopProducts?.slice(0, 8).map((product, index) => (
                            <div key={index} className="flex justify-between items-center">
                                <span className="text-sm text-gray-700">{product.productName}</span>
                                <span className="text-sm font-semibold text-gray-900">{product.orderCount} orders</span>
                            </div>
                        )) || <div className="text-gray-500 text-center py-4">No data available</div>}
                    </div>
                </div>
            </div>

            {/* Quick Actions */}
            <div className="mb-8">
                <QuickActions 
                    userRole={user?.role?.authority || user?.role}
                    onAction={(actionId) => {
                        switch(actionId) {
                            case 'menu-performance':
                                window.location.href = '/restaurant/menu-analytics';
                                break;
                            case 'customer-insights':
                                window.location.href = '/restaurant/customer-analytics';
                                break;
                        }
                    }}
                />
            </div>


        </div>
        </div>
    );
};

export default RestaurantDashboard;