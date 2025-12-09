import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

class WebSocketService {
    constructor() {
        this.client = null;
        this.connected = false;
        this.subscriptions = new Map();
        this.messageHandlers = new Map();
    }

    connect() {
        return new Promise((resolve, reject) => {
            if (this.connected && this.client) {
                resolve();
                return;
            }

            // Disconnect existing client if any
            if (this.client) {
                this.client.deactivate();
            }

            this.client = new Client({
                webSocketFactory: () => new SockJS('http://localhost:8080/ws-chat'),
                onConnect: () => {
                    this.connected = true;
                    console.log('WebSocket connected');
                    resolve();
                },
                onDisconnect: () => {
                    this.connected = false;
                    console.log('WebSocket disconnected');
                },
                onStompError: (frame) => {
                    console.error('STOMP error:', frame);
                    this.connected = false;
                    reject(frame);
                },
                reconnectDelay: 5000,
                heartbeatIncoming: 4000,
                heartbeatOutgoing: 4000
            });

            this.client.activate();
        });
    }

    disconnect() {
        if (this.client) {
            this.client.deactivate();
            this.connected = false;
            this.subscriptions.clear();
            this.messageHandlers.clear();
        }
    }

    subscribeToConversation(conversationId, messageCallback, typingCallback) {
        if (!this.connected) return;

        const messageDestination = `/topic/conversation/${conversationId}`;
        const messageSubscription = this.client.subscribe(messageDestination, (message) => {
            const data = JSON.parse(message.body);
            messageCallback(data);
        });

        const typingDestination = `/topic/conversation/${conversationId}/typing`;
        const typingSubscription = this.client.subscribe(typingDestination, (message) => {
            const data = JSON.parse(message.body);
            if (typingCallback) typingCallback(data);
        });

        this.subscriptions.set(`conversation-${conversationId}`, messageSubscription);
        this.subscriptions.set(`typing-${conversationId}`, typingSubscription);
        return { messageSubscription, typingSubscription };
    }

    subscribeToAdminNotifications(callback) {
        if (!this.connected) return;

        const destination = '/topic/admin/notifications';
        const subscription = this.client.subscribe(destination, (message) => {
            const data = JSON.parse(message.body);
            callback(data);
        });

        this.subscriptions.set('admin-notifications', subscription);
        return subscription;
    }

    subscribeToUserNotifications(callback) {
        if (!this.connected) return;

        const destination = '/topic/user/notifications';
        const subscription = this.client.subscribe(destination, (message) => {
            const data = JSON.parse(message.body);
            callback(data);
        });

        this.subscriptions.set('user-notifications', subscription);
        return subscription;
    }

    subscribeToHappyHour(callback) {
        if (!this.connected) return;

        const destination = '/topic/happy-hour';
        const subscription = this.client.subscribe(destination, (message) => {
            const data = JSON.parse(message.body);
            callback(data);
        });

        this.subscriptions.set('happy-hour', subscription);
        return subscription;
    }

    subscribe(destination, callback) {
        if (!this.connected || !this.client) {
            console.warn('WebSocket not connected, cannot subscribe to', destination);
            return;
        }

        const subscription = this.client.subscribe(destination, (message) => {
            const data = JSON.parse(message.body);
            callback(data);
        });

        this.subscriptions.set(destination, subscription);
        return subscription;
    }

    subscribeToConversationStatus(conversationId, callback) {
        if (!this.connected) return;

        const destination = `/topic/conversation/${conversationId}/status`;
        const subscription = this.client.subscribe(destination, (message) => {
            const data = JSON.parse(message.body);
            callback(data);
        });

        this.subscriptions.set(`status-${conversationId}`, subscription);
        return subscription;
    }

    subscribeToTyping(conversationId, callback) {
        if (!this.connected) return;

        const destination = `/topic/conversation/${conversationId}/typing`;
        const subscription = this.client.subscribe(destination, (message) => {
            const data = JSON.parse(message.body);
            callback(data);
        });

        this.subscriptions.set(`typing-${conversationId}`, subscription);
        return subscription;
    }

    unsubscribe(key) {
        const subscription = this.subscriptions.get(key);
        if (subscription) {
            subscription.unsubscribe();
            this.subscriptions.delete(key);
        }
    }

    sendMessage(conversationId, senderId, content) {
        if (!this.connected || !this.client) return;

        this.client.publish({
            destination: '/app/chat.sendMessage',
            body: JSON.stringify({
                conversationId,
                senderId,
                content
            })
        });
    }

    sendTypingIndicator(conversationId, senderId, isTyping) {
        if (!this.connected || !this.client) return;

        this.client.publish({
            destination: '/app/chat.typing',
            body: JSON.stringify({
                conversationId,
                senderId,
                isTyping
            })
        });
    }

    isConnected() {
        return this.connected;
    }
}

export default new WebSocketService();