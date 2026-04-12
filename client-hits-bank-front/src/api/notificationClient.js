import axios from 'axios';
import { oauthService } from './oauthService';

const generateIdempotencyKey = () => {
  return crypto.randomUUID();
};

const notificationClient = axios.create({
  baseURL: 'http://localhost:1116'
});

notificationClient.interceptors.request.use(async (config) => {
  const token = await oauthService.getAccessToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  
  if (['post', 'put', 'patch', 'delete'].includes(config.method?.toLowerCase())) {
    config.headers['Idempotency-Key'] = generateIdempotencyKey();
  }
  
  return config;
});

notificationClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      
      const newToken = await oauthService.refreshToken();
      if (newToken) {
        originalRequest.headers.Authorization = `Bearer ${newToken}`;
        return notificationClient(originalRequest);
      }
    }
    
    return Promise.reject(error);
  }
);

export default notificationClient;