import React, { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { getSystemHealth, getSystemLogs, getLogFiles, getLogFileContent } from '../api/systemHealthService';
import { toast } from 'react-toastify';
import { useNavigate } from 'react-router-dom';

const SystemHealth = () => {
    const { user } = useAuth();
    const navigate = useNavigate();
    const [logs, setLogs] = useState([]);
    const [metrics, setMetrics] = useState({});
    const [logFiles, setLogFiles] = useState([]);
    const [selectedLogFile, setSelectedLogFile] = useState('');
    const [logContent, setLogContent] = useState('');
    const [showLogViewer, setShowLogViewer] = useState(false);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const userRole = user?.role?.authority || user?.role;
        if (userRole !== 'ROLE_ADMIN') {
            toast.error('Access denied. Admin role required.');
            return;
        }
        fetchSystemHealth();
    }, [user]);

    const fetchSystemHealth = async () => {
        try {
            setLoading(true);
            const [healthData, logsData, logFilesData] = await Promise.all([
                getSystemHealth(),
                getSystemLogs(20),
                getLogFiles()
            ]);
            setMetrics(healthData);
            setLogs(logsData);
            setLogFiles(logFilesData);
        } catch (error) {
            toast.error('Failed to fetch system health data');
        } finally {
            setLoading(false);
        }
    };
    
    const handleViewLogFile = async (filename) => {
        try {
            setSelectedLogFile(filename);
            const content = await getLogFileContent(filename);
            setLogContent(content);
            setShowLogViewer(true);
        } catch (error) {
            toast.error('Failed to load log file');
        }
    };

    useEffect(() => {
        const interval = setInterval(fetchSystemHealth, 30000);
        return () => clearInterval(interval);
    }, []);

    if (loading) {
        return (
            <div className="min-h-screen bg-gray-50 flex items-center justify-center">
                <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-blue-600"></div>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-gray-50 p-6">
            <div className="max-w-7xl mx-auto">
                <div className="flex items-center justify-between mb-8">
                    <div className="flex items-center">
                        <button
                            onClick={() => navigate('/admin/dashboard')}
                            className="mr-4 p-2 text-gray-600 hover:text-gray-900 hover:bg-gray-100 rounded-lg transition-colors cursor-pointer"
                        >
                            <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
                            </svg>
                        </button>
                        <h1 className="text-3xl font-bold text-gray-900">System Health Monitor</h1>
                    </div>
                    <div className="flex items-center space-x-4">
                        <span className="text-sm text-gray-500">Updates every 30 seconds</span>
                        <button
                            onClick={fetchSystemHealth}
                            disabled={loading}
                            className="btn btn-primary btn-sm"
                        >
                            {loading ? (
                                <svg className="animate-spin w-4 h-4" fill="none" viewBox="0 0 24 24">
                                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                                    <path className="opacity-75" fill="currentColor" d="m4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                                </svg>
                            ) : (
                                <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
                                </svg>
                            )}
                            Refresh
                        </button>
                    </div>
                </div>
                
                {/* Metrics Grid */}
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-8">
                    {Object.entries(metrics).map(([key, value]) => (
                        <div key={key} className="bg-white p-6 rounded-lg shadow">
                            <h3 className="text-sm font-medium text-gray-500 uppercase tracking-wide">
                                {key.replace(/([A-Z])/g, ' $1').trim()}
                            </h3>
                            <p className="mt-2 text-2xl font-bold text-gray-900">{value}</p>
                        </div>
                    ))}
                </div>

                {/* Log Files Selector - Compact */}
                <div className="bg-white rounded-lg shadow mb-6">
                    <div className="px-6 py-3 border-b border-gray-200 flex items-center justify-between">
                        <h2 className="text-lg font-semibold text-gray-900">Log Files</h2>
                        <select 
                            onChange={(e) => e.target.value && handleViewLogFile(e.target.value)}
                            className="px-3 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                            defaultValue=""
                        >
                            <option value="">Select a log file to view</option>
                            {logFiles.map((file, index) => (
                                <option key={index} value={file}>{file}</option>
                            ))}
                        </select>
                    </div>
                    <div className="px-6 py-2">
                        <div className="flex flex-wrap gap-2">
                            {logFiles.slice(0, 8).map((file, index) => (
                                <button
                                    key={index}
                                    onClick={() => handleViewLogFile(file)}
                                    className="px-3 py-1 bg-blue-100 hover:bg-blue-200 text-blue-800 text-xs rounded-full transition-colors"
                                >
                                    {file}
                                </button>
                            ))}
                            {logFiles.length > 8 && (
                                <span className="px-3 py-1 bg-gray-100 text-gray-600 text-xs rounded-full">
                                    +{logFiles.length - 8} more
                                </span>
                            )}
                        </div>
                    </div>
                </div>

                {/* Recent Logs - Full Width */}
                <div className="bg-white rounded-lg shadow">
                    <div className="px-6 py-4 border-b border-gray-200 flex items-center justify-between">
                        <h2 className="text-lg font-semibold text-gray-900">Recent System Logs</h2>
                        <div className="flex items-center space-x-2">
                            <div className="w-2 h-2 bg-green-500 rounded-full animate-pulse"></div>
                            <span className="text-sm text-gray-500">Live</span>
                        </div>
                    </div>
                    <div className="p-6">
                        <div className="space-y-2">
                            {logs.map((log, index) => (
                                <div key={index} className="flex items-start space-x-4 p-4 bg-gray-50 hover:bg-gray-100 rounded-lg transition-colors">
                                    <span className={`px-3 py-1 text-xs font-bold rounded-full flex-shrink-0 ${
                                        log.level === 'ERROR' ? 'bg-red-100 text-red-800 border border-red-200' :
                                        log.level === 'WARN' ? 'bg-yellow-100 text-yellow-800 border border-yellow-200' :
                                        'bg-green-100 text-green-800 border border-green-200'
                                    }`}>
                                        {log.level}
                                    </span>
                                    <span className="text-sm text-gray-500 font-mono flex-shrink-0 w-40">{log.timestamp}</span>
                                    <span className="text-sm text-gray-900 flex-1">{log.message}</span>
                                </div>
                            ))}
                        </div>
                    </div>
                </div>
                
                {/* Log Viewer Modal */}
                {showLogViewer && (
                    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
                        <div className="bg-white rounded-lg shadow-xl w-full max-w-6xl h-5/6 flex flex-col">
                            <div className="flex items-center justify-between p-6 border-b">
                                <h3 className="text-lg font-semibold text-gray-900">{selectedLogFile}</h3>
                                <button
                                    onClick={() => setShowLogViewer(false)}
                                    className="text-gray-400 hover:text-gray-600"
                                >
                                    <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                                    </svg>
                                </button>
                            </div>
                            <div className="flex-1 p-6 overflow-auto">
                                <pre className="text-sm text-gray-800 whitespace-pre-wrap font-mono bg-gray-50 p-4 rounded">
                                    {logContent}
                                </pre>
                            </div>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};

export default SystemHealth;