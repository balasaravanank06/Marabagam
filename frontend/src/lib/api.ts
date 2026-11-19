import axios, { AxiosError, InternalAxiosRequestConfig } from 'axios';
import { AuthResponse, AuthTokens } from '../types/auth';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api/v1';
const AI_SERVICE_URL = import.meta.env.VITE_AI_SERVICE_URL || 'http://localhost:8000';

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true, // Enables sending HttpOnly refresh_token cookie automatically
  timeout: 10000,
});

export const aiClient = axios.create({
  baseURL: AI_SERVICE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000,
});

// In-memory access token storage (fallback to sessionStorage and localStorage for tab & window resilience)
let memoryAccessToken: string | null = null;

export const getAccessToken = (): string | null => {
  return (
    memoryAccessToken ||
    sessionStorage.getItem('marabagam_access_token') ||
    localStorage.getItem('marabagam_access_token')
  );
};

export const setAuthTokens = (tokens: AuthTokens | AuthResponse | { accessToken: string } | string) => {
  const token = typeof tokens === 'string' ? tokens : tokens?.accessToken;
  if (token) {
    memoryAccessToken = token;
    sessionStorage.setItem('marabagam_access_token', token);
    localStorage.setItem('marabagam_access_token', token);
  }
};

export const clearAuthTokens = () => {
  memoryAccessToken = null;
  sessionStorage.removeItem('marabagam_access_token');
  localStorage.removeItem('marabagam_access_token');
  localStorage.removeItem('marabagam_refresh_token'); // Clean legacy keys if present
};

// Interceptor: Attach Access Token to Requests
apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = getAccessToken();
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

let isRefreshing = false;
let failedQueue: Array<{
  resolve: (token: string) => void;
  reject: (error: any) => void;
}> = [];

const processQueue = (error: any, token: string | null = null) => {
  failedQueue.forEach((promise) => {
    if (error) {
      promise.reject(error);
    } else if (token) {
      promise.resolve(token);
    }
  });
  failedQueue = [];
};

// Interceptor: Handle 401 Unauthorized & Token Refresh via HttpOnly Cookie
apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean };

    if (error.response?.status === 401 && originalRequest && !originalRequest._retry) {
      // Don't retry if login/refresh itself failed
      if (originalRequest.url?.includes('/auth/login') || originalRequest.url?.includes('/auth/refresh')) {
        return Promise.reject(error);
      }

      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        })
          .then((token) => {
            originalRequest.headers.Authorization = `Bearer ${token}`;
            return apiClient(originalRequest);
          })
          .catch((err) => Promise.reject(err));
      }

      originalRequest._retry = true;
      isRefreshing = true;

      try {
        // Post to /auth/refresh. The browser automatically sends the HttpOnly 'refreshToken' cookie.
        const response = await apiClient.post<{ success: boolean; data: AuthResponse }>('/auth/refresh');

        const authData = response.data.data;
        if (authData?.accessToken) {
          setAuthTokens(authData);
          processQueue(null, authData.accessToken);

          originalRequest.headers.Authorization = `Bearer ${authData.accessToken}`;
          return apiClient(originalRequest);
        } else {
          throw new Error('No access token in refresh response');
        }
      } catch (refreshErr) {
        processQueue(refreshErr, null);
        clearAuthTokens();
        window.dispatchEvent(new CustomEvent('marabagam:auth-expired'));
        return Promise.reject(refreshErr);
      } finally {
        isRefreshing = false;
      }
    }

    return Promise.reject(error);
  }
);
