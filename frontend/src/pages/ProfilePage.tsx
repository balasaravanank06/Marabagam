import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { User, Shield, KeyRound, Globe, Save, CheckCircle2, AlertCircle, Calendar } from 'lucide-react';

export const ProfilePage: React.FC = () => {
  const { user, updateProfile, changePassword } = useAuth();

  const [displayName, setDisplayName] = useState(user?.displayName || '');
  const [preferredLanguage, setPreferredLanguage] = useState<'ta' | 'en'>(user?.preferredLanguage || 'ta');

  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmNewPassword, setConfirmNewPassword] = useState('');

  const [profileSuccess, setProfileSuccess] = useState<string | null>(null);
  const [profileError, setProfileError] = useState<string | null>(null);
  const [profileSubmitting, setProfileSubmitting] = useState(false);

  const [passwordSuccess, setPasswordSuccess] = useState<string | null>(null);
  const [passwordError, setPasswordError] = useState<string | null>(null);
  const [passwordSubmitting, setPasswordSubmitting] = useState(false);

  if (!user) return null;

  const handleUpdateProfile = async (e: React.FormEvent) => {
    e.preventDefault();
    setProfileSuccess(null);
    setProfileError(null);
    setProfileSubmitting(true);

    try {
      await updateProfile({
        displayName,
        preferredLanguage,
      });
      setProfileSuccess('சுயவிவரம் புதுப்பிக்கப்பட்டது! / Profile updated successfully!');
    } catch (err: any) {
      setProfileError(err.response?.data?.message || 'சுயவிவரப் புதுப்பிப்பு தோல்வியடைந்தது.');
    } finally {
      setProfileSubmitting(false);
    }
  };

  const handleChangePassword = async (e: React.FormEvent) => {
    e.preventDefault();
    setPasswordSuccess(null);
    setPasswordError(null);

    if (newPassword !== confirmNewPassword) {
      setPasswordError('புதிய கடவுச்சொற்கள் பொருந்தவில்லை / New passwords do not match');
      return;
    }

    if (newPassword.length < 8) {
      setPasswordError('புதிய கடவுச்சொல் 8 எழுத்துக்கள் கொண்டிருக்க வேண்டும்.');
      return;
    }

    setPasswordSubmitting(true);

    try {
      await changePassword({
        currentPassword,
        newPassword,
      });
      setPasswordSuccess('கடவுச்சொல் மாற்றப்பட்டது. மீண்டும் உள்நுழையவும்...');
    } catch (err: any) {
      setPasswordError(err.response?.data?.message || 'கடவுச்சொல் மாற்றம் தோல்வியடைந்தது.');
    } finally {
      setPasswordSubmitting(false);
    }
  };

  return (
    <div className="max-w-4xl mx-auto my-8 space-y-8">
      {/* Header Profile Summary Banner */}
      <div className="p-8 rounded-2xl bg-gradient-to-r from-amber-950/60 via-slate-900 to-amber-900/30 border border-amber-500/20 shadow-2xl backdrop-blur-md flex flex-col md:flex-row items-center md:items-start justify-between gap-6">
        <div className="flex items-center space-x-6">
          <div className="w-20 h-20 rounded-2xl bg-gradient-to-br from-amber-500 to-amber-700 text-slate-950 font-black text-3xl flex items-center justify-center shadow-xl">
            {user.displayName ? user.displayName.charAt(0).toUpperCase() : user.email.charAt(0).toUpperCase()}
          </div>
          <div className="space-y-1 text-center md:text-left">
            <h1 className="text-2xl sm:text-3xl font-extrabold text-white">{user.displayName || user.fullName}</h1>
            <p className="text-sm text-slate-400">{user.email}</p>
            <div className="pt-2 flex flex-wrap gap-2 justify-center md:justify-start">
              <span className="px-3 py-1 rounded-full bg-amber-500/20 border border-amber-500/30 text-amber-300 text-xs font-semibold flex items-center space-x-1">
                <Shield className="w-3.5 h-3.5" />
                <span>ROLE: {user.role}</span>
              </span>
              <span className="px-3 py-1 rounded-full bg-emerald-500/20 border border-emerald-500/30 text-emerald-300 text-xs font-semibold">
                STATUS: {user.accountStatus}
              </span>
            </div>
          </div>
        </div>

        <div className="text-xs text-slate-400 bg-slate-950/60 p-4 rounded-xl border border-slate-800 space-y-1">
          <div className="flex items-center space-x-2">
            <Calendar className="w-4 h-4 text-amber-400" />
            <span>Joined: {new Date(user.createdAt).toLocaleDateString()}</span>
          </div>
          <div>User UUID: <span className="font-mono text-slate-300">{user.id.substring(0, 13)}...</span></div>
        </div>
      </div>

      {/* Main Grid: Profile Edit & Change Password */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
        {/* Profile Info Form */}
        <div className="p-6 rounded-2xl bg-slate-900/60 border border-slate-800 shadow-xl space-y-6">
          <div className="flex items-center space-x-3 border-b border-slate-800 pb-4">
            <User className="w-6 h-6 text-amber-400" />
            <h2 className="text-xl font-bold text-white">சுயவிவரத் தகவல்கள் / Edit Profile</h2>
          </div>

          {profileSuccess && (
            <div className="p-4 rounded-xl bg-emerald-950/50 border border-emerald-500/40 text-emerald-300 text-sm flex items-center space-x-2">
              <CheckCircle2 className="w-5 h-5 text-emerald-400 flex-shrink-0" />
              <span>{profileSuccess}</span>
            </div>
          )}

          {profileError && (
            <div className="p-4 rounded-xl bg-red-950/50 border border-red-500/40 text-red-300 text-sm flex items-center space-x-2">
              <AlertCircle className="w-5 h-5 text-red-400 flex-shrink-0" />
              <span>{profileError}</span>
            </div>
          )}

          <form onSubmit={handleUpdateProfile} className="space-y-4">
            <div className="space-y-1.5">
              <label className="block text-xs font-semibold text-amber-200/90 uppercase tracking-wider">
                திரைப்பெயர் / Display Name
              </label>
              <input
                type="text"
                value={displayName}
                onChange={(e) => setDisplayName(e.target.value)}
                className="w-full px-4 py-3 bg-slate-950/70 border border-slate-800 rounded-xl text-white focus:outline-none focus:border-amber-500"
              />
            </div>

            <div className="space-y-1.5">
              <label className="block text-xs font-semibold text-amber-200/90 uppercase tracking-wider">
                விருப்ப மொழி / Preferred Language
              </label>
              <div className="relative">
                <Globe className="absolute left-3.5 top-3.5 w-5 h-5 text-slate-400" />
                <select
                  value={preferredLanguage}
                  onChange={(e) => setPreferredLanguage(e.target.value as 'ta' | 'en')}
                  className="w-full pl-11 pr-4 py-3 bg-slate-950/70 border border-slate-800 rounded-xl text-white focus:outline-none focus:border-amber-500"
                >
                  <option value="ta">தமிழ் (Tamil)</option>
                  <option value="en">English</option>
                </select>
              </div>
            </div>

            <button
              type="submit"
              disabled={profileSubmitting}
              className="w-full py-3 px-4 rounded-xl bg-amber-600 hover:bg-amber-500 text-slate-950 font-bold transition-all flex items-center justify-center space-x-2 disabled:opacity-50"
            >
              <Save className="w-5 h-5" />
              <span>சேமி / Save Changes</span>
            </button>
          </form>
        </div>

        {/* Change Password Form */}
        <div className="p-6 rounded-2xl bg-slate-900/60 border border-slate-800 shadow-xl space-y-6">
          <div className="flex items-center space-x-3 border-b border-slate-800 pb-4">
            <KeyRound className="w-6 h-6 text-amber-400" />
            <h2 className="text-xl font-bold text-white">கடவுச்சொல் மாற்றம் / Change Password</h2>
          </div>

          {passwordSuccess && (
            <div className="p-4 rounded-xl bg-emerald-950/50 border border-emerald-500/40 text-emerald-300 text-sm flex items-center space-x-2">
              <CheckCircle2 className="w-5 h-5 text-emerald-400 flex-shrink-0" />
              <span>{passwordSuccess}</span>
            </div>
          )}

          {passwordError && (
            <div className="p-4 rounded-xl bg-red-950/50 border border-red-500/40 text-red-300 text-sm flex items-center space-x-2">
              <AlertCircle className="w-5 h-5 text-red-400 flex-shrink-0" />
              <span>{passwordError}</span>
            </div>
          )}

          <form onSubmit={handleChangePassword} className="space-y-4">
            <div className="space-y-1.5">
              <label className="block text-xs font-semibold text-amber-200/90 uppercase tracking-wider">
                தற்போதைய கடவுச்சொல் / Current Password
              </label>
              <input
                type="password"
                required
                value={currentPassword}
                onChange={(e) => setCurrentPassword(e.target.value)}
                placeholder="••••••••••••"
                className="w-full px-4 py-3 bg-slate-950/70 border border-slate-800 rounded-xl text-white focus:outline-none focus:border-amber-500"
              />
            </div>

            <div className="space-y-1.5">
              <label className="block text-xs font-semibold text-amber-200/90 uppercase tracking-wider">
                புதிய கடவுச்சொல் / New Password
              </label>
              <input
                type="password"
                required
                minLength={8}
                value={newPassword}
                onChange={(e) => setNewPassword(e.target.value)}
                placeholder="••••••••••••"
                className="w-full px-4 py-3 bg-slate-950/70 border border-slate-800 rounded-xl text-white focus:outline-none focus:border-amber-500"
              />
            </div>

            <div className="space-y-1.5">
              <label className="block text-xs font-semibold text-amber-200/90 uppercase tracking-wider">
                புதிய கடவுச்சொல் உறுதிப்படுத்தல் / Confirm New Password
              </label>
              <input
                type="password"
                required
                value={confirmNewPassword}
                onChange={(e) => setConfirmNewPassword(e.target.value)}
                placeholder="••••••••••••"
                className="w-full px-4 py-3 bg-slate-950/70 border border-slate-800 rounded-xl text-white focus:outline-none focus:border-amber-500"
              />
            </div>

            <button
              type="submit"
              disabled={passwordSubmitting}
              className="w-full py-3 px-4 rounded-xl bg-rose-600 hover:bg-rose-500 text-white font-bold transition-all flex items-center justify-center space-x-2 disabled:opacity-50"
            >
              <KeyRound className="w-5 h-5" />
              <span>கடவுச்சொல்லை மாற்று / Update Password</span>
            </button>
          </form>
        </div>
      </div>
    </div>
  );
};
