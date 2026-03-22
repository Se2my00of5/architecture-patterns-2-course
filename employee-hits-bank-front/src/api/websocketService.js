import { Client } from '@stomp/stompjs';
import { oauthService } from './oauthService';

/**
 * @typedef {import('../types/api').WebSocketOperation} WebSocketOperation
 * @typedef {import('../types/api').WebSocketSubscribeOptions} WebSocketSubscribeOptions
 * @typedef {(operation: WebSocketOperation) => void} OnMessageCallback
 */

class WebSocketService {
  constructor() {
    /** @type {Client | null} */
    this.client = null;
    /** @type {Map<string, import('@stomp/stompjs').StompSubscription>} */
    this.subscriptions = new Map();
    /** @type {Array<{accountId: string, onMessage: OnMessageCallback}>} */
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

  /**
   * @param {string} accountId
   * @param {OnMessageCallback} onMessage
   */
  doSubscribe(accountId, onMessage) {
    if (!this.client || !this.client.connected) {
      console.warn(`WebSocket not connected, queueing subscription for ${accountId}`);
      this.pendingSubscriptions.push({ accountId, onMessage });
      return;
    }

    const token = oauthService.getAccessToken();
    const topic = `/topic/accounts/${accountId}/operations`;
    console.log(`Subscribing to: ${topic}`);
    
    /** @type {WebSocketSubscribeOptions} */
    const headers = {
      Authorization: `Bearer ${token}`
    };
    
    const subscription = this.client.subscribe(topic, (message) => {
      console.log(`Received message for account ${accountId}:`, message.body);
      try {
        /** @type {WebSocketOperation} */
        const operation = JSON.parse(message.body);
        onMessage(operation);
      } catch (e) {
        console.error('Error parsing operation:', e);
      }
    }, headers);

    this.subscriptions.set(accountId, subscription);
    console.log(`Subscribed to account ${accountId}`);
  }

  /**
   * @param {string} accountId
   * @param {OnMessageCallback} onMessage
   */
  subscribeToAccount(accountId, onMessage) {
    this.doSubscribe(accountId, onMessage);
  }

  /**
   * @param {string} accountId
   */
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

/** @type {WebSocketService} */
export const websocketService = new WebSocketService();