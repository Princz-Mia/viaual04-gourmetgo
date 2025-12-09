import axiosInstance from './axiosConfig';

// Use the main axios instance which already handles cookies and token refresh

export const startConversation = async (customerId, subject) => {
    const response = await axiosInstance.post('/chat/conversations', {
        customerId,
        subject
    });
    return response.data;
};

export const getCustomerConversations = async (customerId) => {
    const response = await axiosInstance.get(`/chat/conversations/customer/${customerId}`);
    return response.data;
};

export const getAdminConversations = async (adminId) => {
    const response = await axiosInstance.get(`/chat/conversations/admin/${adminId}`);
    return response.data;
};

export const getOpenConversations = async () => {
    const response = await axiosInstance.get('/chat/conversations/open');
    return response.data;
};

export const assignConversation = async (conversationId, adminId) => {
    const response = await axiosInstance.put(`/chat/conversations/${conversationId}/assign`, {
        adminId
    });
    return response.data;
};

export const closeConversation = async (conversationId) => {
    const response = await axiosInstance.put(`/chat/conversations/${conversationId}/close`);
    return response.data;
};

export const getConversation = async (conversationId) => {
    const response = await axiosInstance.get(`/chat/conversations/${conversationId}`);
    return response.data;
};

export const markMessagesAsRead = async (conversationId, userId) => {
    const response = await axiosInstance.put(`/chat/conversations/${conversationId}/read`, {
        userId
    });
    return response.data;
};

export const solveConversation = async (conversationId) => {
    const response = await axiosInstance.put(`/chat/conversations/${conversationId}/solve`);
    return response.data;
};

export const getSolvedConversations = async () => {
    const response = await axiosInstance.get('/chat/conversations/solved');
    return response.data;
};

export const getInProgressConversations = async () => {
    const response = await axiosInstance.get('/chat/conversations/in-progress');
    return response.data;
};

export const getWaitingConversations = async () => {
    const response = await axiosInstance.get('/chat/conversations/waiting');
    return response.data;
};