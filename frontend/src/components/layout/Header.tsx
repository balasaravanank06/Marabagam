import React, { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import {
  Globe,
  Sparkles,
  LogIn,
  UserPlus,
  User as UserIcon,
  LogOut,
  BookOpen,
  ShieldCheck,
  Menu,
  X,
  Compass,
  Award,
  Calendar,
  MessageSquare
} from 'lucide-react';

export const Header: React.FC = () => {
  const { i18n } = useTranslation();
  const { user, isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  const toggleLanguage = () => {
    const nextLang = i18n.language === 'en' ? 'ta' : 'en';
    i18n.changeLanguage(nextLang);
  };

  const handleLogout = async () => {
    setMobileMenuOpen(false);
    await logout();
    navigate('/login');
  };

  const isPrivilegedRole = user && user.role !== 'LEARNER' && user.role !== 'GUEST';

  const isActive = (path: string) => {
    return location.pathname === path;
  };

  const closeMenu = () => setMobileMenuOpen(false);

  return (
    <header className="border-b border-slate-800 bg-slate-900/80 backdrop-blur-md sticky top-0 z-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
        {/* Logo */}
        <div className="flex items-center space-x-6">
          <Link to="/" onClick={closeMenu} className="flex items-center space-x-3 group">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-amber-600 via-rose-700 to-amber-500 flex items-center justify-center shadow-lg shadow-amber-900/20 group-hover:scale-105 transition-transform">
              <Sparkles className="w-5 h-5 text-amber-100" />
            </div>
            <div>
              <h1 className="text-xl font-bold tracking-tight text-transparent bg-clip-text bg-gradient-to-r from-amber-200 via-amber-400 to-rose-300">
                MARABAGAM
              </h1>
              <p className="text-[11px] text-amber-500/80 font-medium">மரபகம் • Tamil Digital Heritage</p>
            </div>
          </Link>

          {/* Desktop Navigation Links */}
          <nav className="hidden lg:flex items-center space-x-1 pl-4 border-l border-slate-800">
            <Link
              to="/art-forms"
              className={`flex items-center space-x-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold transition ${
                isActive('/art-forms')
                  ? 'bg-amber-500/20 text-amber-300 border border-amber-500/30'
                  : 'text-slate-300 hover:text-amber-300 hover:bg-slate-800/60'
              }`}
            >
              <BookOpen className="w-3.5 h-3.5 text-amber-400" />
              <span>கலைகள் / Arts</span>
            </Link>

            <Link
              to="/courses"
              className={`flex items-center space-x-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold transition ${
                isActive('/courses')
                  ? 'bg-amber-500/20 text-amber-300 border border-amber-500/30'
                  : 'text-slate-300 hover:text-amber-300 hover:bg-slate-800/60'
              }`}
            >
              <BookOpen className="w-3.5 h-3.5 text-amber-400" />
              <span>பாடங்கள் / Courses</span>
            </Link>

            <Link
              to="/ai-guide"
              className={`flex items-center space-x-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold transition ${
                isActive('/ai-guide')
                  ? 'bg-amber-500/20 text-amber-300 border border-amber-500/30'
                  : 'text-amber-300 bg-amber-500/10 border border-amber-500/20 hover:bg-amber-500/20'
              }`}
            >
              <Sparkles className="w-3.5 h-3.5 text-amber-400" />
              <span>AI Guide</span>
            </Link>

            <Link
              to="/heritage-map"
              className={`flex items-center space-x-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold transition ${
                isActive('/heritage-map')
                  ? 'bg-amber-500/20 text-amber-300 border border-amber-500/30'
                  : 'text-slate-300 hover:text-amber-300 hover:bg-slate-800/60'
              }`}
            >
              <Globe className="w-3.5 h-3.5 text-amber-400" />
              <span>வரைபடம் / Map</span>
            </Link>

            <Link
              to="/movement-coach"
              className={`flex items-center space-x-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold transition ${
                isActive('/movement-coach')
                  ? 'bg-amber-500/20 text-amber-300 border border-amber-500/30'
                  : 'text-slate-300 hover:text-amber-300 hover:bg-slate-800/60'
              }`}
            >
              <Compass className="w-3.5 h-3.5 text-amber-400" />
              <span>பயிற்சியாளர் / Coach</span>
            </Link>

            <Link
              to="/leaderboard"
              className={`flex items-center space-x-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold transition ${
                isActive('/leaderboard')
                  ? 'bg-amber-500/20 text-amber-300 border border-amber-500/30'
                  : 'text-slate-300 hover:text-amber-300 hover:bg-slate-800/60'
              }`}
            >
              <Award className="w-3.5 h-3.5 text-amber-400" />
              <span>முன்னணி</span>
            </Link>

            {isAuthenticated && (
              <>
                <Link
                  to="/my-learning"
                  className={`flex items-center space-x-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold transition ${
                    isActive('/my-learning')
                      ? 'bg-amber-500/20 text-amber-300 border border-amber-500/30'
                      : 'text-slate-300 hover:text-amber-300 hover:bg-slate-800/60'
                  }`}
                >
                  <span>My Learning</span>
                </Link>

                <Link
                  to="/my-achievements"
                  className={`flex items-center space-x-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold transition ${
                    isActive('/my-achievements')
                      ? 'bg-amber-500/20 text-amber-300 border border-amber-500/30'
                      : 'text-slate-300 hover:text-amber-300 hover:bg-slate-800/60'
                  }`}
                >
                  <span>Achievements</span>
                </Link>

                <Link
                  to="/certificates"
                  className={`flex items-center space-x-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold transition ${
                    isActive('/certificates')
                      ? 'bg-amber-500/20 text-amber-300 border border-amber-500/30'
                      : 'text-slate-300 hover:text-amber-300 hover:bg-slate-800/60'
                  }`}
                >
                  <span>Certificates</span>
                </Link>
              </>
            )}

            {isPrivilegedRole && (
              <Link
                to="/admin/courses"
                className={`flex items-center space-x-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold transition ${
                  isActive('/admin/courses')
                    ? 'bg-amber-500/20 text-amber-300 border border-amber-500/30'
                    : 'text-amber-300 bg-amber-500/10 border border-amber-500/20 hover:bg-amber-500/20'
                }`}
              >
                <ShieldCheck className="w-3.5 h-3.5 text-amber-400" />
                <span>Admin</span>
              </Link>
            )}
          </nav>
        </div>

        {/* Right actions */}
        <div className="flex items-center space-x-2 sm:space-x-3">
          {/* Language Switcher */}
          <button
            onClick={toggleLanguage}
            className="flex items-center space-x-1.5 px-3 py-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 text-xs font-semibold transition border border-slate-700"
            aria-label="Toggle language"
            title="Switch Language"
          >
            <Globe className="w-3.5 h-3.5 text-amber-400" />
            <span>{i18n.language === 'en' ? 'தமிழ்' : 'English'}</span>
          </button>

          {/* User Auth controls */}
          {isAuthenticated && user ? (
            <div className="hidden sm:flex items-center space-x-2 border-l border-slate-800 pl-3">
              <Link
                to="/profile"
                className="flex items-center space-x-2 px-3 py-1.5 rounded-lg bg-amber-500/10 hover:bg-amber-500/20 border border-amber-500/30 text-amber-300 text-xs font-semibold transition"
              >
                <UserIcon className="w-3.5 h-3.5 text-amber-400" />
                <span className="max-w-[100px] truncate">{user.displayName || user.fullName}</span>
                <span className="px-1.5 py-0.2 rounded bg-amber-500/20 text-[10px] text-amber-200">
                  {user.role}
                </span>
              </Link>

              <button
                onClick={handleLogout}
                className="flex items-center space-x-1 px-3 py-1.5 rounded-lg bg-slate-800 hover:bg-rose-950/60 hover:text-rose-300 text-slate-400 text-xs font-medium border border-slate-700 transition"
                title="Log Out"
              >
                <LogOut className="w-3.5 h-3.5" />
                <span className="hidden md:inline">வெளியேறு</span>
              </button>
            </div>
          ) : (
            <div className="hidden sm:flex items-center space-x-2 border-l border-slate-800 pl-3">
              <Link
                to="/login"
                className="flex items-center space-x-1 px-3 py-1.5 rounded-lg text-slate-300 hover:text-amber-300 text-xs font-semibold transition"
              >
                <LogIn className="w-3.5 h-3.5" />
                <span>உள்நுழைவு</span>
              </Link>

              <Link
                to="/register"
                className="flex items-center space-x-1 px-3.5 py-1.5 rounded-lg bg-amber-600 hover:bg-amber-500 text-slate-950 text-xs font-bold transition shadow-md shadow-amber-600/20"
              >
                <UserPlus className="w-3.5 h-3.5" />
                <span>பதிவு</span>
              </Link>
            </div>
          )}

          {/* Mobile Hamburger Button */}
          <button
            onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
            className="lg:hidden p-2 rounded-lg bg-slate-800 text-slate-300 hover:text-white border border-slate-700 focus:outline-none"
            aria-label="Toggle navigation menu"
          >
            {mobileMenuOpen ? <X className="w-5 h-5 text-amber-400" /> : <Menu className="w-5 h-5" />}
          </button>
        </div>
      </div>

      {/* Mobile Navigation Drawer */}
      {mobileMenuOpen && (
        <div className="lg:hidden border-t border-slate-800 bg-slate-950/95 backdrop-blur-xl px-4 pt-3 pb-6 space-y-2 shadow-2xl">
          <div className="grid grid-cols-2 gap-2 text-xs font-semibold pb-3 border-b border-slate-800/80">
            <Link
              to="/art-forms"
              onClick={closeMenu}
              className={`p-2.5 rounded-xl border flex items-center space-x-2 ${
                isActive('/art-forms')
                  ? 'bg-amber-500/20 border-amber-500/40 text-amber-300'
                  : 'bg-slate-900/60 border-slate-800 text-slate-300'
              }`}
            >
              <BookOpen className="w-4 h-4 text-amber-400" />
              <span>கலைகள் / Arts</span>
            </Link>

            <Link
              to="/courses"
              onClick={closeMenu}
              className={`p-2.5 rounded-xl border flex items-center space-x-2 ${
                isActive('/courses')
                  ? 'bg-amber-500/20 border-amber-500/40 text-amber-300'
                  : 'bg-slate-900/60 border-slate-800 text-slate-300'
              }`}
            >
              <BookOpen className="w-4 h-4 text-amber-400" />
              <span>பாடங்கள் / Courses</span>
            </Link>

            <Link
              to="/ai-guide"
              onClick={closeMenu}
              className={`p-2.5 rounded-xl border flex items-center space-x-2 ${
                isActive('/ai-guide')
                  ? 'bg-amber-500/20 border-amber-500/40 text-amber-300'
                  : 'bg-slate-900/60 border-slate-800 text-slate-300'
              }`}
            >
              <Sparkles className="w-4 h-4 text-amber-400" />
              <span>AI Guide</span>
            </Link>

            <Link
              to="/heritage-map"
              onClick={closeMenu}
              className={`p-2.5 rounded-xl border flex items-center space-x-2 ${
                isActive('/heritage-map')
                  ? 'bg-amber-500/20 border-amber-500/40 text-amber-300'
                  : 'bg-slate-900/60 border-slate-800 text-slate-300'
              }`}
            >
              <Globe className="w-4 h-4 text-amber-400" />
              <span>வரைபடம் / Map</span>
            </Link>

            <Link
              to="/movement-coach"
              onClick={closeMenu}
              className={`p-2.5 rounded-xl border flex items-center space-x-2 ${
                isActive('/movement-coach')
                  ? 'bg-amber-500/20 border-amber-500/40 text-amber-300'
                  : 'bg-slate-900/60 border-slate-800 text-slate-300'
              }`}
            >
              <Compass className="w-4 h-4 text-amber-400" />
              <span>பயிற்சியாளர்</span>
            </Link>

            <Link
              to="/leaderboard"
              onClick={closeMenu}
              className={`p-2.5 rounded-xl border flex items-center space-x-2 ${
                isActive('/leaderboard')
                  ? 'bg-amber-500/20 border-amber-500/40 text-amber-300'
                  : 'bg-slate-900/60 border-slate-800 text-slate-300'
              }`}
            >
              <Award className="w-4 h-4 text-amber-400" />
              <span>முன்னணி</span>
            </Link>

            <Link
              to="/events"
              onClick={closeMenu}
              className={`p-2.5 rounded-xl border flex items-center space-x-2 ${
                isActive('/events')
                  ? 'bg-amber-500/20 border-amber-500/40 text-amber-300'
                  : 'bg-slate-900/60 border-slate-800 text-slate-300'
              }`}
            >
              <Calendar className="w-4 h-4 text-amber-400" />
              <span>நிகழ்வுகள் / Events</span>
            </Link>

            <Link
              to="/community"
              onClick={closeMenu}
              className={`p-2.5 rounded-xl border flex items-center space-x-2 ${
                isActive('/community')
                  ? 'bg-amber-500/20 border-amber-500/40 text-amber-300'
                  : 'bg-slate-900/60 border-slate-800 text-slate-300'
              }`}
            >
              <MessageSquare className="w-4 h-4 text-amber-400" />
              <span>மன்றம் / Forum</span>
            </Link>
          </div>

          {/* User Links on Mobile */}
          {isAuthenticated && user ? (
            <div className="space-y-2 pt-2">
              <div className="flex items-center justify-between p-2.5 rounded-xl bg-slate-900 border border-slate-800">
                <Link to="/profile" onClick={closeMenu} className="flex items-center space-x-2 text-xs text-amber-300">
                  <UserIcon className="w-4 h-4" />
                  <span className="font-semibold">{user.displayName || user.fullName}</span>
                  <span className="text-[10px] text-slate-400">({user.role})</span>
                </Link>
                <button
                  onClick={handleLogout}
                  className="px-2.5 py-1 rounded bg-rose-950/60 text-rose-300 border border-rose-500/30 text-[11px] font-bold flex items-center space-x-1"
                >
                  <LogOut className="w-3 h-3" />
                  <span>வெளியேறு</span>
                </button>
              </div>

              <div className="grid grid-cols-3 gap-2 text-xs font-semibold">
                <Link
                  to="/my-learning"
                  onClick={closeMenu}
                  className="p-2 rounded-lg bg-slate-900 text-center border border-slate-800 text-slate-300 hover:text-amber-300"
                >
                  My Learning
                </Link>
                <Link
                  to="/my-achievements"
                  onClick={closeMenu}
                  className="p-2 rounded-lg bg-slate-900 text-center border border-slate-800 text-slate-300 hover:text-amber-300"
                >
                  Achievements
                </Link>
                <Link
                  to="/certificates"
                  onClick={closeMenu}
                  className="p-2 rounded-lg bg-slate-900 text-center border border-slate-800 text-slate-300 hover:text-amber-300"
                >
                  Certificates
                </Link>
              </div>

              {isPrivilegedRole && (
                <div className="grid grid-cols-2 gap-2 text-xs font-semibold pt-1">
                  <Link
                    to="/admin/art-forms"
                    onClick={closeMenu}
                    className="p-2 rounded-lg bg-amber-500/10 text-amber-300 border border-amber-500/30 text-center"
                  >
                    Art Admin
                  </Link>
                  <Link
                    to="/admin/courses"
                    onClick={closeMenu}
                    className="p-2 rounded-lg bg-amber-500/10 text-amber-300 border border-amber-500/30 text-center"
                  >
                    Course Admin
                  </Link>
                </div>
              )}
            </div>
          ) : (
            <div className="grid grid-cols-2 gap-3 pt-2">
              <Link
                to="/login"
                onClick={closeMenu}
                className="py-2.5 text-center rounded-xl bg-slate-800 text-slate-200 text-xs font-bold border border-slate-700"
              >
                உள்நுழைவு / Login
              </Link>
              <Link
                to="/register"
                onClick={closeMenu}
                className="py-2.5 text-center rounded-xl bg-amber-600 text-slate-950 text-xs font-bold shadow-md shadow-amber-600/20"
              >
                பதிவு / Register
              </Link>
            </div>
          )}
        </div>
      )}
    </header>
  );
};
