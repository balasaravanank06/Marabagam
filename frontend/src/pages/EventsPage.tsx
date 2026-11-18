import React, { useEffect, useState } from 'react';
import { CulturalEvent } from '../types/event';
import { eventService } from '../services/eventService';
import { EventCard } from '../components/EventCard';
import { Calendar, Filter } from 'lucide-react';


export const EventsPage: React.FC = () => {
  const [events, setEvents] = useState<CulturalEvent[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedDistrict, setSelectedDistrict] = useState<number | undefined>(undefined);
  const [isTamil, setIsTamil] = useState(false);

  useEffect(() => {
    fetchEvents();
  }, [selectedDistrict]);

  const fetchEvents = async () => {
    setLoading(true);
    try {
      const res = await eventService.getPublicEvents(selectedDistrict);
      setEvents(res.content);
    } catch (err) {
      console.error('Failed to fetch events', err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 p-6 md:p-10 max-w-7xl mx-auto space-y-8">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 border-b border-slate-800 pb-6">
        <div>
          <div className="flex items-center gap-2 text-amber-400 text-sm font-semibold mb-1">
            <Calendar className="w-4 h-4" />
            <span>{isTamil ? 'பண்பாட்டு நிகழ்வுகள்' : 'Cultural Workshops & Fairs'}</span>
          </div>
          <h1 className="text-3xl font-black tracking-tight text-slate-100">
            {isTamil ? 'நிகழ்வுகள் அகராதி' : 'Events & Workshops Directory'}
          </h1>
          <p className="text-sm text-slate-400 mt-1">
            {isTamil ? 'பாரம்பரியக் கலைப் பயிலரங்குகள் மற்றும் நிகழ்வுகளைக் கண்டறியுங்கள்.' : 'Discover authentic workshops, festival gatherings, and masterclasses across Tamil Nadu.'}
          </p>
        </div>

        <button
          onClick={() => setIsTamil(!isTamil)}
          className="self-start md:self-auto text-xs font-semibold px-3 py-1.5 rounded-lg border border-slate-700 text-slate-300 hover:bg-slate-800 transition-colors"
        >
          {isTamil ? 'English' : 'தமிழ்'}
        </button>
      </div>

      <div className="flex items-center gap-3">
        <Filter className="w-4 h-4 text-slate-400" />
        <select
          value={selectedDistrict || ''}
          onChange={(e) => setSelectedDistrict(e.target.value ? Number(e.target.value) : undefined)}
          className="bg-slate-900 border border-slate-800 rounded-lg px-3 py-2 text-sm text-slate-200 focus:outline-none focus:border-amber-500"
        >
          <option value="">{isTamil ? 'அனைத்து மாவட்டங்கள்' : 'All Districts'}</option>
          <option value="1">Madurai</option>
          <option value="2">Thanjavur</option>
          <option value="3">Chennai</option>
          <option value="4">Kanchipuram</option>
        </select>
      </div>

      {loading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {[1, 2, 3].map((i) => (
            <div key={i} className="h-64 bg-slate-900/50 rounded-xl animate-pulse border border-slate-800" />
          ))}
        </div>
      ) : events.length === 0 ? (
        <div className="bg-slate-900/60 border border-slate-800 rounded-xl p-12 text-center text-slate-400 space-y-2">
          <Calendar className="w-8 h-8 text-slate-600 mx-auto mb-2" />
          <h3 className="text-base font-semibold">{isTamil ? 'நிகழ்வுகள் எதுவும் கிடைக்கவில்லை' : 'No Published Events Found'}</h3>
          <p className="text-xs text-slate-500">{isTamil ? 'வேறு மாவட்டத்தைத் தேர்ந்தெடுக்கவும்.' : 'Try adjusting your district filter.'}</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {events.map((event) => (
            <EventCard key={event.id} event={event} isTamil={isTamil} />
          ))}
        </div>
      )}
    </div>
  );
};
