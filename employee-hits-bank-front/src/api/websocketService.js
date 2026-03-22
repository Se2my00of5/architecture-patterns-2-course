import { Client } from '@stomp/stompjs';
import { oauthService } from './oauthService';

class WebSocketService {
  constructor() {
    this.client = null;
    this.subscriptions = new Map();
    this.pendingSubscriptions = [];
  }

  connect() {
    if (this.client && this.client.connected) {
      console.log('Already connected');
      return;
    }

    const token = oauthService.getAccessToken();
    if (!token) {
      console.error('No token for WebSocket connection');
      return;
    }

    console.log('Connecting to WebSocket');

    this.client = new Client({
      brokerURL: 'ws://localhost:1111/ws',
      reconnectDelay: 5000,
      connectHeaders: {
        Authorization: `Bearer ${token}`,
        login: token,
        passcode: token
      },
      debug: (str) => {
        console.log('STOMP debug:', str);
      },
      onConnect: (frame) => {
        console.log('WebSocket connected');
        const pending = [...this.pendingSubscriptions];
        this.pendingSubscriptions = [];
        pending.forEach(({ accountId, onMessage }) => {
          this.doSubscribe(accountId, onMessage);
        });
      },
      onStompError: (frame) => {
        console.error('STOMP error:', frame);
        console.error('Error headers:', frame.headers);
      },
      onWebSocketError: (event) => {
        console.error('WebSocket error:', event);
      },
      onDisconnect: () => {
        console.log('WebSocket disconnected');
      }
    });

    this.client.activate();
  }

  doSubscribe(accountId, onMessage) {
    if (!this.client || !this.client.connected) {
      console.warn(`WebSocket not connected, queueing subscription for ${accountId}`);
      this.pendingSubscriptions.push({ accountId, onMessage });
      return;
    }

    const token = oauthService.getAccessToken();
    const topic = `/topic/accounts/${accountId}/operations`;
    console.log(`Subscribing to: ${topic}`);
    
    const subscription = this.client.subscribe(topic, (message) => {
      console.log(`Received message for account ${accountId}:`, message.body);
      try {
        const operation = JSON.parse(message.body);
        if (onMessage) {
          onMessage(operation);
        }
      } catch (e) {
        console.error('Error parsing operation:', e);
      }
    }, {
      Authorization: `Bearer ${token}`
    });

    this.subscriptions.set(accountId, subscription);
    console.log(`Subscribed to account ${accountId}`);
  }

  subscribeToAccount(accountId, onMessage) {
    this.doSubscribe(accountId, onMessage);
  }

  unsubscribeFromAccount(accountId) {
    const subscription = this.subscriptions.get(accountId);
    if (subscription) {
      subscription.unsubscribe();
      this.subscriptions.delete(accountId);
    }
  }

  unsubscribeAll() {
    this.subscriptions.forEach((subscription) => {
      subscription.unsubscribe();
    });
    this.subscriptions.clear();
  }
}

export const websocketService = new WebSocketService();