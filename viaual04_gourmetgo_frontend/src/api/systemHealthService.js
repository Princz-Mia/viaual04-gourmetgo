import axiosInstance from './axiosConfig';

export const getSystemHealth = async () => {
    const response = await axiosInstance.get('/system/health');
    return response.data.data;
};

export const getSystemLogs = async (limit = 50) => {
    const response = await axiosInstance.get(`/system/logs?limit=${limit}`);
    return response.data.data;
};

export const getLogFiles = async () => {
    const response = await axiosInstance.get('/system/logs/files');
    return response.data.data;
};

export const getLogFileContent = async (filename) => {
    const response = await axiosInstance.get(`/system/logs/file/${filename}`);
    return response.data.data;
};

export const getUserAnalytics = async () => {
    const response = await axiosInstance.get('/system/user-analytics');
    return response.data.data;
};

export const getBusinessInsights = async () => {
    const response = await axiosInstance.get('/system/business-insights');
    return response.data.data;
};