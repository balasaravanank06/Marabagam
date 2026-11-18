import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { Role } from '../../types/auth';

interface ProtectedRouteProps {
  children: React.ReactNode;
  allowedRoles?: Role[];
}

export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children, allowedRoles }) => {
  const { user, isAuthenticated, isLoading } = useAuth();
  const location = useLocation();

  if (isLoading) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[60vh]">
        <div className="w-12 h-12 border-4 border-amber-600 border-t-transparent rounded-full animate-spin"></div>
        <p className="mt-4 text-amber-100 font-medium animate-pulse">ஏற்றுகிறது... / Loading Marabagam Identity...</p>
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  if (allowedRoles && user && !allowedRoles.includes(user.role)) {
    return (
      <div className="max-w-2xl mx-auto my-12 p-8 rounded-2xl bg-amber-950/40 backdrop-blur-md border border-red-500/30 text-center shadow-2xl">
        <div className="w-16 h-16 mx-auto mb-4 flex items-center justify-center rounded-full bg-red-500/20 text-red-400">
          <svg className="w-8 h-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
          </svg>
        </div>
        <h2 className="text-2xl font-bold text-red-200">அனுமதி இல்லை / Access Denied</h2>
        <p className="mt-2 text-amber-100/70">
          Your account role (<span className="text-amber-400 font-semibold">{user.role}</span>) does not have authorization to view this section.
        </p>
        <p className="mt-1 text-xs text-amber-100/50">
          உங்களின் தற்போதைய பயனர் வகைக்கு இந்த பக்கத்தை அணுக அனுமதி இல்லை.
        </p>
      </div>
    );
  }

  return <>{children}</>;
};
