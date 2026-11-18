import React, { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { CulturalEvent } from '../types/event';
import { eventService } from '../services/eventService';
import { exportEventToIcs } from '../utils/icsExporter';
import { Calendar, MapPin, Users, Video, ShieldCheck, CheckCircle2, ArrowLeft } from 'lucide-react';

export const EventDetailPage: React.FC = () => {
  const { slug } = useParams<{ slug: string }>();
  const [event, setEvent] = useState<CulturalEvent | null>(null);
  const [loading, setLoading] = useState(true);
  const [registering, setRegistering] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (slug) fetchEvent();
  }, [slug]);

  const fetchEvent = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await eventService.getEventBySlug(slug!);
      setEvent(data);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Event not found');
    } finally {
      setLoading(false);
    }
  };

  const handleRegister = async () => {
    if (!event) return;
    setRegistering(true);
    try {
      if (event.isUserRegistered) {
        await eventService.cancelRegistration(event.id);
        setEvent({ ...event, isUserRegistered: false, registeredCount: Math.max(0, (event.registeredCount || 1) - 1) });
      } else {
        await eventService.registerForEvent(event.id);
        fetchEvent(); // Refresh to fetch private meeting URL
      }
    } catch (err: any) {
      alert(err.response?.data?.message || 'Registration failed');
    } finally {
      setRegistering(false);
    }
  };

  if (loading) return <div className="p-10 text-center text-slate-400">Loading event details...</div>;
  if (error || !event) return <div className="p-10 text-center text-rose-400">{error || 'Event not found'}</div>;

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 p-6 md:p-10 max-w-4xl mx-auto space-y-8">
      <Link to="/events" className="inline-flex items-center gap-1.5 text-xs text-slate-400 hover:text-amber-400 transition-colors">
        <ArrowLeft className="w-4 h-4" /> Back to Events Directory
      </Link>

      <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 md:p-8 space-y-6 shadow-xl">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <span className="text-xs font-bold px-3 py-1 rounded-full bg-amber-500/10 text-amber-400 border border-amber-500/20">
            {event.eventType}
          </span>
          <div className="flex items-center gap-2">
            <button
              onClick={() => exportEventToIcs(event)}
              className="text-xs font-semibold px-3 py-1.5 rounded-lg border border-slate-700 hover:bg-slate-800 text-slate-300 transition-colors"
            >
              Export .ics Calendar
            </button>
          </div>
        </div>

        <div>
          <h1 className="text-2xl md:text-3xl font-black text-slate-100 mb-2">{event.title}</h1>
          <p className="text-lg font-medium text-amber-400/90 font-sans">{event.titleTa}</p>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-xs bg-slate-950/60 p-4 rounded-xl border border-slate-800">
          <div className="flex items-center gap-2 text-slate-300">
            <Calendar className="w-4 h-4 text-amber-400 shrink-0" />
            <div>
              <span className="font-semibold block text-slate-400">Date & Time</span>
              <span>{event.startAt ? new Date(event.startAt).toLocaleString() : event.eventPeriod}</span>
            </div>
          </div>

          <div className="flex items-center gap-2 text-slate-300">
            <MapPin className="w-4 h-4 text-emerald-400 shrink-0" />
            <div>
              <span className="font-semibold block text-slate-400">Venue</span>
              <span>{event.venueName || (event.isOnline ? 'Online Event' : 'Cultural Workshop Venue')}</span>
            </div>
          </div>

          {event.capacity && (
            <div className="flex items-center gap-2 text-slate-300">
              <Users className="w-4 h-4 text-sky-400 shrink-0" />
              <div>
                <span className="font-semibold block text-slate-400">Capacity & Spots</span>
                <span>{event.registeredCount || 0} / {event.capacity} registered</span>
              </div>
            </div>
          )}

          {event.organizerName && (
            <div className="flex items-center gap-2 text-slate-300">
              <ShieldCheck className="w-4 h-4 text-indigo-400 shrink-0" />
              <div>
                <span className="font-semibold block text-slate-400">Organizer</span>
                <span>{event.organizerName}</span>
              </div>
            </div>
          )}
        </div>

        <div className="space-y-4">
          <h2 className="text-base font-bold text-slate-200">About the Workshop / Event</h2>
          <p className="text-sm text-slate-300 whitespace-pre-wrap leading-relaxed">{event.description}</p>
          <p className="text-sm text-slate-400 whitespace-pre-wrap leading-relaxed font-sans">{event.descriptionTa}</p>
        </div>

        {/* Restricted Private Online Meeting Link Section */}
        {event.isOnline && (
          <div className="p-4 rounded-xl bg-indigo-500/10 border border-indigo-500/20 space-y-2">
            <div className="flex items-center gap-2 text-indigo-400 font-bold text-sm">
              <Video className="w-4 h-4" />
              <span>Online Workshop Access Link</span>
            </div>
            {event.isUserRegistered && event.meetingUrl ? (
              <a
                href={event.meetingUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="inline-block text-xs font-semibold px-4 py-2 rounded-lg bg-indigo-600 hover:bg-indigo-500 text-white transition-colors"
              >
                Join Online Meeting ({event.meetingUrl})
              </a>
            ) : (
              <p className="text-xs text-slate-400">
                Private meeting link is restricted. Register for this event to access the meeting link.
              </p>
            )}
          </div>
        )}

        <div className="pt-4 border-t border-slate-800 flex items-center justify-between">
          <button
            onClick={handleRegister}
            disabled={Boolean(registering || (event.capacity != null && (event.registeredCount || 0) >= event.capacity && !event.isUserRegistered))}

            className={`px-6 py-2.5 rounded-xl text-sm font-bold transition-colors flex items-center gap-2 ${
              event.isUserRegistered
                ? 'bg-rose-500/20 hover:bg-rose-500/30 text-rose-300 border border-rose-500/30'
                : 'bg-amber-500 hover:bg-amber-400 text-slate-950'
            }`}
          >
            {event.isUserRegistered ? (
              <>
                <CheckCircle2 className="w-4 h-4" /> Cancel Registration
              </>
            ) : (
              'Register for Free'
            )}
          </button>
        </div>
      </div>
    </div>
  );
};
