import React, { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import websocketService from '../services/websocketService';
import { startConversation, getOpenConversations } from '../api/chatService';

const ChatTest = () => {
    const { user } = useAuth();
    const [connected, setConnected] = useState(false);
    const [message, setMessage] = useState('');
    const [conversationId, setConversationId] = useState('');
    const [messages, setMessages] = useState([]);
    const [conversations, setConversations] = useState([]);

    useEffect(() => {
        if (user) {
            connect();
            loadConversations();
        }
    }, [user]);

    const connect = async () => {
        try {
            await websocketService.connect();
            setConnected(true);
            console.log('Connected to WebSocket');
        } catch (error) {
            console.error('Connection failed:', error);
        }
    };

    const loadConversations = async () => {
        try {
            const response = await getOpenConversations();
            if (response.success) {
                setConversations(response.data);
            }
        } catch (error) {
            console.error('Failed to load conversations:', error);
        }
    };

    const subscribeToConversation = (convId) => {
        websocketService.subscribeToConversation(convId, (messageData) => {
            setMessages(prev => [...prev, messageData]);
        });
    };

    const sendMessage = () => {
        if (message.trim() && conversationId && user) {
            websocketService.sendMessage(parseInt(conversationId), user.id, message);
            setMessage('');
        }
    };

    const createTestConversation = async () => {
        const userRole = user?.role?.authority || user?.role;
        if (userRole === 'ROLE_CUSTOMER') {
            try {
                const response = await startConversation(user.id, 'Test Conversation');
                if (response.success) {
                    setConversationId(response.data.id.toString());
                    subscribeToConversation(response.data.id);
                    console.log('Conversation created:', response.data);
                }
            } catch (error) {
                console.error('Failed to create conversation:', error);
            }
        }
    };

    return (
        <div className="container mx-auto p-4">
            <h1 className="text-2xl font-bold mb-4">Chat Test Page</h1>
            
            <div className="mb-4">
                <p>User: {user?.fullName || user?.name} ({user?.role?.authority || user?.role})</p>
                <p>WebSocket Status: {connected ? 'Connected' : 'Disconnected'}</p>
            </div>

            <div className="mb-4">
                <button 
                    onClick={createTestConversation}
                    className="bg-blue-500 text-white px-4 py-2 rounded mr-2"
                    disabled={(user?.role?.authority || user?.role) !== 'ROLE_CUSTOMER'}
                >
                    Create Test Conversation
                </button>
                <button 
                    onClick={loadConversations}
                    className="bg-green-500 text-white px-4 py-2 rounded"
                >
                    Load Conversations
                </button>
            </div>

            <div className="mb-4">
                <h3 className="font-bold">Open Conversations:</h3>
                {conversations.map(conv => (
                    <div key={conv.id} className="border p-2 mb-2">
                        <p>ID: {conv.id} - {conv.subject}</p>
                        <p>Customer: {conv.customerName}</p>
                        <button 
                            onClick={() => {
                                setConversationId(conv.id.toString());
                                subscribeToConversation(conv.id);
                            }}
                            className="bg-yellow-500 text-white px-2 py-1 rounded text-sm"
                        >
                            Subscribe
                        </button>
                    </div>
                ))}
            </div>

            <div className="mb-4">
                <input
                    type="text"
                    value={conversationId}
                    onChange={(e) => setConversationId(e.target.value)}
                    placeholder="Conversation ID"
                    className="border p-2 mr-2"
                />
                <button 
                    onClick={() => subscribeToConversation(parseInt(conversationId))}
                    className="bg-purple-500 text-white px-4 py-2 rounded"
                    disabled={!conversationId}
                >
                    Subscribe to Conversation
                </button>
            </div>

            <div className="mb-4">
                <input
                    type="text"
                    value={message}
                    onChange={(e) => setMessage(e.target.value)}
                    placeholder="Type a message..."
                    className="border p-2 mr-2 flex-1"
                    onKeyPress={(e) => e.key === 'Enter' && sendMessage()}
                />
                <button 
                    onClick={sendMessage}
                    className="bg-blue-500 text-white px-4 py-2 rounded"
                    disabled={!message.trim() || !conversationId}
                >
                    Send Message
                </button>
            </div>

            <div className="border p-4 h-64 overflow-y-auto">
                <h3 className="font-bold mb-2">Messages:</h3>
                {messages.map((msg, index) => (
                    <div key={index} className="mb-2 p-2 border-b">
                        <strong>{msg.senderName} ({msg.senderType}):</strong> {msg.content}
                        <div className="text-xs text-gray-500">
                            {new Date(msg.sentAt).toLocaleString()}
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
};

export default ChatTest;