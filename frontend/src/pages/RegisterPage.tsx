import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { UserPlus, Mail, User, Languages, Lock, AlertCircle, CheckCircle2 } from 'lucide-react';

export const RegisterPage: React.FC = () => {
  const { register } = useAuth();
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    email: '',
    fullName: '',
    displayName: '',
    preferredLanguage: 'ta' as 'ta' | 'en',
    password: '',
    confirmPassword: '',
  });

  const [error, setError] = useState<string | null>(null);
  const [successMsg, setSuccessMsg] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    setFormData((prev) => ({ ...prev, [e.target.name]: e.target.value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (formData.password !== formData.confirmPassword) {
      setError('கடவுச்சொற்கள் பொருந்தவில்லை / Passwords do not match');
      return;
    }

    if (formData.password.length < 8) {
      setError('கடவுச்சொல் குறைந்தது 8 எழுத்துக்கள் கொண்டிருக்க வேண்டும் / Password must be at least 8 characters');
      return;
    }

    setError(null);
    setSubmitting(true);

    try {
      const rawUsername = formData.email.split('@')[0].replace(/[^a-zA-Z0-9_]/g, '_');
      const username = rawUsername.length >= 3 ? rawUsername : `user_${Math.floor(Math.random() * 10000)}`;

      await register({
        email: formData.email.trim(),
        username: username,
        displayName: (formData.displayName || formData.fullName).trim(),
        preferredLanguage: formData.preferredLanguage,
        password: formData.password,
      });

      setSuccessMsg('பதிவு வெற்றி! உள்நுழைவு பக்கத்திற்கு மாறுகிறது... / Registration successful! Redirecting to login...');
      setTimeout(() => {
        navigate('/login');
      }, 1500);
    } catch (err: any) {
      const status = err.response?.status;
      const backendMsg = err.response?.data?.message || '';

      if (status === 409 || backendMsg.toLowerCase().includes('already exists') || backendMsg.toLowerCase().includes('email_exists')) {
        setError('இந்த மின்னஞ்சல் முகவரி ஏற்கனவே பதிவு செய்யப்பட்டுள்ளது / An account with this email address already exists.');
      } else if (backendMsg) {
        setError(backendMsg);
      } else {
        setError('பதிவை நிறைவு செய்ய முடியவில்லை. தயவுசெய்து மீண்டும் முயற்சிக்கவும் / Registration could not be completed. Please try again.');
      }
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="max-w-xl mx-auto my-12 p-8 rounded-2xl bg-gradient-to-b from-amber-950/40 via-slate-900/80 to-slate-950 border border-amber-500/20 shadow-2xl backdrop-blur-md">
      <div className="text-center space-y-2 mb-8">
        <div className="inline-flex items-center justify-center w-14 h-14 rounded-2xl bg-amber-500/10 border border-amber-500/30 text-amber-400">
          <UserPlus className="w-7 h-7" />
        </div>
        <h2 className="text-2xl font-bold text-white tracking-wide">
          மரபகம் புதிய பயனர் பதிவு / Marabagam Registration
        </h2>
        <div className="inline-flex items-center space-x-2 px-3 py-1 rounded-full bg-amber-500/10 border border-amber-500/20 text-amber-300 text-xs font-semibold">
          <span>Role Assigned: LEARNER (கற்றல் கலைஞர்)</span>
        </div>
      </div>

      {error && (
        <div className="mb-6 p-4 rounded-xl bg-red-950/50 border border-red-500/40 text-red-300 text-sm flex items-start space-x-3">
          <AlertCircle className="w-5 h-5 text-red-400 flex-shrink-0 mt-0.5" />
          <span>{error}</span>
        </div>
      )}

      {successMsg && (
        <div className="mb-6 p-4 rounded-xl bg-emerald-950/50 border border-emerald-500/40 text-emerald-300 text-sm flex items-start space-x-3">
          <CheckCircle2 className="w-5 h-5 text-emerald-400 flex-shrink-0 mt-0.5" />
          <span>{successMsg}</span>
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-5">
        <div className="space-y-1.5">
          <label className="block text-xs font-semibold text-amber-200/90 uppercase tracking-wider">
            மின்னஞ்சல் / Email Address
          </label>
          <div className="relative">
            <Mail className="absolute left-3.5 top-3.5 w-5 h-5 text-slate-400" />
            <input
              type="email"
              name="email"
              required
              value={formData.email}
              onChange={handleChange}
              placeholder="art_lover@marabagam.org"
              className="w-full pl-11 pr-4 py-3 bg-slate-950/70 border border-slate-800 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:border-amber-500 focus:ring-1 focus:ring-amber-500"
            />
          </div>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div className="space-y-1.5">
            <label className="block text-xs font-semibold text-amber-200/90 uppercase tracking-wider">
              முழு பெயர் / Full Name
            </label>
            <div className="relative">
              <User className="absolute left-3.5 top-3.5 w-5 h-5 text-slate-400" />
              <input
                type="text"
                name="fullName"
                required
                value={formData.fullName}
                onChange={handleChange}
                placeholder="Ramanathan K"
                className="w-full pl-11 pr-4 py-3 bg-slate-950/70 border border-slate-800 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:border-amber-500 focus:ring-1 focus:ring-amber-500"
              />
            </div>
          </div>

          <div className="space-y-1.5">
            <label className="block text-xs font-semibold text-amber-200/90 uppercase tracking-wider">
              திரைப்பெயர் / Display Name
            </label>
            <input
              type="text"
              name="displayName"
              value={formData.displayName}
              onChange={handleChange}
              placeholder="Raman (optional)"
              className="w-full px-4 py-3 bg-slate-950/70 border border-slate-800 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:border-amber-500 focus:ring-1 focus:ring-amber-500"
            />
          </div>
        </div>

        <div className="space-y-1.5">
          <label className="block text-xs font-semibold text-amber-200/90 uppercase tracking-wider">
            விருப்ப மொழி / Preferred Language
          </label>
          <div className="relative">
            <Languages className="absolute left-3.5 top-3.5 w-5 h-5 text-slate-400" />
            <select
              name="preferredLanguage"
              value={formData.preferredLanguage}
              onChange={handleChange}
              className="w-full pl-11 pr-4 py-3 bg-slate-950/70 border border-slate-800 rounded-xl text-white focus:outline-none focus:border-amber-500 focus:ring-1 focus:ring-amber-500"
            >
              <option value="ta">தமிழ் (Tamil)</option>
              <option value="en">English</option>
            </select>
          </div>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div className="space-y-1.5">
            <label className="block text-xs font-semibold text-amber-200/90 uppercase tracking-wider">
              கடவுச்சொல் / Password
            </label>
            <div className="relative">
              <Lock className="absolute left-3.5 top-3.5 w-5 h-5 text-slate-400" />
              <input
                type="password"
                name="password"
                required
                minLength={8}
                value={formData.password}
                onChange={handleChange}
                placeholder="••••••••••••"
                className="w-full pl-11 pr-4 py-3 bg-slate-950/70 border border-slate-800 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:border-amber-500 focus:ring-1 focus:ring-amber-500"
              />
            </div>
          </div>

          <div className="space-y-1.5">
            <label className="block text-xs font-semibold text-amber-200/90 uppercase tracking-wider">
              உறுதி செய்க / Confirm Password
            </label>
            <div className="relative">
              <Lock className="absolute left-3.5 top-3.5 w-5 h-5 text-slate-400" />
              <input
                type="password"
                name="confirmPassword"
                required
                value={formData.confirmPassword}
                onChange={handleChange}
                placeholder="••••••••••••"
                className="w-full pl-11 pr-4 py-3 bg-slate-950/70 border border-slate-800 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:border-amber-500 focus:ring-1 focus:ring-amber-500"
              />
            </div>
          </div>
        </div>

        <button
          type="submit"
          disabled={submitting}
          className="w-full mt-4 py-3.5 px-4 rounded-xl bg-gradient-to-r from-amber-600 via-amber-500 to-amber-600 text-slate-950 font-bold hover:brightness-110 active:scale-[0.99] transition-all shadow-lg shadow-amber-500/10 flex items-center justify-center space-x-2 disabled:opacity-50"
        >
          {submitting ? (
            <div className="w-5 h-5 border-2 border-slate-950 border-t-transparent rounded-full animate-spin" />
          ) : (
            <>
              <UserPlus className="w-5 h-5" />
              <span>பதிவு செய்க / Create Account</span>
            </>
          )}
        </button>
      </form>

      <div className="mt-8 pt-6 border-t border-slate-800/80 text-center text-sm text-slate-400">
        ஏற்கனவே கணக்கு உள்ளதா? / Already have an account?{' '}
        <Link to="/login" className="font-semibold text-amber-400 hover:underline ml-1">
          உள்நுழைக / Sign In
        </Link>
      </div>
    </div>
  );
};
