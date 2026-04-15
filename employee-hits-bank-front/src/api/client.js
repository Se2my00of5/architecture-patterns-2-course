import axios from 'axios';
import { oauthService } from './oauthService';
import { withRetry } from './retry';
import { userServiceCB, coreServiceCB, creditServiceCB } from './circuitBreaker';
import { telemetry } from './telemetryClient';

const generateIdempotencyKey = () => {
  return crypto.randomUUID();
};

const apiClient = axios.create();

function getCircuitBreaker(url) {
  if (url.includes('localhost:1115')) return userServiceCB;
  if (url.includes('localhost:1111')) return coreServiceCB;
  if (url.includes('localhost:5005')) return creditServiceCB;
  return null;
}

apiClient.interceptors.request.use(async (config) => {
  const token = await oauthService.ensureValidToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  
  if (['post', 'put', 'patch', 'delete'].includes(config.method?.toLowerCase())) {
    config.headers['Idempotency-Key'] = generateIdempotencyKey();
  }

  config.timeout = 10000;
  
  config.metadata = { startTime: Date.now() };
  return config;
});

apiClient.interceptors.response.use(
  (response) => {
    const elapsedMs = Date.now() - response.config.metadata.startTime;
    
    telemetry.trace({
      method: response.config.method?.toUpperCase(),
      path: response.config.url,
      statusCode: response.status,
      elapsedMs
    });
    
    return response;
  },
  async (error) => {
    const originalRequest = error.config;
    const startTime = originalRequest?.metadata?.startTime || 0;
    
    if (error.response?.status === 401 && !originalRequest?._retry) {
      originalRequest._retry = true;
      
      const newToken = await oauthService.refreshToken();
      if (newToken) {
        originalRequest.headers.Authorization = `Bearer ${newToken}`;
        return apiClient(originalRequest);
      } else {
        oauthService.logout();
      }
    }
    
    const elapsedMs = Date.now() - startTime;
    telemetry.trace({
      method: originalRequest?.method?.toUpperCase(),
      path: originalRequest?.url,
      statusCode: error.response?.status || 500,
      elapsedMs
    });

    telemetry.error({
      traceId: crypto.randomUUID(),
      errorMessage: error.response?.data?.message || error.message || 'Request failed',
      stackTrace: error.stack || ''
    });
    
    return Promise.reject(error);
  }
);

export async function request(config) {
  const circuitBreaker = getCircuitBreaker(config.url);
  
  const makeRequest = () => apiClient(config);
  
  const requestWithRetry = () => withRetry(makeRequest, {
    maxRetries: 3,
    initialDelay: 1000,
    shouldRetry: (error) => {
      return error.response?.status >= 500 || error.code === 'ERR_NETWORK';
    }
  });
  
  if (circuitBreaker) {
    return circuitBreaker.call(requestWithRetry);
  }
  
  return requestWithRetry();
}

apiClient.get = (url, config) => request({ ...config, method: 'get', url });
apiClient.post = (url, data, config) => request({ ...config, method: 'post', url, data });
apiClient.put = (url, data, config) => request({ ...config, method: 'put', url, data });
apiClient.delete = (url, config) => request({ ...config, method: 'delete', url });
apiClient.patch = (url, data, config) => request({ ...config, method: 'patch', url, data });

export default apiClient;