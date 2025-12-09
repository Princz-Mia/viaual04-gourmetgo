import React, { useState, useEffect, useRef } from 'react';
import { FaUser, FaClock, FaCheck, FaTimes, FaPaperPlane, FaComments } from 'react-icons/fa';
import { useAuth } from '../../contexts/AuthContext';
import { useChat } from '../../contexts/ChatContext';
import { getOpenConversations, getInProgressConversations, assignConversation, closeConversation, solveConversation, getSolvedConversations, getConversation } from '../../api/chatService';
import websocketService from '../../services/websocketService';
import { toast } from 'react-toastify';

const AdminChatPanel = () => {
    const { user } = useAuth();
    const { notifications, markAsRead } = useChat();
    const [openConversations, setOpenConversations] = useState([]);
    const [waitingConversations, setWaitingConversations] = useState([]);
    const [inProgressConversations, setInProgressConversations] = useState([]);
    const [solvedConversations, setSolvedConversations] = useState([]);
    const [activeConversation, setActiveConversation] = useState(null);
    const [message, setMessage] = useState('');
    const [activeTab, setActiveTab] = useState('requests');
    const [isTyping, setIsTyping] = useState(false);
    const [adminIsTyping, setAdminIsTyping] = useState(false);
    const messagesEndRef = useRef(null);
    const typingTimeoutRef = useRef(null);

    useEffect(() => {
        const userRole = user?.role?.authority || user?.role;
        if (userRole === 'ROLE_ADMIN') {
            loadOpenConversations();
            loadWaitingConversations();
            loadInProgressConversations();
            loadSolvedConversations();
            markAsRead();
            
            // Use dashboard-style WebSocket connection
            import('../../services/websocketService').then(({ default: websocketService }) => {
                websocketService.connect().then(() => {
                    console.log('WebSocket connected for admin chat');
                    websocketService.subscribe('/topic/admin/chat', handleAdminNotification);
                    websocketService.subscribe('/topic/admin/notifications', handleAdminNotification);
                }).catch(console.error);
            });
            
            return () => {
                // Cleanup handled by websocket service
            };
        }
    }, [user]);

    useEffect(() => {
        if (activeConversation) {
            websocketService.subscribeToConversation(activeConversation.id, handleNewMessage);
            websocketService.subscribeToTyping(activeConversation.id, handleTypingIndicator);
        }
        return () => {
            if (activeConversation) {
                websocketService.unsubscribe(`conversation-${activeConversation.id}`);
                websocketService.unsubscribe(`typing-${activeConversation.id}`);
            }
        };
    }, [activeConversation]);



    const loadOpenConversations = async () => {
        try {
            const response = await getOpenConversations();
            if (response.success) {
                setOpenConversations(response.data);
            }
        } catch (error) {
            console.error('Failed to load conversations:', error);
        }
    };

    const loadWaitingConversations = async () => {
        // Skip loading - will be populated when admin leaves
    };

    const loadInProgressConversations = async () => {
        try {
            const response = await getInProgressConversations();
            if (response.success) {
                setInProgressConversations(response.data);
            }
        } catch (error) {
            console.error('Failed to load in progress conversations:', error);
        }
    };

    const loadSolvedConversations = async () => {
        try {
            const response = await getSolvedConversations();
            if (response.success) {
                setSolvedConversations(response.data);
            }
        } catch (error) {
            console.error('Failed to load solved conversations:', error);
        }
    };

    const handleNewMessage = (messageData) => {
        setActiveConversation(prev => {
            const updated = {
                ...prev,
                messages: [...(prev.messages || []), messageData]
            };
            
            // Mark customer messages as read immediately
            if (messageData.senderType === 'CUSTOMER') {
                markMessageAsRead(messageData.id);
                messageData.isRead = true;
            }
            
            return updated;
        });
    };

    const handleTypingIndicator = (typingData) => {
        if (typingData.senderType === 'CUSTOMER') {
            setIsTyping(typingData.isTyping ? typingData.senderName : false);
        }
    };

    const markMessageAsRead = async (messageId) => {
        // Skip marking as read due to auth issues
    };

    const handleAdminNotification = (message) => {
        console.log('Admin notification received:', message);
        
        if (message.type === 'NEW_CONVERSATION') {
            // Handle both old and new format
            const conversation = message.data || message.conversation;
            const customerName = message.customerName || conversation?.customerName || 'Customer';
            
            if (conversation) {
                setOpenConversations(prev => {
                    const exists = prev.find(conv => conv.id === conversation.id);
                    if (!exists) {
                        return [conversation, ...prev];
                    }
                    return prev;
                });
                toast.info(`New chat request from ${customerName}`);
            }
        } else if (message.type === 'STATUS_CHANGE') {
            // Handle both old and new format
            const data = message.data || message;
            const conversationId = data.conversationId;
            const status = data.status;
            
            if (conversationId && status === 'WAITING') {
                setInProgressConversations(prev => prev.filter(conv => conv.id !== conversationId));
                setWaitingConversations(prev => {
                    const exists = prev.find(conv => conv.id === conversationId);
                    if (!exists) {
                        const foundConv = [...openConversations, ...inProgressConversations].find(c => c.id === conversationId);
                        if (foundConv) {
                            return [...prev, {...foundConv, status: 'WAITING', adminId: null, adminName: null}];
                        }
                    }
                    return prev;
                });
            } else if (conversationId && status === 'IN_PROGRESS') {
                setWaitingConversations(prev => prev.filter(conv => conv.id !== conversationId));
                setOpenConversations(prev => prev.filter(conv => conv.id !== conversationId));
                loadInProgressConversations();
            }
        } else if (message.senderType === 'CUSTOMER' && message.conversationId) {
            toast.info(`New message from ${message.senderName || 'Customer'}`);
        }
        
        markAsRead();
    };

    const handleAcceptConversation = async (conversationId) => {
        try {
            const response = await assignConversation(conversationId, user.id);
            if (response.success) {
                const fullConversation = await getConversation(conversationId);
                setActiveConversation(fullConversation.data);
                setActiveTab('progress');
                // Remove from waiting list and add to in-progress
                setWaitingConversations(prev => prev.filter(conv => conv.id !== conversationId));
                setOpenConversations(prev => prev.filter(conv => conv.id !== conversationId));
                loadInProgressConversations();
                
                // Send greeting message after admin joins
                setTimeout(() => {
                    const greetingMessage = fullConversation.data.messages && fullConversation.data.messages.length > 1 
                        ? `Hello! I'm ${user.fullName} and I'm back to help you. How can I continue assisting you?`
                        : `Hello! I'm ${user.fullName} and I'm here to help you. How can I assist you today?`;
                    websocketService.sendMessage(conversationId, user.id, greetingMessage);
                }, 500);
                
                toast.success('Conversation accepted');
            }
        } catch (error) {
            toast.error('Failed to accept conversation');
        }
    };

    const handleCloseConversation = async () => {
        if (!activeConversation) return;

        try {
            // Send notification to customer that admin left
            websocketService.sendMessage(activeConversation.id, user.id, `${user.fullName} has left the conversation. Please wait for another admin to join or continue the conversation.`);
            
            // Set conversation to WAITING status so another admin can join
            const response = await closeConversation(activeConversation.id);
            
            if (response.success) {
                // Immediately update local state
                setInProgressConversations(prev => prev.filter(conv => conv.id !== activeConversation.id));
                setWaitingConversations(prev => {
                    const exists = prev.find(conv => conv.id === activeConversation.id);
                    if (!exists) {
                        return [...prev, {...activeConversation, status: 'WAITING', adminId: null, adminName: null}];
                    }
                    return prev;
                });
                setActiveConversation(null);
                setActiveTab('waiting');
                toast.success('Left conversation - moved to waiting queue');
            } else {
                toast.error('Failed to leave conversation');
            }
        } catch (error) {
            toast.error('Failed to leave conversation');
        }
    };

    const handleSolveConversation = async () => {
        if (!activeConversation) return;

        try {
            await solveConversation(activeConversation.id);
            // Send notification to customer
            websocketService.sendMessage(activeConversation.id, user.id, 'Your issue has been resolved. This conversation is now marked as solved. Thank you!');
            setActiveConversation(null);
            loadOpenConversations();
            loadWaitingConversations();
            loadInProgressConversations();
            loadSolvedConversations();
            toast.success('Conversation marked as solved');
        } catch (error) {
            toast.error('Failed to solve conversation');
        }
    };

    const handleSendMessage = () => {
        if (!message.trim() || !activeConversation) return;

        websocketService.sendMessage(activeConversation.id, user.id, message);
        setMessage('');
        setAdminIsTyping(false);
        websocketService.sendTypingIndicator(activeConversation.id, user.id, false);
        if (typingTimeoutRef.current) {
            clearTimeout(typingTimeoutRef.current);
        }
    };

    const handleTyping = (value) => {
        setMessage(value);
        if (!adminIsTyping && value.trim()) {
            setAdminIsTyping(true);
            websocketService.sendTypingIndicator(activeConversation.id, user.id, true);
        }
        
        if (typingTimeoutRef.current) {
            clearTimeout(typingTimeoutRef.current);
        }
        
        if (value.trim()) {
            typingTimeoutRef.current = setTimeout(() => {
                setAdminIsTyping(false);
                websocketService.sendTypingIndicator(activeConversation.id, user.id, false);
            }, 1000);
        } else {
            setAdminIsTyping(false);
            websocketService.sendTypingIndicator(activeConversation.id, user.id, false);
        }
    };



    const getStatusBadge = (status) => {
        const badges = {
            OPEN: 'bg-yellow-100 text-yellow-800',
            IN_PROGRESS: 'bg-blue-100 text-blue-800',
            CLOSED: 'bg-gray-100 text-gray-800'
        };
        return badges[status] || badges.OPEN;
    };

    const userRole = user?.role?.authority || user?.role;
    if (userRole !== 'ROLE_ADMIN') return null;

    return (
        <div className="flex flex-col lg:flex-row min-h-screen bg-gray-100">
            {/* Sidebar */}
            <div className="w-full lg:w-1/3 bg-white border-r lg:border-b-0 border-b">
                <div className="p-4 border-b">
                    <h2 className="text-lg font-semibold">Chat Support Panel</h2>
                    <div className="flex mt-3 border-b">
                        <button
                            onClick={() => setActiveTab('requests')}
                            className={`px-4 py-2 text-sm font-medium ${
                                activeTab === 'requests'
                                    ? 'border-b-2 border-blue-500 text-blue-600'
                                    : 'text-gray-500 hover:text-gray-700'
                            }`}
                        >
                            Requests ({openConversations.length})
                        </button>
                        <button
                            onClick={() => setActiveTab('waiting')}
                            className={`px-4 py-2 text-sm font-medium ${
                                activeTab === 'waiting'
                                    ? 'border-b-2 border-blue-500 text-blue-600'
                                    : 'text-gray-500 hover:text-gray-700'
                            }`}
                        >
                            Waiting ({waitingConversations.length})
                        </button>
                        <button
                            onClick={() => setActiveTab('progress')}
                            className={`px-4 py-2 text-sm font-medium ${
                                activeTab === 'progress'
                                    ? 'border-b-2 border-blue-500 text-blue-600'
                                    : 'text-gray-500 hover:text-gray-700'
                            }`}
                        >
                            In Progress ({inProgressConversations.length})
                        </button>
                        <button
                            onClick={() => setActiveTab('solved')}
                            className={`px-4 py-2 text-sm font-medium ${
                                activeTab === 'solved'
                                    ? 'border-b-2 border-blue-500 text-blue-600'
                                    : 'text-gray-500 hover:text-gray-700'
                            }`}
                        >
                            Solved ({solvedConversations.length})
                        </button>
                    </div>
                </div>

                <div className="flex-1 overflow-y-auto max-h-64 lg:max-h-none">
                    <div className="p-4">
                        {activeTab === 'requests' ? (
                            <>
                                {openConversations.length === 0 ? (
                                    <p className="text-gray-500 text-sm">No pending requests</p>
                                ) : (
                                    openConversations.map((conv) => (
                                        <div
                                            key={conv.id}
                                            className="p-3 border rounded mb-2 hover:bg-gray-50"
                                        >
                                            <div className="flex items-center justify-between mb-2">
                                                <div className="flex items-center">
                                                    <FaUser className="mr-2 text-gray-400" />
                                                    <span className="font-medium text-sm">
                                                        {conv.customerName}
                                                    </span>
                                                </div>
                                                <span className="px-2 py-1 rounded text-xs bg-orange-100 text-orange-800">
                                                    REQUESTED
                                                </span>
                                            </div>
                                            <div className="text-sm text-gray-600 mb-2">
                                                {conv.subject}
                                            </div>
                                            <div className="flex items-center justify-between">
                                                <div className="flex items-center text-xs text-gray-400">
                                                    <FaClock className="mr-1" />
                                                    {new Date(conv.createdAt).toLocaleString()}
                                                </div>
                                                <button
                                                    onClick={() => handleAcceptConversation(conv.id)}
                                                    className="bg-blue-600 text-white px-3 py-1 rounded text-xs hover:bg-blue-700"
                                                >
                                                    Accept
                                                </button>
                                            </div>
                                        </div>
                                    ))
                                )}
                            </>
                        ) : activeTab === 'waiting' ? (
                            <>
                                {waitingConversations.length === 0 ? (
                                    <p className="text-gray-500 text-sm">No customers waiting</p>
                                ) : (
                                    waitingConversations.map((conv) => (
                                        <div
                                            key={conv.id}
                                            className="p-3 border rounded mb-2 hover:bg-gray-50"
                                        >
                                            <div className="flex items-center justify-between mb-2">
                                                <div className="flex items-center">
                                                    <FaUser className="mr-2 text-gray-400" />
                                                    <span className="font-medium text-sm">
                                                        {conv.customerName}
                                                    </span>
                                                </div>
                                                <span className="px-2 py-1 rounded text-xs bg-yellow-100 text-yellow-800">
                                                    WAITING
                                                </span>
                                            </div>
                                            <div className="text-sm text-gray-600 mb-2">
                                                {conv.subject}
                                            </div>
                                            <div className="flex items-center justify-between">
                                                <div className="flex items-center text-xs text-gray-400">
                                                    <FaClock className="mr-1" />
                                                    {new Date(conv.createdAt).toLocaleString()}
                                                </div>
                                                <button
                                                    onClick={() => handleAcceptConversation(conv.id)}
                                                    className="bg-blue-600 text-white px-3 py-1 rounded text-xs hover:bg-blue-700"
                                                >
                                                    Accept
                                                </button>
                                            </div>
                                        </div>
                                    ))
                                )}
                            </>
                        ) : activeTab === 'progress' ? (
                            <>
                                {inProgressConversations.length === 0 ? (
                                    <p className="text-gray-500 text-sm">No conversations in progress</p>
                                ) : (
                                    inProgressConversations.map((conv) => (
                                        <div
                                            key={conv.id}
                                            className="p-3 border rounded mb-2 hover:bg-gray-50 cursor-pointer"
                                            onClick={() => setActiveConversation(conv)}
                                        >
                                            <div className="flex items-center justify-between mb-2">
                                                <div className="flex items-center">
                                                    <FaUser className="mr-2 text-gray-400" />
                                                    <span className="font-medium text-sm">
                                                        {conv.customerName}
                                                    </span>
                                                </div>
                                                <span className="px-2 py-1 rounded text-xs bg-blue-100 text-blue-800">
                                                    IN PROGRESS
                                                </span>
                                            </div>
                                            <div className="text-sm text-gray-600 mb-1">
                                                {conv.subject}
                                            </div>
                                            <div className="flex items-center text-xs text-gray-400">
                                                <FaClock className="mr-1" />
                                                {new Date(conv.updatedAt).toLocaleString()}
                                            </div>
                                        </div>
                                    ))
                                )}
                            </>
                        ) : (
                            <>
                                {solvedConversations.length === 0 ? (
                                    <p className="text-gray-500 text-sm">No solved conversations</p>
                                ) : (
                                    solvedConversations.map((conv) => (
                                        <div
                                            key={conv.id}
                                            className="p-3 border rounded mb-2 bg-green-50 hover:bg-green-100 cursor-pointer"
                                            onClick={() => setActiveConversation(conv)}
                                        >
                                            <div className="flex items-center justify-between mb-2">
                                                <div className="flex items-center">
                                                    <FaUser className="mr-2 text-gray-400" />
                                                    <span className="font-medium text-sm">
                                                        {conv.customerName}
                                                    </span>
                                                </div>
                                                <span className="px-2 py-1 rounded text-xs bg-green-100 text-green-800">
                                                    SOLVED
                                                </span>
                                            </div>
                                            <div className="text-sm text-gray-600 mb-1">
                                                {conv.subject}
                                            </div>
                                            <div className="flex items-center text-xs text-gray-400">
                                                <FaClock className="mr-1" />
                                                {new Date(conv.updatedAt).toLocaleString()}
                                            </div>
                                        </div>
                                    ))
                                )}
                            </>
                        )}
                    </div>
                </div>
            </div>

            {/* Chat Area */}
            <div className="flex-1 flex flex-col min-h-0">
                {activeConversation ? (
                    <>
                        <div className="p-4 border-b bg-white flex items-center justify-between">
                            <div>
                                <h3 className="font-semibold">{activeConversation.customerName}</h3>
                                <p className="text-sm text-gray-600">{activeConversation.subject}</p>
                                {activeConversation.status === 'SOLVED' && (
                                    <span className="text-xs bg-green-100 text-green-800 px-2 py-1 rounded mt-1 inline-block">
                                        SOLVED - Read Only
                                    </span>
                                )}
                            </div>
                            {activeConversation.status !== 'SOLVED' && (
                                <div className="flex gap-2">
                                    <button
                                        onClick={handleSolveConversation}
                                        className="bg-green-600 text-white px-3 py-1 rounded text-sm hover:bg-green-700"
                                    >
                                        <FaCheck className="mr-1" />
                                        Solve
                                    </button>
                                    <button
                                        onClick={handleCloseConversation}
                                        className="bg-orange-600 text-white px-3 py-1 rounded text-sm hover:bg-orange-700"
                                    >
                                        <FaTimes className="mr-1" />
                                        Leave
                                    </button>
                                </div>
                            )}
                        </div>

                        <div className="flex-1 p-4 overflow-y-auto bg-gray-50 h-64 lg:h-auto" style={{minHeight: '300px', maxHeight: 'calc(100vh - 200px)'}}>
                            {activeConversation.messages?.map((msg, index) => (
                                <div
                                    key={index}
                                    className={`mb-4 flex ${
                                        msg.senderType === 'ADMIN' ? 'justify-end' : 'justify-start'
                                    }`}
                                >
                                    <div
                                        className={`max-w-xs lg:max-w-md px-4 py-2 rounded-lg ${
                                            msg.senderType === 'ADMIN'
                                                ? 'bg-blue-600 text-white'
                                                : 'bg-white text-gray-800'
                                        }`}
                                    >
                                        <div className="text-xs mb-1 opacity-75">
                                            {msg.senderName}
                                        </div>
                                        <div>{msg.content}</div>
                                        <div className="text-xs mt-1 opacity-75 flex items-center">
                                            {new Date(msg.sentAt).toLocaleTimeString()}
                                            {msg.senderType === 'ADMIN' && msg.isRead && <span className="ml-2">✓</span>}
                                        </div>
                                    </div>
                                </div>
                            ))}
                            {isTyping && (
                                <div className="mb-2 p-2 rounded text-sm bg-gray-100 mr-4 italic text-gray-500">
                                    {isTyping} is typing...
                                </div>
                            )}
                            <div ref={messagesEndRef} />
                        </div>

                        {activeConversation.status !== 'SOLVED' && (
                            <div className="p-4 bg-white border-t">
                                <div className="flex gap-2">
                                    <input
                                        type="text"
                                        value={message}
                                        onChange={(e) => handleTyping(e.target.value)}
                                        onKeyPress={(e) => e.key === 'Enter' && handleSendMessage()}
                                        placeholder="Type your message..."
                                        className="flex-1 p-2 border rounded"
                                    />
                                    <button
                                        onClick={handleSendMessage}
                                        className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700"
                                    >
                                        <FaPaperPlane />
                                    </button>
                                </div>
                            </div>
                        )}
                    </>
                ) : (
                    <div className="flex-1 flex items-center justify-center bg-gray-50">
                        <div className="text-center">
                            <FaComments size={48} className="mx-auto text-gray-400 mb-4" />
                            <p className="text-gray-600">Select a conversation to start chatting</p>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};

export default AdminChatPanel;