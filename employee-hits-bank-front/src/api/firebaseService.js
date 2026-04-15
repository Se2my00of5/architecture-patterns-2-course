import { initializeApp } from 'firebase/app';
import { getMessaging, getToken, onMessage, deleteToken } from 'firebase/messaging';
import { toast } from 'react-toastify';
import notificationClient from './notificationClient';
import { withRetry } from './retry';
import { userServiceCB } from './circuitBreaker';

const firebaseConfig = {
  apiKey: "AIzaSyBP7SCInRd5kBpL01vFt8YEmAbWHbqdlAk",
  authDomain: "hitsbank-d82e8.firebaseapp.com",
  projectId: "hitsbank-d82e8",
  storageBucket: "hitsbank-d82e8.firebasestorage.app",
  messagingSenderId: "69483825086",
  appId: "1:69483825086:web:76ff7f8fe60d94058e1bef"
};

class FirebaseService {
  constructor() {
    this.messaging = null;
    this.token = null;
    this.isSupported = false;
  }

  async init() {
    if (!('Notification' in window)) {
      console.warn('This browser does not support notifications');
      return false;
    }

    if (!('serviceWorker' in navigator)) {
      console.warn('Service Worker not supported');
      return false;
    }

    try {
      const app = initializeApp(firebaseConfig);
      this.messaging = getMessaging(app);
      this.isSupported = true;
      
      onMessage(this.messaging, (payload) => {
        console.log('Foreground message:', payload);
        const title = payload.notification?.title || payload.data?.title || 'Новая операция';
        const body = payload.notification?.body || payload.data?.body || '';
        toast.info(`${title}: ${body}`, { autoClose: 5000 });
      });
      
      return true;
    } catch (error) {
      console.error('Firebase init failed:', error);
      return false;
    }
  }

  async enable() {
    const inited = await this.init();
    if (!inited) return false;
    
    try {
      if (!navigator.serviceWorker.controller) {
        await navigator.serviceWorker.register('/firebase-messaging-sw.js');
        await navigator.serviceWorker.ready;
      }

      const permission = await Notification.requestPermission();
      if (permission !== 'granted') {
        toast.error('Разрешение не получено');
        return false;
      }

      this.token = await getToken(this.messaging, {
        vapidKey: process.env.REACT_APP_FIREBASE_VAPID_KEY,
      });
      console.log('FCM Token:', this.token);

      const makeRequest = () => notificationClient.post('/api/push/tokens', { token: this.token });
      
      const requestWithCB = () => userServiceCB.call(makeRequest);
      
      await withRetry(requestWithCB, {
        maxRetries: 3,
        initialDelay: 1000,
        shouldRetry: (error) => {
          return error.response?.status >= 500 || error.code === 'ERR_NETWORK';
        }
      });
      
      localStorage.setItem('fcm_token', this.token);
      return true;
    } catch (error) {
      console.error('Enable failed:', error);
      toast.error('Не удалось включить уведомления');
      return false;
    }
  }

  async disable() {
    if (!this.token) {
      this.token = localStorage.getItem('fcm_token');
    }
    
    if (this.token && this.messaging) {
      try {
        const unregisterWithRetry = () => withRetry(
          () => notificationClient.delete('/api/push/tokens', { data: { token: this.token } }),
          {
            maxRetries: 3,
            initialDelay: 1000,
            shouldRetry: (error) => {
              return error.response?.status >= 500 || error.code === 'ERR_NETWORK';
            }
          }
        );

        await userServiceCB.call(unregisterWithRetry);
        
        if (this.messaging) {
          try {
            await deleteToken(this.messaging);
          } catch (e) {
            console.warn('Failed to delete Firebase token:', e);
          }
        }
        
        localStorage.removeItem('fcm_token');
        this.token = null;
        return true;
      } catch (error) {
        console.error('Disable failed:', error);
        toast.error('Не удалось отключить уведомления');
        return false;
      }
    }
    return true;
  }

  async cleanup() {
    if (this.token) {
      await this.disable();
    }
  }
}

export const firebaseService = new FirebaseService();