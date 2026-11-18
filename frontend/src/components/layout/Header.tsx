import React from 'react';
import { useTranslation } from 'react-i18next';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { Globe, Sparkles, LogIn, UserPlus, User as UserIcon, LogOut, BookOpen, ShieldCheck } from 'lucide-react';

export const Header: React.FC = () => {
  const { i18n } = useTranslation();
  const { user, isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();

  const toggleLanguage = () => {
    const nextLang = i18n.language === 'en' ? 'ta' : 'en';
    i18n.changeLanguage(nextLang);
  };

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  const isPrivilegedRole = user && user.role !== 'LEARNER' && user.role !== 'GUEST';

  return (
    <header className="border-b border-slate-800 bg-slate-900/60 backdrop-blur-md sticky top-0 z-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
        <div className="flex items-center space-x-6">
          <Link to="/" className="flex items-center space-x-3 group">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-amber-600 via-rose-700 to-amber-500 flex items-center justify-center shadow-lg shadow-amber-900/20 group-hover:scale-105 transition-transform">
              <Sparkles className="w-5 h-5 text-amber-100" />
            </div>
            <div>
              <h1 className="text-xl font-bold tracking-tight text-transparent bg-clip-text bg-gradient-to-r from-amber-200 via-amber-400 to-rose-300">
                MARABAGAM
              </h1>
              <p className="text-xs text-amber-500/80 font-medium">மரபகம் • Tamil Digital Heritage</p>
            </div>
          </Link>

          {/* Main Navigation Links */}
          <nav className="hidden md:flex items-center space-x-4 pl-4 border-l border-slate-800">
            <Link
              to="/art-forms"
              className="flex items-center space-x-1.5 px-3 py-1.5 rounded-lg text-slate-300 hover:text-amber-300 hover:bg-slate-800/60 text-xs font-semibold transition"
            >
              <BookOpen className="w-4 h-4 text-amber-400" />
              <span>கலைகள் / Art Directory</span>
            </Link>

            <Link
              to="/courses"
              className="flex items-center space-x-1.5 px-3 py-1.5 rounded-lg text-slate-300 hover:text-amber-300 hover:bg-slate-800/60 text-xs font-semibold transition"
            >
              <BookOpen className="w-4 h-4 text-amber-400" />
              <span>பாடங்கள் / Courses</span>
            </Link>

            <Link
              to="/ai-guide"
              className="flex items-center space-x-1.5 px-3 py-1.5 rounded-lg text-amber-300 bg-amber-500/10 border border-amber-500/20 text-xs font-semibold hover:bg-amber-500/20 transition"
            >
              <Sparkles className="w-4 h-4 text-amber-400" />
              <span>AI வழிகாட்டி / AI Guide</span>
            </Link>

            <Link
              to="/heritage-map"
              className="flex items-center space-x-1.5 px-3 py-1.5 rounded-lg text-slate-300 hover:text-amber-300 hover:bg-slate-800/60 text-xs font-semibold transition"
            >
              <Globe className="w-4 h-4 text-amber-400" />
              <span>வரைபடம் / Heritage Map</span>
            </Link>

            <Link
              to="/movement-coach"
              className="flex items-center space-x-1.5 px-3 py-1.5 rounded-lg text-amber-300 bg-amber-500/10 border border-amber-500/20 text-xs font-semibold hover:bg-amber-500/20 transition"
            >
              <Sparkles className="w-4 h-4 text-amber-400" />
              <span>பயிற்சியாளர் / Movement Coach</span>
            </Link>

            <Link
              to="/leaderboard"
              className="flex items-center space-x-1.5 px-3 py-1.5 rounded-lg text-slate-300 hover:text-amber-300 hover:bg-slate-800/60 text-xs font-semibold transition"
            >
              <Sparkles className="w-4 h-4 text-amber-400" />
              <span>முன்னணி / Leaderboard</span>
            </Link>

            {isAuthenticated && (
              <>
                <Link
                  to="/my-achievements"
                  className="flex items-center space-x-1.5 px-3 py-1.5 rounded-lg text-slate-300 hover:text-amber-300 hover:bg-slate-800/60 text-xs font-semibold transition"
                >
                  <span>சாதனைகள் / Achievements</span>
                </Link>
                <Link
                  to="/certificates"
                  className="flex items-center space-x-1.5 px-3 py-1.5 rounded-lg text-slate-300 hover:text-amber-300 hover:bg-slate-800/60 text-xs font-semibold transition"
                >
                  <span>சான்றிதழ்கள் / Certificates</span>
                </Link>
                <Link
                  to="/my-learning"
                  className="flex items-center space-x-1.5 px-3 py-1.5 rounded-lg text-slate-300 hover:text-amber-300 hover:bg-slate-800/60 text-xs font-semibold transition"
                >
                  <span>எனது பாடங்கள் / My Learning</span>
                </Link>
              </>
            )}

            {isPrivilegedRole && (
              <>
                <Link
                  to="/admin/art-forms"
                  className="flex items-center space-x-1.5 px-3 py-1.5 rounded-lg text-amber-300 bg-amber-500/10 border border-amber-500/20 text-xs font-semibold hover:bg-amber-500/20 transition"
                >
                  <ShieldCheck className="w-4 h-4 text-amber-400" />
                  <span>Art Admin</span>
                </Link>
                <Link
                  to="/admin/courses"
                  className="flex items-center space-x-1.5 px-3 py-1.5 rounded-lg text-amber-300 bg-amber-500/10 border border-amber-500/20 text-xs font-semibold hover:bg-amber-500/20 transition"
                >
                  <ShieldCheck className="w-4 h-4 text-amber-400" />
                  <span>Course Admin</span>
                </Link>
              </>
            )}
          </nav>
        </div>

        <div className="flex items-center space-x-3">
          <button
            onClick={toggleLanguage}
            className="flex items-center space-x-1.5 px-3 py-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 text-xs font-semibold transition border border-slate-700"
            aria-label="Toggle language"
          >
            <Globe className="w-4 h-4 text-amber-400" />
            <span>{i18n.language === 'en' ? 'தமிழ் (TA)' : 'English (EN)'}</span>
          </button>

          {isAuthenticated && user ? (
            <div className="flex items-center space-x-3 border-l border-slate-800 pl-3">
              <Link
                to="/profile"
                className="flex items-center space-x-2 px-3 py-1.5 rounded-lg bg-amber-500/10 hover:bg-amber-500/20 border border-amber-500/30 text-amber-300 text-xs font-semibold transition"
              >
                <UserIcon className="w-4 h-4 text-amber-400" />
                <span className="max-w-[100px] truncate">{user.displayName || user.fullName}</span>
                <span className="hidden sm:inline-block px-1.5 py-0.5 rounded bg-amber-500/20 text-[10px] text-amber-200">
                  {user.role}
                </span>
              </Link>

              <button
                onClick={handleLogout}
                className="flex items-center space-x-1 px-3 py-1.5 rounded-lg bg-slate-800 hover:bg-red-950/60 hover:text-red-300 text-slate-400 text-xs font-medium border border-slate-700 transition"
                title="Log Out"
              >
                <LogOut className="w-4 h-4" />
                <span className="hidden sm:inline">வெளியேறு</span>
              </button>
            </div>
          ) : (
            <div className="flex items-center space-x-2 border-l border-slate-800 pl-3">
              <Link
                to="/login"
                className="flex items-center space-x-1 px-3 py-1.5 rounded-lg text-slate-300 hover:text-amber-300 text-xs font-semibold transition"
              >
                <LogIn className="w-4 h-4" />
                <span>உள்நுழைவு</span>
              </Link>

              <Link
                to="/register"
                className="flex items-center space-x-1 px-3.5 py-1.5 rounded-lg bg-amber-600 hover:bg-amber-500 text-slate-950 text-xs font-bold transition shadow-md shadow-amber-600/20"
              >
                <UserPlus className="w-4 h-4" />
                <span>பதிவு</span>
              </Link>
            </div>
          )}
        </div>
      </div>
    </header>
  );
};
