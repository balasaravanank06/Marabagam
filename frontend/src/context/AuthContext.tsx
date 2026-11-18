import React, { createContext, useContext, useEffect, useState } from 'react';
import { apiClient, clearAuthTokens, getAccessToken, setAuthTokens } from '../lib/api';
import { AuthResponse, ChangePasswordRequest, LoginRequest, RegisterRequest, UpdateProfileRequest, User } from '../types/auth';

interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (data: LoginRequest) => Promise<void>;
  register: (data: RegisterRequest) => Promise<User>;
  logout: () => Promise<void>;
  updateProfile: (data: UpdateProfileRequest) => Promise<User>;
  changePassword: (data: ChangePasswordRequest) => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);

  // Initialize auth state from stored token
  useEffect(() => {
    const initializeAuth = async () => {
      const token = getAccessToken();
      if (!token) {
        setIsLoading(false);
        return;
      }

      try {
        const response = await apiClient.get<{ success: boolean; data: User }>('/users/me');
        if (response.data?.data) {
          setUser(response.data.data);
        }
      } catch (err) {
        clearAuthTokens();
        setUser(null);
      } finally {
        setIsLoading(false);
      }
    };

    initializeAuth();

    // Listen for auth-expired event from API interceptor
    const handleAuthExpired = () => {
      clearAuthTokens();
      setUser(null);
    };

    window.addEventListener('marabagam:auth-expired', handleAuthExpired);
    return () => window.removeEventListener('marabagam:auth-expired', handleAuthExpired);
  }, []);

  const login = async (credentials: LoginRequest): Promise<void> => {
    setIsLoading(true);
    try {
      const response = await apiClient.post<{ success: boolean; data: AuthResponse }>('/auth/login', credentials);
      const authData = response.data.data;
      setAuthTokens(authData);
      setUser(authData.user);
    } finally {
      setIsLoading(false);
    }
  };

  const register = async (data: RegisterRequest): Promise<User> => {
    setIsLoading(true);
    try {
      const response = await apiClient.post<{ success: boolean; data: User }>('/auth/register', data);
      return response.data.data;
    } finally {
      setIsLoading(false);
    }
  };

  const logout = async (): Promise<void> => {
    setIsLoading(true);
    try {
      await apiClient.post('/auth/logout');
    } catch {
      // Ignore logout backend error if token expired
    } finally {
      clearAuthTokens();
      setUser(null);
      setIsLoading(false);
    }
  };

  const updateProfile = async (data: UpdateProfileRequest): Promise<User> => {
    const response = await apiClient.put<{ success: boolean; data: User }>('/users/me', data);
    const updatedUser = response.data.data;
    setUser(updatedUser);
    return updatedUser;
  };

  const changePassword = async (data: ChangePasswordRequest): Promise<void> => {
    await apiClient.post('/users/me/change-password', data);
    // Token/sessions revoked on backend after password change
    await logout();
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        isAuthenticated: !!user,
        isLoading,
        login,
        register,
        logout,
        updateProfile,
        changePassword,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
