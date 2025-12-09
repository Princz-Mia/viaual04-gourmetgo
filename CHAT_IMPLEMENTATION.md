# Chat Implementation Guide

## Overview
This document describes the WebSocket-based live chat functionality implemented for the GourmetGo food ordering application. The chat system allows customers to communicate with admins for support.

## Architecture

### Backend (Already Implemented)
- **WebSocket Configuration**: `/ws-chat` endpoint with SockJS support
- **Chat Entities**: `ChatConversation` and `ChatMessage` with status tracking
- **REST API**: Full CRUD operations for conversations and messages
- **Real-time Messaging**: STOMP protocol for WebSocket communication

### Frontend Implementation

#### Core Components

1. **WebSocket Service** (`src/services/websocketService.js`)
   - Manages WebSocket connections using SockJS and STOMP
   - Handles subscriptions to conversation topics
   - Provides methods for sending messages

2. **Chat API Service** (`src/api/chatService.js`)
   - REST API calls for conversation management
   - Functions for starting, assigning, and closing conversations

3. **Customer Chat Widget** (`src/components/chat/CustomerChatWidget.jsx`)
   - Floating chat button for customers
   - Modal interface for starting conversations
   - Real-time message display and sending

4. **Admin Chat Panel** (`src/components/chat/AdminChatPanel.jsx`)
   - Full-screen admin interface
   - List of open conversations with status indicators
   - Conversation assignment and management
   - Real-time message handling

5. **Chat Context** (`src/contexts/ChatContext.jsx`)
   - Global state management for chat notifications
   - WebSocket connection management
   - Unread message counting

## Features

### Customer Features
- **Start Conversations**: Customers can initiate chat with a subject
- **Real-time Messaging**: Instant message delivery and receipt
- **Conversation History**: Access to previous conversations
- **Status Indicators**: Visual feedback on conversation status

### Admin Features
- **Conversation Queue**: View all open customer requests
- **Accept Conversations**: Assign conversations to specific admins
- **Real-time Notifications**: Instant alerts for new messages
- **Conversation Management**: Close completed conversations
- **Status Tracking**: Visual indicators for conversation states

### Conversation States
- **OPEN**: New conversation waiting for admin assignment
- **ASSIGNED**: Conversation assigned to an admin
- **CLOSED**: Conversation completed by admin

## Usage

### For Customers
1. Click the floating chat button (bottom-right corner)
2. Click "Start New Chat" if no active conversations
3. Enter a subject and click "Start"
4. Type messages and press Enter or click send button

### For Admins
1. Navigate to `/admin/chat` or use the "Chat Support" link in header
2. View open conversations in the left sidebar
3. Click on a conversation to accept and start chatting
4. Use the "Close" button to end conversations
5. Monitor the notification icon in header for new messages

## Technical Details

### WebSocket Topics
- `/topic/conversation/{conversationId}`: Messages for specific conversations
- `/topic/admin/notifications`: New message notifications for admins

### API Endpoints
- `POST /api/chat/conversations`: Start new conversation
- `GET /api/chat/conversations/open`: Get open conversations (admin)
- `PUT /api/chat/conversations/{id}/assign`: Assign conversation to admin
- `PUT /api/chat/conversations/{id}/close`: Close conversation
- `GET /api/chat/conversations/{id}`: Get conversation details

### Message Format
```javascript
{
  id: number,
  conversationId: number,
  senderId: number,
  senderName: string,
  senderType: "CUSTOMER" | "ADMIN",
  content: string,
  sentAt: string,
  isRead: boolean
}
```

## Testing

### Test Page
Access `/chat-test` when logged in to test chat functionality:
- Create test conversations
- Subscribe to conversations
- Send and receive messages
- Monitor WebSocket connection status

### Manual Testing Steps
1. **Customer Flow**:
   - Login as customer
   - Start a new chat conversation
   - Send messages
   - Verify real-time delivery

2. **Admin Flow**:
   - Login as admin
   - Navigate to chat panel
   - Accept customer conversations
   - Respond to messages
   - Close conversations

3. **Real-time Testing**:
   - Open customer and admin interfaces in different browsers
   - Verify instant message delivery
   - Test notification system
   - Confirm status updates

## Configuration

### Environment Variables
- WebSocket endpoint: `http://localhost:8080/ws-chat`
- Modify in `websocketService.js` for different environments

### Dependencies Added
```json
{
  "sockjs-client": "^1.6.1",
  "@stomp/stompjs": "^7.0.0"
}
```

## Troubleshooting

### Common Issues
1. **WebSocket Connection Failed**: Check backend server is running
2. **Messages Not Delivering**: Verify subscription to correct topic
3. **Authentication Issues**: Ensure JWT token is valid
4. **CORS Errors**: Check WebSocket CORS configuration

### Debug Tips
- Check browser console for WebSocket connection logs
- Monitor network tab for WebSocket frames
- Use the test page to isolate issues
- Verify backend WebSocket configuration

## Future Enhancements
- File/image sharing in chat
- Typing indicators
- Message read receipts
- Chat history persistence
- Multiple admin assignment
- Customer satisfaction ratings