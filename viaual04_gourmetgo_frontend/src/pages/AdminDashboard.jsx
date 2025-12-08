import React, { useState, useEffect, useCallback } from 'react';
import { getAdminDashboard, getOnlineUsersCount } from '../api/statisticsService';
import { generateAdminPdfReport } from '../api/reportService';
import LineChart from '../components/charts/LineChart';
import PieChart from '../components/charts/PieChart';
import StatCard from '../components/charts/StatCard';
import DashboardHeader from '../components/DashboardHeader';
import QuickActions from '../components/QuickActions';
import { useAuth } from '../contexts/AuthContext';
import { toast } from 'react-toastify';

const AdminDashboard = () => {
    const { user } = useAuth();
    const [dashboardData, setDashboardData] = useState(null);
    const [onlineUsers, setOnlineUsers] = useState(0);
    const [performanceStats, setPerformanceStats] = useState(null);
    const [orderStats, setOrderStats] = useState(null);
    const [trafficStats, setTrafficStats] = useState(null);
    const [topProducts, setTopProducts] = useState(null);
    const [loading, setLoading] = useState(true);
    const [dateRange, setDateRange] = useState({
        startDate: new Date(Date.now() - 7 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
        endDate: new Date().toISOString().split('T')[0]
    });

    useEffect(() => {
        const userRole = user?.role?.authority || user?.role;
        if (userRole !== 'ROLE_ADMIN') {
            toast.error('Access denied. Admin role required.');
            return;
        }
        fetchDashboardData();
        fetchOnlineUsers();
        
        // Set up WebSocket for real-time updates
        import('../services/websocketService').then(({ default: websocketService }) => {
            websocketService.connect().then(() => {
                websocketService.subscribe('/topic/admin/statistics', (message) => {
                    if (message.type === 'VISIT_UPDATE') {
                        setOnlineUsers(message.data);
                    } else if (message.type === 'PERFORMANCE_UPDATE') {
                        setPerformanceStats(message.data);
                    } else if (message.type === 'ORDER_UPDATE') {
                        setOrderStats(message.data);
                    } else if (message.type === 'TRAFFIC_UPDATE') {
                        setTrafficStats(message.data);
                    } else if (message.type === 'PRODUCTS_UPDATE') {
                        setTopProducts(message.data);
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
            const data = await getAdminDashboard(dateRange.startDate, dateRange.endDate);
            setDashboardData(data);
            // Reset WebSocket state to use fresh data
            setPerformanceStats(null);
            setOrderStats(null);
            setTrafficStats(null);
            setTopProducts(null);
        } catch (error) {
            console.error('Error fetching dashboard data:', error);
            toast.error('Failed to load dashboard data');
        } finally {
            setLoading(false);
        }
    }, [dateRange.startDate, dateRange.endDate]);

    const fetchOnlineUsers = async () => {
        try {
            const count = await getOnlineUsersCount();
            setOnlineUsers(count);
        } catch (error) {
            console.error('Error fetching online users:', error);
        }
    };

    const handleDateRangeChange = (field, value) => {
        setDateRange(prev => ({
            ...prev,
            [field]: value
        }));
    };

    const handleGeneratePdf = async (type = 'server') => {
        try {
            await generateAdminPdfReport(dateRange.startDate, dateRange.endDate);
            toast.success('PDF report generated successfully!');
        } catch (error) {
            toast.error('Failed to generate PDF report');
        }
    };

    const userRole = user?.role?.authority || user?.role;
    if (userRole !== 'ROLE_ADMIN') {
        return (
            <div className="container mx-auto px-4 py-8">
                <div className="text-center">
                    <h1 className="text-2xl font-bold text-red-600">Access Denied</h1>
                    <p className="text-gray-600 mt-2">You need admin privileges to access this page.</p>
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

    const currentTrafficStats = trafficStats || dashboardData?.trafficStats;
    const currentOrderStats = orderStats || dashboardData?.orderStats;

    const getTimeScale = () => {
        const start = new Date(dateRange.startDate);
        const end = new Date(dateRange.endDate);
        const diffDays = Math.ceil((end - start) / (1000 * 60 * 60 * 24));
        return diffDays <= 1 ? 'hours' : 'days';
    };

    const trafficChartData = currentTrafficStats?.dailyVisits ?
        Object.entries(currentTrafficStats.dailyVisits)
            .map(([date, visits]) => ({ date, visits }))
            .sort((a, b) => new Date(a.date) - new Date(b.date)) : [];

    const ordersChartData = currentOrderStats?.dailyOrders ?
        Object.entries(currentOrderStats.dailyOrders)
            .map(([date, orders]) => ({ date, orders }))
            .sort((a, b) => new Date(a.date) - new Date(b.date)) : [];

    const orderStatusData = currentOrderStats?.ordersByStatus ?
        Object.entries(currentOrderStats.ordersByStatus).map(([status, count]) => ({
            label: status.replace('_', ' '),
            value: count
        })) : [];

    return (
        <div className="min-h-screen bg-gray-50">
            <DashboardHeader
                title="Admin Dashboard"
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
                    title="Online Users"
                    value={onlineUsers}
                    subtitle="Currently active"
                    color="green"
                    icon={
                        <svg className="w-6 h-6" fill="currentColor" viewBox="0 0 20 20">
                            <path d="M13 6a3 3 0 11-6 0 3 3 0 016 0zM18 8a2 2 0 11-4 0 2 2 0 014 0zM14 15a4 4 0 00-8 0v3h8v-3zM6 8a2 2 0 11-4 0 2 2 0 014 0zM16 18v-3a5.972 5.972 0 00-.75-2.906A3.005 3.005 0 0119 15v3h-3zM4.75 12.094A5.973 5.973 0 004 15v3H1v-3a3 3 0 013.75-2.906z" />
                        </svg>
                    }
                />
                <StatCard
                    title="Total Orders"
                    value={(orderStats || dashboardData?.orderStats)?.totalOrders || 0}
                    subtitle="In selected period"
                    color="blue"
                />
                <StatCard
                    title="Weekly Visits"
                    value={(trafficStats || dashboardData?.trafficStats)?.weeklyVisits || 0}
                    subtitle="Last 7 days"
                    color="purple"
                />
                <StatCard
                    title="Server Error Rate"
                    value={`${((performanceStats || dashboardData?.performanceStats)?.errorRate || 0).toFixed(1)}%`}
                    subtitle="Server errors only"
                    color={((performanceStats || dashboardData?.performanceStats)?.errorRate || 0) > 5 ? 'red' : 'green'}
                />
            </div>

            {/* Charts Row 1 */}
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
                <LineChart
                    data={trafficChartData}
                    title={getTimeScale() === 'hours' ? 'Hourly Visits' : 'Daily Visits'}
                    xKey="date"
                    yKey="visits"
                    color="#3B82F6"
                    timeScale={getTimeScale()}
                />
                <LineChart
                    data={ordersChartData}
                    title={getTimeScale() === 'hours' ? 'Hourly Orders' : 'Daily Orders'}
                    xKey="date"
                    yKey="orders"
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
                        {(topProducts || dashboardData?.topProducts)?.slice(0, 5).map((product, index) => (
                            <div key={index} className="flex justify-between items-center">
                                <span className="text-sm text-gray-700">{product.productName}</span>
                                <span className="text-sm font-semibold text-gray-900">{product.orderCount} orders</span>
                            </div>
                        )) || <div className="text-gray-500 text-center py-4">No data available</div>}
                    </div>
                </div>
            </div>

            {/* Happy Hour Stats */}
            {dashboardData?.happyHourStats && (
                <div className="bg-white p-6 rounded-lg shadow mb-8">
                    <h3 className="text-lg font-semibold mb-4">Happy Hour Performance</h3>
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                        <div className="text-center">
                            <p className="text-sm text-gray-600">Orders in Happy Hour</p>
                            <p className="text-2xl font-bold text-orange-600">
                                {dashboardData.happyHourStats.ordersInHappyHour}
                            </p>
                        </div>
                        <div className="text-center">
                            <p className="text-sm text-gray-600">Orders Outside Happy Hour</p>
                            <p className="text-2xl font-bold text-gray-600">
                                {dashboardData.happyHourStats.ordersOutsideHappyHour}
                            </p>
                        </div>
                        <div className="text-center">
                            <p className="text-sm text-gray-600">Happy Hour Increase</p>
                            <p className="text-2xl font-bold text-green-600">
                                +{dashboardData.happyHourStats.happyHourIncrease?.toFixed(1) || 0}%
                            </p>
                        </div>
                    </div>
                </div>
            )}

            {/* Quick Actions */}
            <div className="mb-8">
                <QuickActions 
                    userRole={user?.role?.authority || user?.role}
                    onAction={(actionId) => {
                        switch(actionId) {
                            case 'system-health':
                                window.location.href = '/admin/system-health';
                                break;
                            case 'user-analytics':
                                window.location.href = '/admin/user-analytics';
                                break;
                            case 'business-insights':
                                window.location.href = '/admin/business-insights';
                                break;
                        }
                    }}
                />
            </div>

            {/* Performance Stats */}
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                <div className="bg-white p-6 rounded-lg shadow">
                    <h3 className="text-lg font-semibold mb-4">Performance Metrics</h3>
                    <div className="space-y-4">
                        <div className="flex justify-between">
                            <span className="text-gray-600">Requests/Hour:</span>
                            <span className="font-semibold">{(performanceStats || dashboardData?.performanceStats)?.requestsPerHour || 0}</span>
                        </div>
                        <div className="flex justify-between">
                            <span className="text-gray-600">Avg Response Time:</span>
                            <span className="font-semibold">{((performanceStats || dashboardData?.performanceStats)?.averageResponseTime || 0).toFixed(0)}ms</span>
                        </div>
                        <div className="flex justify-between">
                            <span className="text-gray-600">Error Rate:</span>
                            <span className="font-semibold">{((performanceStats || dashboardData?.performanceStats)?.errorRate || 0).toFixed(1)}%</span>
                        </div>
                    </div>
                </div>

                <div className="bg-white p-6 rounded-lg shadow">
                    <h3 className="text-lg font-semibold mb-4">Top Endpoints</h3>
                    <div className="space-y-3">
                        {(performanceStats || dashboardData?.performanceStats)?.topEndpoints?.slice(0, 5).map((endpoint, index) => (
                            <div key={index} className="flex justify-between items-center">
                                <span className="text-sm text-gray-700 truncate">{endpoint.endpoint}</span>
                                <span className="text-sm font-semibold text-gray-900">{endpoint.requestCount}</span>
                            </div>
                        )) || <div className="text-gray-500 text-center py-4">No data available</div>}
                    </div>
                </div>
            </div>
        </div>
        </div>
    );
};

export default AdminDashboard;