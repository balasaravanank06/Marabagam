import { CulturalEvent } from '../types/event';

export function exportEventToIcs(event: CulturalEvent): void {
  const startDateStr = event.startAt ? new Date(event.startAt).toISOString().replace(/-|:|\.\d+/g, '') : new Date().toISOString().replace(/-|:|\.\d+/g, '');
  const endDateStr = event.endAt ? new Date(event.endAt).toISOString().replace(/-|:|\.\d+/g, '') : new Date(Date.now() + 7200000).toISOString().replace(/-|:|\.\d+/g, '');

  const icsContent = [
    'BEGIN:VCALENDAR',
    'VERSION:2.0',
    'PRODID:-//Marabagam Cultural Heritage Platform//EN',
    'CALSCALE:GREGORIAN',
    'METHOD:PUBLISH',
    'BEGIN:VEVENT',
    `UID:event-${event.id}@marabagam.com`,
    `DTSTAMP:${new Date().toISOString().replace(/-|:|\.\d+/g, '')}`,
    `DTSTART:${startDateStr}`,
    `DTEND:${endDateStr}`,
    `SUMMARY:${escapeIcsText(event.title)}`,
    `DESCRIPTION:${escapeIcsText(event.description)}`,
    `LOCATION:${escapeIcsText(event.venueName || (event.isOnline ? 'Online Event' : 'Cultural Center'))}`,
    'STATUS:CONFIRMED',
    'END:VEVENT',
    'END:VCALENDAR'
  ].join('\r\n');

  const blob = new Blob([icsContent], { type: 'text/calendar;charset=utf-8' });
  const link = document.createElement('a');
  link.href = window.URL.createObjectURL(blob);
  link.setAttribute('download', `${event.slug || 'cultural-event'}.ics`);
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
}

function escapeIcsText(str: string): string {
  if (!str) return '';
  return str.replace(/\\/g, '\\\\').replace(/;/g, '\\;').replace(/,/g, '\\,').replace(/\n/g, '\\n');
}
