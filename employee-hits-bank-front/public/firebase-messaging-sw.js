importScripts('https://www.gstatic.com/firebasejs/10.13.2/firebase-app-compat.js');
importScripts('https://www.gstatic.com/firebasejs/10.13.2/firebase-messaging-compat.js');

firebase.initializeApp({
  apiKey: "AIzaSyBP7SCInRd5kBpL01vFt8YEmAbWHbqdlAk",
  authDomain: "hitsbank-d82e8.firebaseapp.com",
  projectId: "hitsbank-d82e8",
  storageBucket: "hitsbank-d82e8.firebasestorage.app",
  messagingSenderId: "69483825086",
  appId: "1:69483825086:web:76ff7f8fe60d94058e1bef"
});

const messaging = firebase.messaging();

messaging.onBackgroundMessage((payload) => {
  console.log('Background message:', payload);
  const title = payload.notification?.title || 'Новая операция';
  const options = {
    body: payload.notification?.body || '',
    icon: '/favicon.ico',
    data: payload.data,
  };
  self.registration.showNotification(title, options);
});