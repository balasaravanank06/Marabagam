export type Role = 'GUEST' | 'LEARNER' | 'ARTIST' | 'INSTRUCTOR' | 'CULTURAL_EXPERT' | 'ADMIN';

export type AccountStatus = 'ACTIVE' | 'DISABLED' | 'LOCKED';

export interface User {
  id: string;
  email: string;
  fullName: string;
  displayName: string;
  preferredLanguage: 'ta' | 'en';
  role: Role;
  accountStatus: AccountStatus;
  bio?: string;
  avatarUrl?: string;
  createdAt: string;
  updatedAt: string;
}

export interface AuthTokens {
  accessToken: string;
  refreshToken?: string;
  tokenType: string;
  expiresInMs: number;
}

export interface AuthResponse {
  accessToken: string;
  tokenType: string;
  expiresInMs: number;
  refreshToken?: string;
  user: User;
}

export interface LoginRequest {
  usernameOrEmail: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  username: string;
  password: string;
  displayName: string;
  preferredLanguage?: 'ta' | 'en';
}

export interface TokenRefreshRequest {
  refreshToken: string;
}

export interface UpdateProfileRequest {
  displayName?: string;
  preferredLanguage?: 'ta' | 'en';
  bio?: string;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}

export interface ApiErrorResponse {
  code: string;
  message: string;
  details?: Record<string, string>;
  timestamp: string;
}
