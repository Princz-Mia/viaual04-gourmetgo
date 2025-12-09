import React, { useState, useEffect } from 'react';

const RealTimeStats = () => {
    const [connectionStatus] = useState('connected');
    const [lastUpdate, setLastUpdate] = useState(new Date());

    useEffect(() => {
        const interval = setInterval(() => {
            setLastUpdate(new Date());
        }, 30000);
        return () => clearInterval(interval);
    }, []);

    const getStatusColor = () => {
        switch (connectionStatus) {
            case 'connected': return 'text-green-500';
            case 'connecting': return 'text-yellow-500';
            case 'error': return 'text-red-500';
            default: return 'text-gray-500';
        }
    };

    const getStatusIcon = () => {
        switch (connectionStatus) {
            case 'connected':
                return (
                    <div className="flex items-center space-x-1">
                        <div className="w-2 h-2 bg-green-500 rounded-full animate-pulse"></div>
                        <span className="text-xs text-green-600">Live</span>
                    </div>
                );
            case 'connecting':
                return (
                    <div className="flex items-center space-x-1">
                        <div className="w-2 h-2 bg-yellow-500 rounded-full animate-spin"></div>
                        <span className="text-xs text-yellow-600">Connecting</span>
                    </div>
                );
            case 'error':
                return (
                    <div className="flex items-center space-x-1">
                        <div className="w-2 h-2 bg-red-500 rounded-full"></div>
                        <span className="text-xs text-red-600">Offline</span>
                    </div>
                );
            default:
                return (
                    <div className="flex items-center space-x-1">
                        <div className="w-2 h-2 bg-gray-500 rounded-full"></div>
                        <span className="text-xs text-gray-600">Disconnected</span>
                    </div>
                );
        }
    };

    return (
        <div className="flex items-center space-x-4 text-sm">
            {getStatusIcon()}
            {lastUpdate && (
                <span className="text-xs text-gray-500">
                    Last update: {lastUpdate.toLocaleTimeString()}
                </span>
            )}
        </div>
    );
};

export default RealTimeStats;