import React, { useEffect, useState } from 'react';
import { EventRegistration } from '../types/event';
import { eventService } from '../services/eventService';
import { Calendar } from 'lucide-react';

import { Link } from 'react-router-dom';

export const MyEventsPage: React.FC = () => {
  const [registrations, setRegistrations] = useState<EventRegistration[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchMyRegistrations();
  }, []);

  const fetchMyRegistrations = async () => {
    setLoading(true);
    try {
      const data = await eventService.getMyRegistrations();
      setRegistrations(data);
    } catch (err) {
      console.error('Failed to fetch user registrations', err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 p-6 md:p-10 max-w-4xl mx-auto space-y-8">
      <div className="border-b border-slate-800 pb-6">
        <div className="flex items-center gap-2 text-amber-400 text-sm font-semibold mb-1">
          <Calendar className="w-4 h-4" />
          <span>My Workshop Registrations</span>
        </div>
        <h1 className="text-3xl font-black tracking-tight text-slate-100">Registered Events</h1>
      </div>

      {loading ? (
        <div className="p-8 text-center text-slate-400">Loading registrations...</div>
      ) : registrations.length === 0 ? (
        <div className="bg-slate-900/60 border border-slate-800 rounded-xl p-12 text-center text-slate-400 space-y-3">
          <Calendar className="w-8 h-8 text-slate-600 mx-auto" />
          <h3 className="text-base font-semibold">No Registered Events</h3>
          <p className="text-xs text-slate-500">Explore published cultural workshops in the events directory.</p>
          <Link to="/events" className="inline-block mt-2 text-xs font-semibold px-4 py-2 rounded-lg bg-amber-500 text-slate-950">
            Browse Events
          </Link>
        </div>
      ) : (
        <div className="space-y-4">
          {registrations.map((reg) => (
            <div key={reg.id} className="bg-slate-900 border border-slate-800 rounded-xl p-5 flex items-center justify-between gap-4">
              <div>
                <span className={`text-[10px] font-bold px-2 py-0.5 rounded border inline-block mb-1.5 ${
                  reg.status === 'REGISTERED' ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20' : 'bg-rose-500/10 text-rose-400 border-rose-500/20'
                }`}>
                  {reg.status}
                </span>
                <h3 className="text-base font-bold text-slate-100">{reg.eventTitle}</h3>
                <p className="text-xs text-slate-400 font-sans mt-0.5">{reg.eventTitleTa}</p>
                <span className="text-[11px] text-slate-500 mt-2 block">
                  Registered: {new Date(reg.registeredAt).toLocaleDateString()}
                </span>
              </div>

              {reg.status === 'REGISTERED' && (
                <button
                  onClick={async () => {
                    await eventService.cancelRegistration(reg.eventId);
                    fetchMyRegistrations();
                  }}
                  className="text-xs font-semibold px-3 py-1.5 rounded-lg border border-rose-500/30 text-rose-400 hover:bg-rose-500/10 transition-colors"
                >
                  Cancel Registration
                </button>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
