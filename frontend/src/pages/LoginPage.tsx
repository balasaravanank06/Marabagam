import React, { useState, useRef } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { KeyRound, Mail, LogIn, AlertCircle } from 'lucide-react';

export const LoginPage: React.FC = () => {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const from = (location.state as { from?: { pathname: string } })?.from?.pathname || '/';

  const usernameInputRef = useRef<HTMLInputElement>(null);
  const passwordInputRef = useRef<HTMLInputElement>(null);

  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (submitting) return;

    // Read directly from the actual submitted HTML form controls / refs as source of truth
    const formElement = e.currentTarget;
    const formData = new FormData(formElement);

    const rawIdentifier = String(formData.get('usernameOrEmail') ?? usernameInputRef.current?.value ?? '');
    const rawPassword = String(formData.get('password') ?? passwordInputRef.current?.value ?? '');

    const finalIdentifier = rawIdentifier.trim();
    const finalPassword = rawPassword;

    // Safe Development Debug Logging (NEVER logging plaintext password)
    if (process.env.NODE_ENV !== 'production') {
      console.debug('Login submit extracted values:', {
        hasIdentifier: finalIdentifier.length > 0,
        hasPassword: finalPassword.length > 0,
        identifierLength: finalIdentifier.length,
        passwordLength: finalPassword.length,
      });
    }

    if (!finalIdentifier || !finalPassword) {
      if (!finalIdentifier && !finalPassword) {
        setError('மின்னஞ்சல்/பயனர் பெயர் மற்றும் கடவுச்சொல் தேவை / Email or username and password are required.');
      } else if (!finalIdentifier) {
        setError('மின்னஞ்சல் அல்லது பயனர் பெயர் தேவை / Email or username is required.');
      } else {
        setError('கடவுச்சொல் தேவை / Password is required.');
      }
      return;
    }

    setError(null);
    setSubmitting(true);

    try {
      await login({
        usernameOrEmail: finalIdentifier,
        password: finalPassword,
      });
      navigate(from, { replace: true });
    } catch (err: any) {
      const serverMessage = err.response?.data?.message;
      if (serverMessage?.includes('Invalid username/email or password')) {
        setError('தவறான மின்னஞ்சல்/பயனர் பெயர் அல்லது கடவுச்சொல் / Invalid email/username or password.');
      } else if (serverMessage?.includes('not active') || serverMessage?.includes('disabled')) {
        setError('உங்கள் கணக்கு செயலில் இல்லை. ஆதரவைத் தொடர்பு கொள்ளவும் / Your account is not active. Please contact support.');
      } else if (serverMessage) {
        setError(serverMessage);
      } else {
        setError('உள்நுழைவு தோல்வியடைந்தது. தகவல்களை சரிபார்க்கவும் / Login could not be completed. Please check credentials.');
      }
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="max-w-md mx-auto my-12 p-8 rounded-2xl bg-gradient-to-b from-amber-950/40 via-slate-900/80 to-slate-950 border border-amber-500/20 shadow-2xl backdrop-blur-md">
      <div className="text-center space-y-2 mb-8">
        <div className="inline-flex items-center justify-center w-14 h-14 rounded-2xl bg-amber-500/10 border border-amber-500/30 text-amber-400">
          <LogIn className="w-7 h-7" />
        </div>
        <h2 className="text-2xl font-bold text-white tracking-wide">
          மரபகம் உள்நுழைவு / Login to Marabagam
        </h2>
        <p className="text-sm text-slate-400">
          Sign in to access traditional Tamil arts digital learning
        </p>
      </div>

      {error && (
        <div className="mb-6 p-4 rounded-xl bg-red-950/50 border border-red-500/40 text-red-300 text-sm flex items-start space-x-3">
          <AlertCircle className="w-5 h-5 text-red-400 flex-shrink-0 mt-0.5" />
          <span>{error}</span>
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-6">
        <div className="space-y-2">
          <label htmlFor="usernameOrEmail" className="block text-xs font-semibold text-amber-200/90 uppercase tracking-wider">
            மின்னஞ்சல் அல்லது பயனர் பெயர் / Email or Username
          </label>
          <div className="relative">
            <Mail className="absolute left-3.5 top-3.5 w-5 h-5 text-slate-400" />
            <input
              ref={usernameInputRef}
              type="text"
              id="usernameOrEmail"
              name="usernameOrEmail"
              autoComplete="username"
              required
              defaultValue=""
              placeholder="e.g. learner@marabagam.org or learner1"
              className="w-full pl-11 pr-4 py-3 bg-slate-950/70 border border-slate-800 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:border-amber-500 focus:ring-1 focus:ring-amber-500 transition-colors"
            />
          </div>
        </div>

        <div className="space-y-2">
          <label htmlFor="password" className="block text-xs font-semibold text-amber-200/90 uppercase tracking-wider">
            கடவுச்சொல் / Password
          </label>
          <div className="relative">
            <KeyRound className="absolute left-3.5 top-3.5 w-5 h-5 text-slate-400" />
            <input
              ref={passwordInputRef}
              type="password"
              id="password"
              name="password"
              autoComplete="current-password"
              required
              defaultValue=""
              placeholder="••••••••••••"
              className="w-full pl-11 pr-4 py-3 bg-slate-950/70 border border-slate-800 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:border-amber-500 focus:ring-1 focus:ring-amber-500 transition-colors"
            />
          </div>
        </div>

        <button
          type="submit"
          disabled={submitting}
          className="w-full py-3.5 px-4 rounded-xl bg-gradient-to-r from-amber-600 via-amber-500 to-amber-600 text-slate-950 font-bold hover:brightness-110 active:scale-[0.99] transition-all shadow-lg shadow-amber-500/10 flex items-center justify-center space-x-2 disabled:opacity-50"
        >
          {submitting ? (
            <div className="w-5 h-5 border-2 border-slate-950 border-t-transparent rounded-full animate-spin" />
          ) : (
            <>
              <LogIn className="w-5 h-5" />
              <span>உள்நுழைக / Sign In</span>
            </>
          )}
        </button>
      </form>

      <div className="mt-8 pt-6 border-t border-slate-800/80 text-center text-sm text-slate-400">
        புதிய பயனரா? / Don't have an account?{' '}
        <Link to="/register" className="font-semibold text-amber-400 hover:underline ml-1">
          கணக்கு தொடங்குக / Register
        </Link>
      </div>
    </div>
  );
};

export default LoginPage;


