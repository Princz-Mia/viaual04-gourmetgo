import React, { useState, useEffect, useRef } from 'react';
import { FaComments, FaTimes, FaPaperPlane, FaHistory } from 'react-icons/fa';
import { useAuth } from '../../contexts/AuthContext';
import { useChat } from '../../contexts/ChatContext';
import { startConversation, getCustomerConversations } from '../../api/chatService';
import websocketService from '../../services/websocketService';
import { toast } from 'react-toastify';

const CHAT_CATEGORIES = [
    'Order Issue',
    'Payment Problem', 
    'Delivery Delay',
    'Food Quality',
    'Account Help',
    'Technical Support',
    'Other'
];

const CustomerChatWidget = () => {
    const { user } = useAuth();
    const { markAsRead } = useChat();
    const [isOpen, setIsOpen] = useState(false);
    const [conversations, setConversations] = useState([]);
    const [activeConversation, setActiveConversation] = useState(null);
    const [message, setMessage] = useState('');
    const [subject, setSubject] = useState('');
    const [showNewChat, setShowNewChat] = useState(false);
    const [showHistory, setShowHistory] = useState(false);
    const [isTyping, setIsTyping] = useState(false);
    const [showAdminJoined, setShowAdminJoined] = useState(false);
    const [hasUnreadMessages, setHasUnreadMessages] = useState(false);
    const [adminTyping, setAdminTyping] = useState(false);
    const messagesEndRef = useRef(null);
    const typingTimeoutRef = useRef(null);

    useEffect(() => {
        const userRole = user?.role?.authority || user?.role;
        if (userRole === 'ROLE_CUSTOMER' && isOpen) {
            loadConversations();
            connectWebSocket();
        }
    }, [user, isOpen]);

    useEffect(() => {
        if (activeConversation) {
            websocketService.subscribeToConversation(activeConversation.id, handleNewMessage);
            websocketService.subscribeToConversationStatus(activeConversation.id, handleStatusUpdate);
            websocketService.subscribeToTyping(activeConversation.id, handleAdminTyping);
        }
        return () => {
            if (activeConversation) {
                websocketService.unsubscribe(`conversation-${activeConversation.id}`);
                websocketService.unsubscribe(`status-${activeConversation.id}`);
                websocketService.unsubscribe(`typing-${activeConversation.id}`);
            }
        };
    }, [activeConversation]);

/*     useEffect(() => {
        if (user?.role.authority === 'ROLE_CUSTOMER' && isOpen) {
            const interval = setInterval(() => {
                loadConversations();
            }, 5000); // Refresh every 5 seconds
            
            return () => clearInterval(interval);
        }
    }, [user, isOpen, activeConversation?.id]); */

    useEffect(() => {
        scrollToBottom();
    }, [activeConversation?.messages]);

    const connectWebSocket = async () => {
        try {
            await websocketService.connect();
            // Subscribe to customer notifications
            if (user?.id) {
                websocketService.subscribe(`/topic/customer/${user.id}/notifications`, handleCustomerNotification);
            }
        } catch (error) {
            console.error('WebSocket connection failed:', error);
        }
    };

    const handleCustomerNotification = (message) => {
        console.log('Customer notification received:', message);
        
        // Handle different message formats
        const data = message.data || message;
        const type = message.type || data.type;
        
        if (type === 'ADMIN_JOINED') {
            const adminName = data.adminName || 'Admin';
            const conversationId = data.conversationId;
            
            setActiveConversation(prev => prev ? {...prev, status: 'IN_PROGRESS', adminName} : null);
            setConversations(prev => prev.map(conv => 
                conv.id === conversationId ? {...conv, status: 'IN_PROGRESS', adminName} : conv
            ));
            setShowAdminJoined(true);
            setTimeout(() => setShowAdminJoined(false), 3000);
            toast.success(`${adminName} joined the chat`);
        } else if (type === 'CONVERSATION_SOLVED') {
            const conversationId = data.conversationId;
            
            setActiveConversation(prev => prev ? {...prev, status: 'SOLVED'} : null);
            setConversations(prev => prev.map(conv => 
                conv.id === conversationId ? {...conv, status: 'SOLVED'} : conv
            ));
            toast.success('Your issue has been resolved');
        } else if (type === 'ADMIN_LEFT') {
            const conversationId = data.conversationId;
            
            setActiveConversation(prev => prev ? {...prev, status: 'WAITING', adminName: null} : null);
            setConversations(prev => prev.map(conv => 
                conv.id === conversationId ? {...conv, status: 'WAITING', adminName: null} : conv
            ));
            setShowAdminJoined(false);
            toast.info('Admin has left the chat. You are back in the waiting queue.');
        }
    };

    const loadConversations = async () => {
        try {
            const response = await getCustomerConversations(user.id);
            if (response.success) {
                setConversations(response.data);
                // Only auto-select if no conversation is active and there's an active/waiting conversation
                if (response.data.length > 0 && !activeConversation) {
                    const activeConv = response.data.find(conv => 
                        conv.status === 'OPEN' || conv.status === 'IN_PROGRESS'
                    );
                    if (activeConv) {
                        setActiveConversation(activeConv);
                    }
                }
            }
        } catch (error) {
            console.error('Failed to load conversations:', error);
        }
    };

    const handleNewMessage = (messageData) => {
        setActiveConversation(prev => {
            const updated = {
                ...prev,
                messages: [...(prev.messages || []), messageData]
            };
            
            // Mark message as read if from admin and chat is open
            if (messageData.senderType === 'ADMIN') {
                if (isOpen) {
                    markMessageAsRead(messageData.id);
                    // Update the message as read in the UI immediately
                    messageData.isRead = true;
                } else {
                    setHasUnreadMessages(true);
                }
            }
            
            return updated;
        });
    };

    const handleStatusUpdate = (statusData) => {
        const prevStatus = activeConversation?.status;
        
        setActiveConversation(prev => ({
            ...prev,
            status: statusData.status
        }));
        
        // Reload conversations to get updated status
        loadConversations();
        
        // Show admin joined banner when transitioning to IN_PROGRESS
        if (statusData.status === 'IN_PROGRESS' && (prevStatus === 'WAITING' || prevStatus === 'REQUESTED') && statusData.adminName) {
            setShowAdminJoined(true);
            setTimeout(() => setShowAdminJoined(false), 3000);
            toast.success(`${statusData.adminName} joined the chat`);
        }
        
        // When admin leaves, conversation goes back to WAITING
        if (statusData.status === 'WAITING' && prevStatus === 'IN_PROGRESS') {
            toast.info('Admin has left the chat. You are back in the waiting queue.');
            // Force show waiting banner
            setShowAdminJoined(false);
        }
        
        if (statusData.status === 'CLOSED') {
            toast.info('Chat has been closed by admin');
            setTimeout(() => {
                setActiveConversation(null);
            }, 2000);
        }
        
        if (statusData.status === 'SOLVED') {
            toast.success('Your issue has been marked as solved');
            setTimeout(() => {
                setActiveConversation(null);
            }, 3000);
        }
    };

    const handleAdminTyping = (typingData) => {
        if (typingData.senderType === 'ADMIN') {
            setAdminTyping(typingData.isTyping ? typingData.senderName : false);
        }
    };

    const markMessageAsRead = async (messageId) => {
        try {
            await fetch(`http://localhost:8080/api/v1/chat/messages/${messageId}/read`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${user.token}`
                },
                body: JSON.stringify({ userId: user.id })
            });
        } catch (error) {
            console.error('Failed to mark message as read:', error);
        }
    };

    const handleStartConversation = async () => {
        if (!subject.trim()) return;

        try {
            const response = await startConversation(user.id, subject);
            if (response.success) {
                setConversations(prev => [response.data, ...prev]);
                setActiveConversation(response.data);
                setSubject('');
                setShowNewChat(false);
                toast.success('Chat started successfully');
            }
        } catch (error) {
            toast.error('Failed to start conversation');
        }
    };

    const handleSendMessage = () => {
        if (!message.trim() || !activeConversation) return;

        websocketService.sendMessage(activeConversation.id, user.id, message);
        setMessage('');
        setIsTyping(false);
        websocketService.sendTypingIndicator(activeConversation.id, user.id, false);
        if (typingTimeoutRef.current) {
            clearTimeout(typingTimeoutRef.current);
        }
    };

    const handleTyping = (value) => {
        setMessage(value);
        if (!isTyping && value.trim()) {
            setIsTyping(true);
            websocketService.sendTypingIndicator(activeConversation.id, user.id, true);
        }
        
        if (typingTimeoutRef.current) {
            clearTimeout(typingTimeoutRef.current);
        }
        
        if (value.trim()) {
            typingTimeoutRef.current = setTimeout(() => {
                setIsTyping(false);
                websocketService.sendTypingIndicator(activeConversation.id, user.id, false);
            }, 1000);
        } else {
            setIsTyping(false);
            websocketService.sendTypingIndicator(activeConversation.id, user.id, false);
        }
    };

    const scrollToBottom = () => {
        messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    };

    const userRole = user?.role?.authority || user?.role;
    if (!user || userRole !== 'ROLE_CUSTOMER') return null;

    return (
        <>
            <div className="fixed bottom-6 right-6 z-50">
                <button
                    onClick={() => {
                        setIsOpen(!isOpen);
                        if (!isOpen) {
                            setHasUnreadMessages(false);
                            markAsRead();
                        }
                    }}
                    className="bg-blue-600 hover:bg-blue-700 text-white p-4 rounded-full shadow-lg transition-all duration-200 hover:scale-105 relative"
                    title="Need help? Chat with support"
                >
                    <FaComments size={20} />
                    {hasUnreadMessages && (
                        <span className="absolute -top-1 -right-1 bg-red-500 text-white text-xs rounded-full w-5 h-5 flex items-center justify-center">
                            !
                        </span>
                    )}
                </button>
            </div>

            {isOpen && (
                <div className="fixed bottom-24 right-6 w-96 h-[500px] bg-white border rounded-lg shadow-xl z-50 animate-in slide-in-from-bottom-2">
                    <div className="flex items-center justify-between p-3 bg-blue-600 text-white rounded-t-lg">
                        <h3 className="font-semibold">Customer Support</h3>
                        <button onClick={() => setIsOpen(false)}>
                            <FaTimes />
                        </button>
                    </div>

                    {showHistory ? (
                        <div className="h-full flex flex-col">
                            <div className="p-3 border-b flex items-center justify-between">
                                <h4 className="font-semibold">Conversation History</h4>
                                <button
                                    onClick={() => setShowHistory(false)}
                                    className="text-gray-500 hover:text-gray-700"
                                >
                                    <FaTimes />
                                </button>
                            </div>
                            <div className="flex-1 overflow-y-auto p-3" style={{height: 'calc(100% - 60px)'}}>
                                {conversations.length === 0 ? (
                                    <p className="text-gray-500 text-sm text-center mt-8">No conversations yet</p>
                                ) : (
                                    conversations.map((conv) => (
                                        <div
                                            key={conv.id}
                                            className="p-3 border rounded mb-2 hover:bg-gray-50 cursor-pointer"
                                            onClick={() => {
                                                setActiveConversation(conv);
                                                setShowHistory(false);
                                            }}
                                        >
                                            <div className="flex items-center justify-between mb-1">
                                                <span className="font-medium text-sm">{conv.subject}</span>
                                                <span className={`px-2 py-1 rounded text-xs ${
                                                    conv.status === 'REQUESTED' ? 'bg-orange-100 text-orange-800' :
                                                    conv.status === 'WAITING' ? 'bg-yellow-100 text-yellow-800' :
                                                    conv.status === 'IN_PROGRESS' ? 'bg-blue-100 text-blue-800' :
                                                    conv.status === 'SOLVED' ? 'bg-green-100 text-green-800' :
                                                    'bg-gray-100 text-gray-800'
                                                }`}>
                                                    {conv.status === 'REQUESTED' ? 'REQUESTED' :
                                                     conv.status === 'WAITING' ? 'WAITING' :
                                                     conv.status === 'IN_PROGRESS' ? 'ACTIVE' :
                                                     conv.status === 'SOLVED' ? 'SOLVED' : 'CLOSED'}
                                                </span>
                                            </div>
                                            <div className="text-xs text-gray-500">
                                                {new Date(conv.updatedAt).toLocaleString()}
                                            </div>
                                        </div>
                                    ))
                                )}
                            </div>
                        </div>
                    ) : showNewChat ? (
                        <div className="p-4 h-full overflow-y-auto">
                            <div className="mb-3">
                                <label className="block text-sm font-medium text-gray-700 mb-2">What can we help you with?</label>
                                <div className="grid grid-cols-1 gap-2 max-h-64 overflow-y-auto">
                                    {CHAT_CATEGORIES.map((category) => (
                                        <button
                                            key={category}
                                            onClick={() => setSubject(category)}
                                            className={`p-2 text-left border rounded-lg transition-colors ${
                                                subject === category
                                                    ? 'bg-blue-100 border-blue-500 text-blue-700'
                                                    : 'bg-white border-gray-300 hover:bg-gray-50'
                                            }`}
                                        >
                                            {category}
                                        </button>
                                    ))}
                                </div>
                            </div>
                            <div className="flex gap-2">
                                <button
                                    onClick={handleStartConversation}
                                    disabled={!subject.trim()}
                                    className="flex-1 bg-blue-600 text-white py-2 rounded-lg hover:bg-blue-700 disabled:bg-gray-300 disabled:cursor-not-allowed transition-colors"
                                >
                                    Start Chat
                                </button>
                                <button
                                    onClick={() => {
                                        setShowNewChat(false);
                                        setSubject('');
                                    }}
                                    className="px-4 bg-gray-100 text-gray-700 py-2 rounded-lg hover:bg-gray-200 transition-colors"
                                >
                                    Cancel
                                </button>
                            </div>
                        </div>
                    ) : activeConversation ? (
                        <>
                            <div className="p-2 border-b flex items-center justify-between">
                                <div className="flex items-center">
                                    <button
                                        onClick={() => setActiveConversation(null)}
                                        className="text-gray-500 hover:text-gray-700 mr-2"
                                    >
                                        ←
                                    </button>
                                    <span className="text-sm font-medium">{activeConversation.subject}</span>
                                </div>
                                <span className={`px-2 py-1 rounded text-xs ${
                                    activeConversation.status === 'REQUESTED' ? 'bg-orange-100 text-orange-800' :
                                    activeConversation.status === 'WAITING' ? 'bg-yellow-100 text-yellow-800' :
                                    activeConversation.status === 'IN_PROGRESS' ? 'bg-blue-100 text-blue-800' :
                                    activeConversation.status === 'SOLVED' ? 'bg-green-100 text-green-800' :
                                    'bg-gray-100 text-gray-800'
                                }`}>
                                    {activeConversation.status === 'REQUESTED' ? 'REQUESTED' :
                                     activeConversation.status === 'WAITING' ? 'WAITING' :
                                     activeConversation.status === 'IN_PROGRESS' ? 'ACTIVE' :
                                     activeConversation.status === 'SOLVED' ? 'SOLVED' : 'CLOSED'}
                                </span>
                            </div>
                                            {activeConversation?.status === 'REQUESTED' && (
                                <div className="bg-orange-50 p-2 text-xs text-center border-b">
                                    <span className="text-orange-700">⏳ Request sent, waiting for admin...</span>
                                </div>
                            )}
                            {activeConversation?.status === 'WAITING' && (
                                <div className="bg-yellow-50 p-2 text-xs text-center border-b">
                                    <span className="text-yellow-700">⏳ Admin left, waiting for another admin to join...</span>
                                </div>
                            )}
                            {activeConversation?.status === 'OPEN' && (
                                <div className="bg-yellow-50 p-2 text-xs text-center border-b">
                                    <span className="text-yellow-700">⏳ Waiting for admin to join...</span>
                                </div>
                            )}
                            {showAdminJoined && activeConversation?.status === 'IN_PROGRESS' && (
                                <div className="bg-green-50 p-2 text-xs text-center border-b">
                                    <span className="text-green-700">✅ Admin joined the chat</span>
                                </div>
                            )}
                            <div className="flex-1 p-3 overflow-y-auto" style={{height: 'calc(100% - 120px)'}}>
                                {activeConversation?.messages?.map((msg, index) => (
                                    <div
                                        key={index}
                                        className={`mb-2 p-2 rounded text-sm ${
                                            msg.senderType === 'CUSTOMER'
                                                ? 'bg-blue-100 ml-4'
                                                : 'bg-gray-100 mr-4'
                                        }`}
                                    >
                                        <div className="font-semibold text-xs mb-1">
                                            {msg.senderName}
                                        </div>
                                        <div>{msg.content}</div>
                                        <div className="text-xs text-gray-500 mt-1">
                                            {new Date(msg.sentAt).toLocaleTimeString()}
                                            {msg.senderType === 'CUSTOMER' && msg.isRead && <span className="ml-2">✓</span>}
                                        </div>
                                    </div>
                                ))}
                                {adminTyping && (
                                    <div className="mb-2 p-2 rounded text-sm bg-white text-gray-800 mr-4 italic">
                                        {adminTyping} is typing...
                                    </div>
                                )}
                                <div ref={messagesEndRef} />
                            </div>

                            {activeConversation.status !== 'SOLVED' && activeConversation.status !== 'CLOSED' && (
                                <div className="p-3 border-t flex gap-2">
                                    <input
                                        type="text"
                                        value={message}
                                        onChange={(e) => handleTyping(e.target.value)}
                                        onKeyPress={(e) => e.key === 'Enter' && handleSendMessage()}
                                        placeholder="Type your message..."
                                        className="flex-1 p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent text-sm"
                                    />
                                    <button
                                        onClick={handleSendMessage}
                                        disabled={!message.trim()}
                                        className="bg-blue-600 text-white p-2 rounded-lg hover:bg-blue-700 disabled:bg-gray-300 disabled:cursor-not-allowed transition-colors"
                                    >
                                        <FaPaperPlane size={14} />
                                    </button>
                                </div>
                            )}
                        </>
                    ) : (
                        <div className="p-6 text-center">
                            <div className="mb-4">
                                <FaComments size={48} className="mx-auto text-gray-300 mb-3" />
                                <p className="text-gray-600 mb-2">Need help?</p>
                                <p className="text-sm text-gray-500">Start a conversation with our support team</p>
                            </div>
                            <div className="space-y-2">
                                {conversations.some(conv => conv.status === 'REQUESTED' || conv.status === 'WAITING' || conv.status === 'IN_PROGRESS') ? (
                                    <div className="text-center">
                                        <p className="text-sm text-orange-600 mb-2">You have an active conversation</p>
                                        <button
                                            onClick={() => {
                                                const activeConv = conversations.find(conv => 
                                                    conv.status === 'REQUESTED' || conv.status === 'WAITING' || conv.status === 'IN_PROGRESS'
                                                );
                                                setActiveConversation(activeConv);
                                            }}
                                            className="w-full bg-orange-600 text-white px-6 py-2 rounded-lg hover:bg-orange-700 transition-colors mb-2"
                                        >
                                            Continue Chat
                                        </button>
                                    </div>
                                ) : (
                                    <button
                                        onClick={() => setShowNewChat(true)}
                                        className="w-full bg-blue-600 text-white px-6 py-2 rounded-lg hover:bg-blue-700 transition-colors"
                                    >
                                        Start New Chat
                                    </button>
                                )}
                                <button
                                    onClick={() => setShowHistory(true)}
                                    className="w-full bg-gray-100 text-gray-700 px-6 py-2 rounded-lg hover:bg-gray-200 transition-colors flex items-center justify-center"
                                >
                                    <FaHistory className="mr-2" />
                                    View History
                                </button>
                            </div>
                        </div>
                    )}
                </div>
            )}
        </>
    );
};

export default CustomerChatWidget;