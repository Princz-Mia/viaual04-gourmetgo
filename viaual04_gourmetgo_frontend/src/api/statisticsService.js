import axiosInstance from './axiosConfig';

export const getAdminDashboard = async (startDate, endDate) => {
    const params = {};
    if (startDate) params.startDate = startDate;
    if (endDate) params.endDate = endDate;
    
    const response = await axiosInstance.get('/statistics/admin/dashboard', { params });
    return response.data.data;
};

export const getRestaurantDashboard = async (restaurantId, startDate, endDate) => {
    const params = {};
    if (startDate) params.startDate = startDate;
    if (endDate) params.endDate = endDate;
    
    const response = await axiosInstance.get(`/statistics/restaurant/${restaurantId}/dashboard`, { params });
    return response.data.data;
};

export const getOnlineUsersCount = async () => {
    const response = await axiosInstance.get('/statistics/online-users');
    return response.data.data;
};