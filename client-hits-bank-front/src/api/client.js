import axios from 'axios';
import { oauthService } from './oauthService';

const apiClient = axios.create();

apiClient.interceptors.request.use(async (config) => {
  const token = await oauthService.ensureValidToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      
      const newToken = await oauthService.refreshToken();
      if (newToken) {
        originalRequest.headers.Authorization = `Bearer ${newToken}`;
        return apiClient(originalRequest);
      }
    }
    
    return Promise.reject(error);
  }
);

export default apiClient;