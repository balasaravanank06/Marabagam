import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import {
  Award,
  Sparkles,
  BookOpen,
  Flame,
  Compass,
  RefreshCw,
  AlertCircle,
  CheckCircle2,
  Clock,
  Trophy,
  GraduationCap,
  CheckSquare,
  LogIn
} from 'lucide-react';
import { gamificationService } from '../services/gamificationService';
import { GamificationProfile } from '../types/gamification';
import { XpProgressBar } from '../components/gamification/XpProgressBar';
import { StreakWidget } from '../components/gamification/StreakWidget';
import { BadgeCard } from '../components/gamification/BadgeCard';

const formatActivityLabel = (activity: string, isTa: boolean): string => {
  switch (activity) {
    case 'LESSON_COMPLETED':
      return isTa ? 'பாடம் நிறைவு செய்யப்பட்டது' : 'Traditional Art Lesson Completed';
    case 'QUIZ_PASSED':
      return isTa ? 'வினாடி வினாவில் தேர்ச்சி' : 'Knowledge Quiz Passed';
    case 'PERFECT_QUIZ_BONUS':
      return isTa ? 'பிழையற்ற வினாடி வினா போனஸ்' : 'Flawless 100% Score Bonus';
    case 'COURSE_COMPLETED':
      return isTa ? 'முழு படிப்பு நிறைவு செய்யப்பட்டது' : 'Masterclass Course Completed';
    case 'MOVEMENT_PRACTICE':
      return isTa ? 'அசைவு பயிற்சி நிறைவு' : 'AI Movement Practice Session';
    case 'STREAK_BONUS':
      return isTa ? 'தொடர் பயிற்சி போனஸ்' : 'Consecutive Streak Bonus';
    default:
      return isTa ? 'கற்றல் புள்ளி வழங்கப்பட்டது' : 'XP Milestone Awarded';
  }
};

