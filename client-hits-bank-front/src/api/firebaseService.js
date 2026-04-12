import { initializeApp } from 'firebase/app';
import { getMessaging, getToken, onMessage } from 'firebase/messaging';
import { toast } from 'react-toastify';
import notificationClient from './notificationClient';

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

  async requestPermission() {
    if (!this.isSupported) return null;

    try {
      const permission = await Notification.requestPermission();
      if (permission === 'granted') {
        this.token = await getToken(this.messaging, {
          vapidKey: process.env.REACT_APP_FIREBASE_VAPID_KEY,
        });
        console.log('FCM Token:', this.token);
        return this.token;
      }
      return null;
    } catch (error) {
      console.error('Permission error:', error);
      return null;
    }
  }

  async registerToken(token) {
    try {
      await notificationClient.post('/api/push/tokens', { token });
      console.log('Token registered');
      return true;
    } catch (error) {
      console.error('Token registration failed:', error);
      return false;
    }
  }

  async unregisterToken(token) {
    try {
      await notificationClient.delete('/api/push/tokens', { data: { token } });
      console.log('Token unregistered');
      return true;
    } catch (error) {
      console.error('Token unregister failed:', error);
      return false;
    }
  }

  async setup() {
    const inited = await this.init();
    if (!inited) return false;
    
    const token = await this.requestPermission();
    if (token) {
      await this.registerToken(token);
      return true;
    }
    return false;
  }

  async cleanup() {
    if (this.token) {
      await this.unregisterToken(this.token);
    }
  }
}

export const firebaseService = new FirebaseService();