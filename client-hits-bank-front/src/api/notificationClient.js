import axios from 'axios';
import { oauthService } from './oauthService';

const notificationClient = axios.create({
  baseURL: 'http://localhost:1116'
});

notificationClient.interceptors.request.use(async (config) => {
  const token = await oauthService.getAccessToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export default notificationClient;