export const AchievementsPage: React.FC = () => {
  const { i18n } = useTranslation();
  const isTa = i18n.language === 'ta';

  const [profile, setProfile] = useState<GamificationProfile | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [isAuthError, setIsAuthError] = useState<boolean>(false);

  useEffect(() => {
    fetchProfile();
  }, []);

  const fetchProfile = async () => {
    setLoading(true);
    setError(null);
    setIsAuthError(false);
    try {
      const data = await gamificationService.getProfile();
      // Ensure starting defaults if null
      if (!data) {
        setProfile({
          totalXp: 0,
          level: 1,
          levelTitleEn: 'Novice Student',
          levelTitleTa: 'ஆரம்பநிலை மாணவர்',
          currentStreak: 0,
          longestStreak: 0,
          totalBadges: 0,
          badges: [],
          completedCoursesCount: 0,
          quizzesPassedCount: 0,
          leaderboardRank: null,
          recentActivities: []
        });
      } else {
        setProfile(data);
      }
    } catch (err: any) {
      if (err?.response?.status === 401) {
        setIsAuthError(true);
      }
      const errorMsg =
        err?.response?.data?.message ||
        err?.message ||
        (isTa ? 'சாதனைகளை ஏற்றுவதில் தோல்வி' : 'Failed to fetch gamification profile');
      setError(errorMsg);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-[50vh] flex flex-col items-center justify-center space-y-4">
        <div className="animate-spin rounded-full h-12 w-12 border-4 border-amber-500 border-t-transparent" />
        <p className="text-sm text-slate-400">
          {isTa ? 'சாதனைகள் ஏற்றப்படுகின்றன...' : 'Loading your achievements...'}
        </p>
      </div>
    );
  }

  return (
    <div className="space-y-8 max-w-7xl mx-auto">
      {/* Header */}
      <header className="border-b border-slate-800 pb-5 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <div className="flex items-center space-x-2 text-amber-400 text-xs font-semibold uppercase tracking-wider mb-1">
            <Sparkles className="w-4 h-4" />
            <span>{isTa ? 'பாரம்பரிய முன்னேற்றம்' : 'Heritage Progression'}</span>
          </div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-white">
            {isTa ? 'என் சாதனைகள் (My Achievements)' : 'My Achievements'}
          </h1>
          <p className="text-slate-400 text-xs sm:text-sm mt-1">
            {isTa
              ? 'உங்கள் கற்றல் புள்ளிகள் (XP), தற்போதைய நிலை, தினசரி கற்றல் தொடர் மற்றும் பாரம்பரியப் பதக்கங்களைக் கண்காணிக்கவும்.'
              : 'Track your XP, current level, daily learning streak, and earned Tamil heritage badges.'}
          </p>
        </div>

        <Link
          to="/leaderboard"
          className="inline-flex items-center space-x-2 px-4 py-2 rounded-xl bg-slate-900 hover:bg-slate-800 text-amber-300 text-xs font-semibold border border-slate-700 transition self-start sm:self-auto"
        >
          <Award className="w-4 h-4 text-amber-400" />
          <span>{isTa ? 'முன்னணிப் பட்டியல்' : 'Leaderboard'}</span>
        </Link>
      </header>

      {/* Unauthenticated / Session Expired State */}
      {isAuthError ? (
        <div className="bg-slate-900/80 border border-amber-500/30 p-8 rounded-2xl text-center space-y-4 max-w-xl mx-auto shadow-2xl backdrop-blur-md">
          <div className="w-14 h-14 rounded-full bg-amber-500/10 text-amber-400 flex items-center justify-center mx-auto border border-amber-500/30">
            <LogIn className="w-7 h-7" />
          </div>
          <div className="space-y-1">
            <h3 className="text-lg font-bold text-white">
              {isTa ? 'உள்நுழைவு தேவைப்படுகிறது' : 'Authentication Required'}
            </h3>
            <p className="text-xs sm:text-sm text-slate-300">
              {isTa
                ? 'உங்கள் சாதனைகள், கற்றல் புள்ளிகள் மற்றும் பதக்கங்களைக் காண உள்நுழையவும்.'
                : 'Please sign in to view your authentic achievements, XP streak, and earned badges.'}
            </p>
          </div>
          <div className="pt-2">
            <Link
              to="/login"
              className="inline-flex items-center space-x-2 px-6 py-2.5 rounded-xl bg-gradient-to-r from-amber-600 to-amber-500 text-slate-950 font-bold hover:brightness-110 shadow-lg shadow-amber-500/20 transition-all text-xs"
            >
              <LogIn className="w-4 h-4" />
              <span>{isTa ? 'உள்நுழைக (Sign In)' : 'Sign In to Marabagam'}</span>
            </Link>
          </div>
        </div>
      ) : error ? (
        /* Genuine API failure state with Retry */
        <div className="bg-rose-950/60 border border-rose-500/50 p-6 rounded-2xl text-center space-y-4 shadow-xl">
          <div className="w-12 h-12 rounded-full bg-rose-500/20 text-rose-400 flex items-center justify-center mx-auto">
            <AlertCircle className="w-6 h-6" />
          </div>
          <div>
            <h3 className="text-base font-bold text-rose-200">
              {isTa ? 'சாதனை விவரங்களைப் பெற முடியவில்லை' : 'Failed to fetch gamification profile'}
            </h3>
            <p className="text-xs text-rose-300/80 mt-1 max-w-md mx-auto">{error}</p>
          </div>
          <button
            onClick={fetchProfile}
            className="inline-flex items-center space-x-2 px-4 py-2 rounded-xl bg-rose-600 hover:bg-rose-500 text-white text-xs font-bold transition shadow-lg shadow-rose-900/30"
          >
            <RefreshCw className="w-3.5 h-3.5" />
            <span>{isTa ? 'மீண்டும் முயற்சிக்கவும் (Retry)' : 'Retry'}</span>
          </button>
        </div>
      ) : profile ? (
        <>
          {/* Quick Stats Ribbon */}
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
            <div className="p-4 rounded-xl bg-slate-900/60 border border-slate-800 backdrop-blur-sm flex items-center space-x-3">
              <div className="p-2.5 rounded-lg bg-amber-500/10 text-amber-400 border border-amber-500/20">
                <Trophy className="w-5 h-5" />
              </div>
              <div>
                <p className="text-[11px] text-slate-400 uppercase font-semibold tracking-wider">
                  {isTa ? 'தரவரிசை' : 'Leaderboard'}
                </p>
                <p className="text-lg font-bold text-white">
                  {profile.leaderboardRank ? `#${profile.leaderboardRank}` : isTa ? 'பட்டியலில் இல்லை' : 'Unranked'}
                </p>
              </div>
            </div>

            <div className="p-4 rounded-xl bg-slate-900/60 border border-slate-800 backdrop-blur-sm flex items-center space-x-3">
              <div className="p-2.5 rounded-lg bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
                <GraduationCap className="w-5 h-5" />
              </div>
              <div>
                <p className="text-[11px] text-slate-400 uppercase font-semibold tracking-wider">
                  {isTa ? 'முடித்த படிப்புகள்' : 'Courses Done'}
                </p>
                <p className="text-lg font-bold text-white">
                  {profile.completedCoursesCount ?? 0}
                </p>
              </div>
            </div>

            <div className="p-4 rounded-xl bg-slate-900/60 border border-slate-800 backdrop-blur-sm flex items-center space-x-3">
              <div className="p-2.5 rounded-lg bg-sky-500/10 text-sky-400 border border-sky-500/20">
                <CheckSquare className="w-5 h-5" />
              </div>
              <div>
                <p className="text-[11px] text-slate-400 uppercase font-semibold tracking-wider">
                  {isTa ? 'வினாடி வினாக்கள்' : 'Quizzes Passed'}
                </p>
                <p className="text-lg font-bold text-white">
                  {profile.quizzesPassedCount ?? 0}
                </p>
              </div>
            </div>

            <div className="p-4 rounded-xl bg-slate-900/60 border border-slate-800 backdrop-blur-sm flex items-center space-x-3">
              <div className="p-2.5 rounded-lg bg-purple-500/10 text-purple-400 border border-purple-500/20">
                <Award className="w-5 h-5" />
              </div>
              <div>
                <p className="text-[11px] text-slate-400 uppercase font-semibold tracking-wider">
                  {isTa ? 'வென்ற பதக்கங்கள்' : 'Badges Earned'}
                </p>
                <p className="text-lg font-bold text-white">
                  {profile.totalBadges} / {profile.badges?.length || 0}
                </p>
              </div>
            </div>
          </div>

          {/* New User Welcome & Guidance on earning XP / Badges */}
          {profile.totalXp === 0 && profile.totalBadges === 0 && (
            <div className="p-6 rounded-2xl bg-gradient-to-r from-amber-950/50 via-slate-900 to-amber-900/30 border border-amber-500/30 backdrop-blur-md shadow-xl space-y-4">
              <div className="flex items-center space-x-3">
                <div className="p-2.5 rounded-xl bg-amber-500/20 text-amber-300 border border-amber-500/30">
                  <Compass className="w-5 h-5" />
                </div>
                <div>
                  <h3 className="text-base font-bold text-amber-200">
                    {isTa ? 'கற்றலைத் தொடங்குங்கள் — புதிய சாதனையாளர்!' : 'Welcome, Heritage Learner! Start Earning XP'}
                  </h3>
                  <p className="text-xs text-slate-300">
                    {isTa
                      ? 'கீழே உள்ள வழிகாட்டுதல்களைப் பின்பற்றி புள்ளிகள் (XP) மற்றும் சிறப்புப் பதக்கங்களை வெல்லுங்கள்.'
                      : 'You are at Level 1 (Novice). Complete learning activities across Marabagam to earn XP and unlock authentic heritage badges.'}
                  </p>
                </div>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3 pt-1">
                <div className="p-3.5 rounded-xl bg-slate-950/60 border border-slate-800 text-xs space-y-1">
                  <div className="flex items-center space-x-1.5 text-amber-400 font-bold">
                    <BookOpen className="w-3.5 h-3.5" />
                    <span>{isTa ? 'பாடங்களை முடித்தல்' : 'Complete Lessons'}</span>
                  </div>
                  <p className="text-slate-400 text-[11px]">
                    {isTa ? '+100 XP பாட நிறைவுக்கு. முதல் படி பதக்கத்தைப் பெறுங்கள்.' : '+100 XP per lesson completed. Unlocks "First Step" badge.'}
                  </p>
                </div>

                <div className="p-3.5 rounded-xl bg-slate-950/60 border border-slate-800 text-xs space-y-1">
                  <div className="flex items-center space-x-1.5 text-emerald-400 font-bold">
                    <CheckCircle2 className="w-3.5 h-3.5" />
                    <span>{isTa ? 'வினாடி வினா தேர்ச்சி' : 'Pass Quizzes'}</span>
                  </div>
                  <p className="text-slate-400 text-[11px]">
                    {isTa ? '+150 XP 80%+ மதிப்பெண்ணுக்கு. முதல் வெற்றி பதக்கம்.' : '+150 XP for scoring 80%+. Unlocks "First Victory" badge.'}
                  </p>
                </div>

                <div className="p-3.5 rounded-xl bg-slate-950/60 border border-slate-800 text-xs space-y-1">
                  <div className="flex items-center space-x-1.5 text-sky-400 font-bold">
                    <Sparkles className="w-3.5 h-3.5" />
                    <span>{isTa ? 'அசைவுப் பயிற்சி' : 'Movement Coach'}</span>
                  </div>
                  <p className="text-slate-400 text-[11px]">
                    {isTa ? 'சிலம்பம் சுவடு பயிற்சிகளை கேமரா மூலம் சோதிக்கவும்.' : 'Practice postures with computer vision posture evaluation.'}
                  </p>
                </div>

                <div className="p-3.5 rounded-xl bg-slate-950/60 border border-slate-800 text-xs space-y-1">
                  <div className="flex items-center space-x-1.5 text-rose-400 font-bold">
                    <Flame className="w-3.5 h-3.5" />
                    <span>{isTa ? 'தினசரித் தொடர்' : 'Daily Streak'}</span>
                  </div>
                  <p className="text-slate-400 text-[11px]">
                    {isTa ? 'தொடர்ந்து 7 நாட்கள் படித்து +100 XP போனஸ் பெறவும்.' : 'Keep a 7-day streak for an extra +100 XP bonus.'}
                  </p>
                </div>
              </div>

              <div className="flex flex-wrap gap-3 pt-1">
                <Link
                  to="/courses"
                  className="px-4 py-2 rounded-xl bg-amber-600 hover:bg-amber-500 text-slate-950 text-xs font-bold transition shadow-md shadow-amber-600/20"
                >
                  {isTa ? 'பாடங்களைத் தொடங்குங்கள்' : 'Browse Courses'}
                </Link>
                <Link
                  to="/movement-coach"
                  className="px-4 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs font-semibold border border-slate-700 transition"
                >
                  {isTa ? 'அசைவுப் பயிற்சியாளர்' : 'AI Movement Coach'}
                </Link>
              </div>
            </div>
          )}

          {/* Level and Streak Summary Grid */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <XpProgressBar
              totalXp={profile.totalXp}
              level={profile.level}
              levelTitleEn={profile.levelTitleEn}
              levelTitleTa={profile.levelTitleTa}
            />
            <StreakWidget
              currentStreak={profile.currentStreak}
              longestStreak={profile.longestStreak}
            />
          </div>

          {/* Badges Section */}
          <section className="space-y-4">
            <div className="flex justify-between items-center border-b border-slate-800 pb-3">
              <h2 className="text-lg sm:text-xl font-bold text-amber-300">
                {isTa
                  ? `பதக்கங்கள் (${profile.totalBadges} / ${profile.badges?.length || 0})`
                  : `Badges (${profile.totalBadges} / ${profile.badges?.length || 0})`}
              </h2>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-4">
              {profile.badges && profile.badges.length > 0 ? (
                profile.badges.map((b) => (
                  <BadgeCard key={b.id || b.code} badge={b} language={isTa ? 'ta' : 'en'} />
                ))
              ) : (
                <div className="col-span-full p-8 text-center bg-slate-900/40 rounded-xl border border-slate-800 text-slate-500 text-xs">
                  {isTa ? 'பதக்கங்கள் எதுவும் கிடைக்கவில்லை' : 'No badges defined in catalog.'}
                </div>
              )}
            </div>
          </section>

          {/* Recent Achievement Activity */}
          {profile.recentActivities && profile.recentActivities.length > 0 && (
            <section className="space-y-4">
              <div className="flex items-center space-x-2 border-b border-slate-800 pb-3">
                <Clock className="w-5 h-5 text-amber-400" />
                <h2 className="text-lg sm:text-xl font-bold text-white">
                  {isTa ? 'சமீபத்திய சாதனைகள் (Recent Activity)' : 'Recent Achievement Activity'}
                </h2>
              </div>

              <div className="divide-y divide-slate-800 rounded-2xl bg-slate-900/40 border border-slate-800 overflow-hidden">
                {profile.recentActivities.map((act) => {
                  const formattedDate = new Date(act.createdAt).toLocaleDateString(
                    isTa ? 'ta-IN' : 'en-US',
                    { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' }
                  );
                  return (
                    <div key={act.id} className="p-4 flex items-center justify-between hover:bg-slate-800/40 transition">
                      <div className="flex items-center space-x-3">
                        <div className="w-8 h-8 rounded-lg bg-amber-500/20 text-amber-400 flex items-center justify-center font-bold text-xs">
                          +{act.amount}
                        </div>
                        <div>
                          <p className="text-sm font-semibold text-slate-200">
                            {formatActivityLabel(act.sourceActivity, isTa)}
                          </p>
                          <p className="text-[11px] text-slate-500 font-mono">{formattedDate}</p>
                        </div>
                      </div>
                      <span className="text-xs px-2.5 py-1 rounded-full bg-slate-800 text-amber-300 font-mono font-semibold">
                        +{act.amount} XP
                      </span>
                    </div>
                  );
                })}
              </div>
            </section>
          )}
        </>
      ) : null}
    </div>
  );
};
