import React from 'react';
import { Link } from 'react-router-dom';
import { CulturalEvent } from '../types/event';
import { Calendar, MapPin, Users, Video } from 'lucide-react';
import { exportEventToIcs } from '../utils/icsExporter';

interface EventCardProps {
  event: CulturalEvent;
  isTamil?: boolean;
}

export const EventCard: React.FC<EventCardProps> = ({ event, isTamil = false }) => {
  const title = isTamil ? event.titleTa || event.title : event.title;
  const description = isTamil ? event.descriptionTa || event.description : event.description;
  const venue = isTamil ? event.venueNameTa || event.venueName : event.venueName;
  const period = isTamil ? event.eventPeriodTa || event.eventPeriod : event.eventPeriod;

  return (
    <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 hover:border-amber-500/50 transition-all flex flex-col justify-between shadow-lg">
      <div>
        <div className="flex items-center justify-between gap-2 mb-3">
          <span className="text-xs font-semibold px-2.5 py-1 rounded-full bg-amber-500/10 text-amber-400 border border-amber-500/20">
            {event.eventType || 'CULTURAL_EVENT'}
          </span>
          {event.isOnline && (
            <span className="text-xs font-semibold px-2.5 py-1 rounded-full bg-indigo-500/10 text-indigo-400 border border-indigo-500/20 flex items-center gap-1">
              <Video className="w-3 h-3" />
              {isTamil ? 'இணையவழியில்' : 'Online'}
            </span>
          )}
        </div>

        <Link to={`/event/${event.slug}`}>
          <h3 className="text-lg font-bold text-slate-100 hover:text-amber-400 transition-colors line-clamp-1 mb-2">
            {title}
          </h3>
        </Link>

        <p className="text-sm text-slate-400 line-clamp-2 mb-4">
          {description}
        </p>

        <div className="space-y-2 text-xs text-slate-300 mb-4">
          <div className="flex items-center gap-2">
            <Calendar className="w-4 h-4 text-amber-400 shrink-0" />
            <span>{event.startAt ? new Date(event.startAt).toLocaleString() : period}</span>
          </div>

          {venue && (
            <div className="flex items-center gap-2">
              <MapPin className="w-4 h-4 text-emerald-400 shrink-0" />
              <span className="truncate">{venue}</span>
            </div>
          )}

          {event.capacity && (
            <div className="flex items-center gap-2">
              <Users className="w-4 h-4 text-sky-400 shrink-0" />
              <span>
                {event.registeredCount || 0} / {event.capacity} {isTamil ? 'பதிவுகள்' : 'Registered'}
              </span>
            </div>
          )}
        </div>
      </div>

      <div className="pt-3 border-t border-slate-800 flex items-center justify-between gap-2">
        <Link
          to={`/event/${event.slug}`}
          className="text-xs font-semibold px-3 py-1.5 rounded-lg bg-amber-500 hover:bg-amber-400 text-slate-950 transition-colors"
        >
          {isTamil ? 'விவரங்கள்' : 'View Details'}
        </Link>

        <button
          onClick={() => exportEventToIcs(event)}
          className="text-xs font-medium px-2.5 py-1.5 rounded-lg border border-slate-700 text-slate-300 hover:bg-slate-800 transition-colors"
        >
          {isTamil ? '.ics நாட்காட்டி' : '.ics Export'}
        </button>
      </div>
    </div>
  );
};
