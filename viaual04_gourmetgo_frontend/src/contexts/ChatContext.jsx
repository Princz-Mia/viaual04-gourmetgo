import React, { createContext, useContext, useState, useEffect } from 'react';
import { useAuth } from './AuthContext';
import websocketService from '../services/websocketService';
import { getOpenConversations } from '../api/chatService';

const ChatContext = createContext();

export const ChatProvider = ({ children }) => {
    const { user } = useAuth();
    const [isConnected, setIsConnected] = useState(false);
    const [notifications, setNotifications] = useState([]);
    const [unreadCount, setUnreadCount] = useState(0);
    const [customerUnreadCount, setCustomerUnreadCount] = useState(0);

    useEffect(() => {
        if (user) {
            connectWebSocket();
        } else {
            disconnectWebSocket();
        }

        return () => disconnectWebSocket();
    }, [user]);

    const connectWebSocket = async () => {
        try {
            await websocketService.connect();
            setIsConnected(true);

            if (user?.role.authority === 'ROLE_CUSTOMER') {
                websocketService.subscribeToUserNotifications(handleCustomerNotification);
            } else if (user?.role.authority === 'ROLE_ADMIN') {
                websocketService.subscribeToAdminNotifications(handleAdminNotification);
                loadWaitingCount();
            }
        } catch (error) {
            console.error('WebSocket connection failed:', error);
            setIsConnected(false);
        }
    };

    const disconnectWebSocket = () => {
        websocketService.disconnect();
        setIsConnected(false);
        setNotifications([]);
        setUnreadCount(0);
    };

    const handleCustomerNotification = (messageData) => {
        setCustomerUnreadCount(prev => prev + 1);
    };

    const handleAdminNotification = (messageData) => {
        loadWaitingCount();
    };

    const loadWaitingCount = async () => {
        if (user?.role.authority === 'ROLE_ADMIN') {
            try {
                const response = await getOpenConversations();
                if (response.success) {
                    setUnreadCount(response.data.length);
                }
            } catch (error) {
                console.error('Failed to load waiting count:', error);
            }
        }
    };

    const clearNotifications = () => {
        setNotifications([]);
        setUnreadCount(0);
        setCustomerUnreadCount(0);
    };

    const markAsRead = () => {
        if (user?.role.authority === 'ROLE_ADMIN') {
            loadWaitingCount();
        } else {
            setCustomerUnreadCount(0);
        }
    };

    return (
        <ChatContext.Provider value={{
            isConnected,
            notifications,
            unreadCount: user?.role.authority === 'ROLE_ADMIN' ? unreadCount : customerUnreadCount,
            clearNotifications,
            markAsRead,
            connectWebSocket,
            disconnectWebSocket
        }}>
            {children}
        </ChatContext.Provider>
    );
};

export const useChat = () => {
    const context = useContext(ChatContext);
    if (!context) {
        throw new Error('useChat must be used within a ChatProvider');
    }
    return context;
};