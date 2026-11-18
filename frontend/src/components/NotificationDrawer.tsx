import React, { useEffect, useState } from 'react';
import { Bell, X, Check, Settings, ShieldAlert, Award, Calendar, MessageSquare } from 'lucide-react';

import { NotificationItem, NotificationPreference } from '../types/notification';
import { notificationService } from '../services/notificationService';

interface NotificationDrawerProps {
  isOpen: boolean;
  onClose: () => void;
  isTamil?: boolean;
}

export const NotificationDrawer: React.FC<NotificationDrawerProps> = ({ isOpen, onClose, isTamil = false }) => {
  const [notifications, setNotifications] = useState<NotificationItem[]>([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [preferences, setPreferences] = useState<NotificationPreference | null>(null);
  const [showSettings, setShowSettings] = useState(false);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (isOpen) {
      fetchNotifications();
      fetchPreferences();
    }
  }, [isOpen]);

  const fetchNotifications = async () => {
    setLoading(true);
    try {
      const res = await notificationService.getUserNotifications(0, 20);
      setNotifications(res.notifications.content);
      setUnreadCount(res.unreadCount);
    } catch (err) {
      console.error('Failed to fetch notifications', err);
    } finally {
      setLoading(false);
    }
  };

  const fetchPreferences = async () => {
    try {
      const prefs = await notificationService.getPreferences();
      setPreferences(prefs);
    } catch (err) {
      console.error('Failed to fetch preferences', err);
    }
  };

  const handleMarkRead = async (id: string) => {
    try {
      await notificationService.markAsRead(id);
      setNotifications((prev) => prev.map((n) => (n.id === id ? { ...n, isRead: true } : n)));
      setUnreadCount((prev) => Math.max(0, prev - 1));
    } catch (err) {
      console.error('Failed to mark read', err);
    }
  };

  const handleMarkAllRead = async () => {
    try {
      await notificationService.markAllAsRead();
      setNotifications((prev) => prev.map((n) => ({ ...n, isRead: true })));
      setUnreadCount(0);
    } catch (err) {
      console.error('Failed to mark all read', err);
    }
  };

  const handleTogglePref = async (key: keyof NotificationPreference) => {
    if (!preferences) return;
    const updated = { ...preferences, [key]: !preferences[key] };
    setPreferences(updated);
    try {
      await notificationService.updatePreferences({ [key]: updated[key] });
    } catch (err) {
      console.error('Failed to update preferences', err);
    }
  };

  const getNotificationIcon = (type: string) => {
    switch (type) {
      case 'EVENT_REMINDER':
      case 'EVENT_CANCELLED':
        return <Calendar className="w-4 h-4 text-amber-400" />;
      case 'BADGE_EARNED':
      case 'CERTIFICATE_ISSUED':
        return <Award className="w-4 h-4 text-emerald-400" />;
      case 'COMMUNITY_REPLY':
        return <MessageSquare className="w-4 h-4 text-sky-400" />;
      case 'MODERATION_UPDATE':
      case 'ACCOUNT_SECURITY':
      default:
        return <ShieldAlert className="w-4 h-4 text-rose-400" />;
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-y-0 right-0 z-50 w-full max-w-sm bg-slate-900 border-l border-slate-800 shadow-2xl flex flex-col">
      <div className="p-4 border-b border-slate-800 flex items-center justify-between">
        <div className="flex items-center gap-2">
          <Bell className="w-5 h-5 text-amber-400" />
          <h2 className="text-base font-bold text-slate-100">
            {isTamil ? 'அறிவிப்புகள்' : 'Notifications'}
          </h2>
          {unreadCount > 0 && (
            <span className="text-xs font-bold px-2 py-0.5 rounded-full bg-amber-500 text-slate-950">
              {unreadCount}
            </span>
          )}
        </div>

        <div className="flex items-center gap-2">
          <button
            onClick={() => setShowSettings(!showSettings)}
            className="p-1.5 rounded-lg text-slate-400 hover:text-amber-400 hover:bg-slate-800 transition-colors"
            title={isTamil ? 'அமைப்புகள்' : 'Notification Preferences'}
          >
            <Settings className="w-4 h-4" />
          </button>
          <button
            onClick={onClose}
            className="p-1.5 rounded-lg text-slate-400 hover:text-slate-200 hover:bg-slate-800 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>
      </div>

      {showSettings ? (
        <div className="p-4 space-y-4 border-b border-slate-800 bg-slate-950/60">
          <h3 className="text-xs font-bold text-slate-300 uppercase tracking-wider">
            {isTamil ? 'அறிவிப்பு விருப்பங்கள்' : 'Notification Preferences'}
          </h3>

          {preferences && (
            <div className="space-y-3 text-xs">
              <label className="flex items-center justify-between text-slate-300 cursor-pointer">
                <span>{isTamil ? 'நிகழ்வுகள் பற்றிய அறிவிப்புகள்' : 'Event Reminders & Updates'}</span>
                <input
                  type="checkbox"
                  checked={preferences.eventsEnabled}
                  onChange={() => handleTogglePref('eventsEnabled')}
                  className="rounded border-slate-700 text-amber-500 focus:ring-amber-500 bg-slate-900"
                />
              </label>

              <label className="flex items-center justify-between text-slate-300 cursor-pointer">
                <span>{isTamil ? 'சமூகப் பதில்கள்' : 'Community Reply Notifications'}</span>
                <input
                  type="checkbox"
                  checked={preferences.communityEnabled}
                  onChange={() => handleTogglePref('communityEnabled')}
                  className="rounded border-slate-700 text-amber-500 focus:ring-amber-500 bg-slate-900"
                />
              </label>

              <label className="flex items-center justify-between text-slate-300 cursor-pointer">
                <span>{isTamil ? 'சாதனைகள் & சான்றிதழ்கள்' : 'Badges & Certificates'}</span>
                <input
                  type="checkbox"
                  checked={preferences.achievementsEnabled}
                  onChange={() => handleTogglePref('achievementsEnabled')}
                  className="rounded border-slate-700 text-amber-500 focus:ring-amber-500 bg-slate-900"
                />
              </label>
            </div>
          )}
        </div>
      ) : (
        <div className="p-2 border-b border-slate-800/80 flex items-center justify-end">
          {unreadCount > 0 && (
            <button
              onClick={handleMarkAllRead}
              className="text-xs font-semibold text-amber-400 hover:text-amber-300 px-2 py-1 transition-colors flex items-center gap-1"
            >
              <Check className="w-3.5 h-3.5" />
              {isTamil ? 'அனைத்தும் படித்ததாகக் குறிக்க' : 'Mark all as read'}
            </button>
          )}
        </div>
      )}

      <div className="flex-1 overflow-y-auto p-3 space-y-2">
        {loading ? (
          <div className="p-8 text-center text-xs text-slate-400">Loading notifications...</div>
        ) : notifications.length === 0 ? (
          <div className="p-8 text-center text-xs text-slate-500">
            {isTamil ? 'அறிவிப்புகள் எதுவும் இல்லை.' : 'No notifications yet.'}
          </div>
        ) : (
          notifications.map((item) => (
            <div
              key={item.id}
              onClick={() => !item.isRead && handleMarkRead(item.id)}
              className={`p-3 rounded-xl border transition-all cursor-pointer ${
                item.isRead ? 'bg-slate-950/40 border-slate-800/60 text-slate-400' : 'bg-slate-900 border-amber-500/30 text-slate-200 shadow-sm'
              }`}
            >
              <div className="flex items-start gap-2.5">
                <div className="mt-0.5 shrink-0">{getNotificationIcon(item.notificationType)}</div>
                <div className="flex-1 min-w-0">
                  <h4 className="text-xs font-bold truncate">
                    {isTamil ? item.titleTa || item.title : item.title}
                  </h4>
                  <p className="text-[11px] text-slate-300 mt-1 line-clamp-2">
                    {isTamil ? item.messageTa || item.message : item.message}
                  </p>
                  <span className="text-[10px] text-slate-500 mt-1 block">
                    {new Date(item.createdAt).toLocaleString()}
                  </span>
                </div>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
};
