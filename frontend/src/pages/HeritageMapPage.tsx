import React, { useState, useEffect, useRef } from 'react';
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import {
  MapPin,
  List,
  Map as MapIcon,
  Search,
  ChevronRight,
  X,
  Info
} from 'lucide-react';
import {
  heritageMapService,
  HeritageMapSummary,
  DistrictSummary,
  DistrictDetail
} from '../services/heritageMapService';

interface GeoFeature {
  type: string;
  properties: { code: string; name: string; nameTa: string };
  geometry: { type: string; coordinates: number[][][] };
}

interface GeoJSONData {
  type: string;
  features: GeoFeature[];
}

export const HeritageMapPage: React.FC = () => {
  const { i18n } = useTranslation();
  const isTa = i18n.language === 'ta';

  const [viewMode, setViewMode] = useState<'map' | 'list'>('map');
  const [mapSummary, setMapSummary] = useState<HeritageMapSummary | null>(null);
  const [geoData, setGeoData] = useState<GeoJSONData | null>(null);
  const [geoValidationError, setGeoValidationError] = useState<string | null>(null);
  const [searchQuery, setSearchQuery] = useState('');

  const [selectedDistrictId, setSelectedDistrictId] = useState<number | null>(null);
  const [districtDetail, setDistrictDetail] = useState<DistrictDetail | null>(null);
  const [loadingDetail, setLoadingDetail] = useState(false);
  const [activeTab, setActiveTab] = useState<'artForms' | 'locations' | 'events'>('artForms');

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const mapContainerRef = useRef<HTMLDivElement | null>(null);
  const mapRef = useRef<L.Map | null>(null);
  const geoJsonLayerRef = useRef<L.GeoJSON | null>(null);
  const markersGroupRef = useRef<L.LayerGroup | null>(null);

  useEffect(() => {
    fetchMapData();
  }, []);

  const fetchMapData = async () => {
    try {
      setLoading(true);
      setError(null);
      const summary = await heritageMapService.getHeritageMapSummary();
      setMapSummary(summary);

      // Fetch local GeoJSON boundaries
      const res = await fetch('/geo/tn-districts.json');
      if (res.ok) {
        const json: GeoJSONData = await res.json();
        validateGeoJSON(json, summary.districts);
        setGeoData(json);
      }
    } catch (err: any) {
      setError(err.message || 'Failed to load heritage map data.');
    } finally {
      setLoading(false);
    }
  };

  const validateGeoJSON = (geo: GeoJSONData, districts: DistrictSummary[]) => {
    const geoCodes = new Set(geo.features.map((f) => f.properties.code));
    const dbCodes = new Set(districts.map((d) => d.code).filter(Boolean));

    const duplicates = geo.features.filter(
      (f, index, self) => self.findIndex((t) => t.properties.code === f.properties.code) !== index
    );

    if (duplicates.length > 0) {
      setGeoValidationError(`GeoJSON Integrity Error: Duplicate codes found: ${duplicates.map(d => d.properties.code).join(', ')}`);
      return;
    }

    const orphans = Array.from(geoCodes).filter((code) => !dbCodes.has(code));
    if (orphans.length > 0) {
      setGeoValidationError(`GeoJSON Integrity Warning: Unknown polygon codes present: ${orphans.join(', ')}`);
    }
  };

  // Initialize Leaflet map
  useEffect(() => {
    if (viewMode !== 'map' || loading || !mapContainerRef.current) return;

    if (mapRef.current) {
      mapRef.current.invalidateSize();
      return;
    }

    // Tamil Nadu Center [latitude, longitude]
    const tnCenter: [number, number] = [10.8505, 78.6569];
    const initialZoom = 7;

    const map = L.map(mapContainerRef.current, {
      center: tnCenter,
      zoom: initialZoom,
      zoomControl: true,
      scrollWheelZoom: true,
    });

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 18,
      attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
    }).addTo(map);

    markersGroupRef.current = L.layerGroup().addTo(map);
    mapRef.current = map;

    return () => {
      if (mapRef.current) {
        mapRef.current.remove();
        mapRef.current = null;
        markersGroupRef.current = null;
        geoJsonLayerRef.current = null;
      }
    };
  }, [viewMode, loading]);

  // Update GeoJSON & Markers on Leaflet map
  useEffect(() => {
    if (!mapRef.current) return;
    const map = mapRef.current;

    // Clean previous layers
    if (geoJsonLayerRef.current) {
      map.removeLayer(geoJsonLayerRef.current);
      geoJsonLayerRef.current = null;
    }
    if (markersGroupRef.current) {
      markersGroupRef.current.clearLayers();
    }

    // Add GeoJSON Layer if available
    if (geoData) {
      geoJsonLayerRef.current = L.geoJSON(geoData as any, {
        style: (feature) => {
          const code = feature?.properties?.code;
          const district = mapSummary?.districts.find((d) => d.code === code);
          const isSelected = selectedDistrictId === district?.id;

          return {
            fillColor: isSelected ? '#f59e0b' : '#1e293b',
            fillOpacity: isSelected ? 0.6 : 0.3,
            color: isSelected ? '#fbbf24' : '#475569',
            weight: isSelected ? 2 : 1,
          };
        },
        onEachFeature: (feature, layer) => {
          const code = feature.properties.code;
          const district = mapSummary?.districts.find((d) => d.code === code);

          if (district) {
            const nameText = isTa ? district.nameTa : district.name;
            layer.bindTooltip(
              `<div style="font-size: 12px; font-weight: bold; color: #f59e0b;">${nameText}</div>` +
              `<div style="font-size: 10px; color: #cbd5e1;">🎨 ${district.artFormCount} Arts | 🏛️ ${district.locationCount} Academies</div>`,
              { sticky: true }
            );

            layer.on('click', () => {
              handleSelectDistrict(district.id);
            });
          }
        },
      }).addTo(map);
    }

    // Add Markers for Districts with Lat/Lng
    if (mapSummary?.districts && markersGroupRef.current) {
      mapSummary.districts.forEach((d) => {
        if (d.latitude && d.longitude) {
          const isSelected = selectedDistrictId === d.id;
          const markerIcon = L.divIcon({
            className: 'custom-leaflet-marker',
            html: `
              <div style="
                width: 24px;
                height: 24px;
                background-color: ${isSelected ? '#f59e0b' : '#991b1b'};
                border: 2px solid ${isSelected ? '#fef3c7' : '#f59e0b'};
                border-radius: 50%;
                display: flex;
                align-items: center;
                justify-content: center;
                color: #ffffff;
                font-weight: bold;
                font-size: 11px;
                box-shadow: 0 0 8px rgba(245, 158, 11, 0.5);
                cursor: pointer;
              ">
                📍
              </div>
            `,
            iconSize: [24, 24],
            iconAnchor: [12, 12],
          });

          const marker = L.marker([d.latitude, d.longitude], { icon: markerIcon });
          const nameText = isTa ? d.nameTa : d.name;
          marker.bindTooltip(
            `<div style="font-size: 12px; font-weight: bold; color: #f59e0b;">${nameText}</div>`,
            { direction: 'top' }
          );

          marker.on('click', () => {
            handleSelectDistrict(d.id);
          });

          markersGroupRef.current?.addLayer(marker);
        }
      });
    }
  }, [geoData, mapSummary, selectedDistrictId, isTa]);

  const handleSelectDistrict = async (id: number) => {
    try {
      setSelectedDistrictId(id);
      setLoadingDetail(true);
      const detail = await heritageMapService.getDistrictDetail(id);
      setDistrictDetail(detail);
      setActiveTab('artForms');

      if (mapRef.current && detail.latitude && detail.longitude) {
        mapRef.current.flyTo([detail.latitude, detail.longitude], 9, {
          duration: 1.2,
        });
      }
    } catch (err: any) {
      console.error('Failed to load district detail', err);
    } finally {
      setLoadingDetail(false);
    }
  };

  const filteredDistricts = (mapSummary?.districts || []).filter((d) => {
    if (!searchQuery.trim()) return true;
    const q = searchQuery.toLowerCase();
    return d.name.toLowerCase().includes(q) || d.nameTa.includes(q) || d.code.toLowerCase().includes(q);
  });

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 bg-slate-900/60 p-6 rounded-2xl border border-slate-800 backdrop-blur-md">
        <div>
          <div className="flex items-center space-x-2 text-amber-400 text-xs font-semibold uppercase tracking-wider mb-1">
            <MapPin className="w-4 h-4" />
            <span>{isTa ? 'மண்டல பாரம்பரிய வரைபடம்' : 'Regional Heritage Map'}</span>
          </div>
          <h1 className="text-2xl sm:text-3xl font-bold tracking-tight text-white">
            {isTa ? 'தமிழ்நாடு கலை வரைபடம்' : 'Tamil Nadu Geographic Discovery'}
          </h1>
          <p className="text-slate-400 text-xs sm:text-sm mt-1">
            {isTa
              ? 'மாவட்ட ரீதியாக தமிழ்நாட்டின் பாரம்பரிய தற்காப்புக் கலைகள், நடனங்கள் மற்றும் கலாச்சார மையங்களைக் கண்டறியவும்.'
              : 'Explore traditional Tamil arts, training academies, and regional festivals geographically across Tamil Nadu.'}
          </p>
        </div>

        {/* View Mode Switcher */}
        <div className="flex items-center space-x-2 bg-slate-950 p-1.5 rounded-xl border border-slate-800 self-start md:self-auto">
          <button
            onClick={() => setViewMode('map')}
            className={`flex items-center space-x-2 px-4 py-2 rounded-lg text-xs font-bold transition ${
              viewMode === 'map'
                ? 'bg-amber-500 text-slate-950 shadow-md shadow-amber-500/20'
                : 'text-slate-400 hover:text-white hover:bg-slate-900'
            }`}
          >
            <MapIcon className="w-4 h-4" />
            <span>{isTa ? 'வரைபடம்' : 'Map View'}</span>
          </button>
          <button
            onClick={() => setViewMode('list')}
            className={`flex items-center space-x-2 px-4 py-2 rounded-lg text-xs font-bold transition ${
              viewMode === 'list'
                ? 'bg-amber-500 text-slate-950 shadow-md shadow-amber-500/20'
                : 'text-slate-400 hover:text-white hover:bg-slate-900'
            }`}
          >
            <List className="w-4 h-4" />
            <span>{isTa ? 'பட்டியல்' : 'Accessible List'}</span>
          </button>
        </div>
      </div>

      {/* Overview Stats Bar */}
      {mapSummary && (
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
          <div className="bg-slate-900/40 p-4 rounded-xl border border-slate-800/80">
            <span className="text-xs text-slate-400 font-medium block">{isTa ? 'மாவட்டங்கள்' : 'Districts'}</span>
            <span className="text-2xl font-bold text-amber-400">{mapSummary.totalDistricts}</span>
          </div>
          <div className="bg-slate-900/40 p-4 rounded-xl border border-slate-800/80">
            <span className="text-xs text-slate-400 font-medium block">{isTa ? 'கலைகள்' : 'Published Art Forms'}</span>
            <span className="text-2xl font-bold text-rose-400">{mapSummary.totalPublishedArtForms}</span>
          </div>
          <div className="bg-slate-900/40 p-4 rounded-xl border border-slate-800/80">
            <span className="text-xs text-slate-400 font-medium block">{isTa ? 'பயிற்சி மையங்கள்' : 'Training Academies'}</span>
            <span className="text-2xl font-bold text-emerald-400">{mapSummary.totalPublishedLocations}</span>
          </div>
          <div className="bg-slate-900/40 p-4 rounded-xl border border-slate-800/80">
            <span className="text-xs text-slate-400 font-medium block">{isTa ? 'திருவிழாக்கள்' : 'Heritage Festivals'}</span>
            <span className="text-2xl font-bold text-sky-400">{mapSummary.totalPublishedEvents}</span>
          </div>
        </div>
      )}

      {/* Validation Warning */}
      {geoValidationError && (
        <div className="p-3 bg-amber-500/10 border border-amber-500/30 rounded-xl text-amber-300 text-xs flex items-center space-x-2">
          <Info className="w-4 h-4 flex-shrink-0" />
          <span>{geoValidationError}</span>
        </div>
      )}

      {/* Search Bar */}
      <div className="relative">
        <Search className="w-5 h-5 absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-500" />
        <input
          type="text"
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          placeholder={isTa ? 'மாவட்டம் பெயரைத் தேடுக...' : 'Search districts by English or Tamil name...'}
          className="w-full pl-10 pr-4 py-3 bg-slate-900/80 border border-slate-800 rounded-xl text-white text-sm focus:outline-none focus:border-amber-500 transition"
        />
      </div>

      {loading ? (
        <div className="py-20 text-center text-slate-500">
          <div className="w-8 h-8 border-2 border-amber-500 border-t-transparent rounded-full animate-spin mx-auto mb-3"></div>
          <p className="text-sm font-medium">{isTa ? 'வரைபடம் பதிவேறுகிறது...' : 'Loading Tamil Nadu Heritage Map...'}</p>
        </div>
      ) : error ? (
        <div className="p-6 bg-red-950/40 border border-red-800 rounded-2xl text-center text-red-300 text-sm">
          {error}
        </div>
      ) : (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Main Display Area (Map or List) */}
          <div className="lg:col-span-2 space-y-4">
            {viewMode === 'map' ? (
              <div className="bg-slate-900/60 p-4 rounded-2xl border border-slate-800 relative overflow-hidden min-h-[480px]">
                <div className="text-xs text-slate-400 mb-2 flex items-center justify-between">
                  <span>{isTa ? 'தமிழ்நாடு மாவட்ட வரைபடம்' : 'Tamil Nadu Interactive Map'}</span>
                  <span className="text-[11px] text-slate-500">
                    {isTa ? 'மாவட்டத்தைச் சொடுக்கவும்' : 'Click a district marker or polygon to explore'}
                  </span>
                </div>

                {/* Leaflet Map Container */}
                <div
                  ref={mapContainerRef}
                  id="heritage-map-container"
                  className="w-full h-[480px] min-h-[480px] bg-slate-950 rounded-xl border border-slate-900 z-0 overflow-hidden"
                />

                <div className="mt-2 text-right">
                  <span className="text-[10px] text-slate-500">
                    Map data &copy; <a href="https://www.openstreetmap.org/copyright" target="_blank" rel="noreferrer" className="underline hover:text-slate-400">OpenStreetMap</a> contributors
                  </span>
                </div>
              </div>
            ) : (
              /* Accessible Non-Map List View Mode */
              <div
                role="region"
                aria-label={isTa ? 'தமிழ்நாடு மாவட்டங்களின் பட்டியல்' : 'Tamil Nadu Heritage Discovery List'}
                className="space-y-3"
              >
                <div className="text-xs font-semibold text-slate-400 mb-2">
                  {isTa ? `மாவட்டங்கள் (${filteredDistricts.length})` : `Districts (${filteredDistricts.length})`}
                </div>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                  {filteredDistricts.map((d) => (
                    <button
                      key={d.id}
                      onClick={() => handleSelectDistrict(d.id)}
                      className={`p-4 rounded-xl border text-left transition flex items-start justify-between ${
                        selectedDistrictId === d.id
                          ? 'bg-amber-500/10 border-amber-500/50 text-amber-300'
                          : 'bg-slate-900/40 border-slate-800 text-slate-300 hover:bg-slate-800/60 hover:border-slate-700'
                      }`}
                    >
                      <div>
                        <h3 className="font-bold text-base text-white">{isTa ? d.nameTa : d.name}</h3>
                        <p className="text-xs text-slate-400">{isTa ? d.name : d.nameTa}</p>

                        <div className="flex items-center space-x-3 mt-3 text-xs text-slate-400">
                          <span className="bg-slate-800 px-2 py-0.5 rounded text-[11px]">
                            🎨 {d.artFormCount} {isTa ? 'கலைகள்' : 'Arts'}
                          </span>
                          <span className="bg-slate-800 px-2 py-0.5 rounded text-[11px]">
                            🏛️ {d.locationCount} {isTa ? 'மையங்கள்' : 'Academies'}
                          </span>
                        </div>
                      </div>
                      <ChevronRight className="w-5 h-5 text-slate-600 mt-1" />
                    </button>
                  ))}
                </div>
              </div>
            )}
          </div>

          {/* District Detail Drawer / Sidebar Panel */}
          <div className="bg-slate-900/60 p-6 rounded-2xl border border-slate-800 h-fit space-y-4 backdrop-blur-md">
            {loadingDetail ? (
              <div className="py-12 text-center text-slate-500">
                <div className="w-6 h-6 border-2 border-amber-500 border-t-transparent rounded-full animate-spin mx-auto mb-2"></div>
                <p className="text-xs">{isTa ? 'விவரங்கள் பெறப்படுகின்றன...' : 'Fetching district details...'}</p>
              </div>
            ) : districtDetail ? (
              <div className="space-y-5">
                {/* District Header */}
                <div>
                  <div className="flex items-center justify-between">
                    <span className="text-[11px] font-bold uppercase tracking-wider text-amber-400">
                      {districtDetail.code}
                    </span>
                    <button
                      onClick={() => {
                        setSelectedDistrictId(null);
                        setDistrictDetail(null);
                      }}
                      className="text-slate-500 hover:text-slate-300"
                    >
                      <X className="w-4 h-4" />
                    </button>
                  </div>
                  <h2 className="text-2xl font-bold text-white mt-1">
                    {isTa ? districtDetail.nameTa : districtDetail.name}
                  </h2>
                  <p className="text-xs text-slate-400">
                    {isTa ? districtDetail.name : districtDetail.nameTa} • Centroid ({districtDetail.latitude?.toFixed(2)}°N, {districtDetail.longitude?.toFixed(2)}°E)
                  </p>
                </div>

                {/* Short History */}
                {districtDetail.shortHistory && (
                  <div className="bg-slate-950/60 p-3.5 rounded-xl border border-slate-900 text-xs text-slate-300 leading-relaxed">
                    <p className="font-semibold text-amber-300/90 mb-1">
                      {isTa ? 'வரலாற்றுப் பின்னணி:' : 'Historical Heritage:'}
                    </p>
                    <p>{isTa ? districtDetail.shortHistoryTa : districtDetail.shortHistory}</p>
                  </div>
                )}

                {/* Tabs Switcher */}
                <div className="flex border-b border-slate-800 text-xs font-semibold">
                  <button
                    onClick={() => setActiveTab('artForms')}
                    className={`pb-2 px-3 transition border-b-2 ${
                      activeTab === 'artForms'
                        ? 'border-amber-500 text-amber-300'
                        : 'border-transparent text-slate-400 hover:text-slate-200'
                    }`}
                  >
                    🎨 {isTa ? 'கலைகள்' : 'Art Forms'} ({districtDetail.artForms.length})
                  </button>
                  <button
                    onClick={() => setActiveTab('locations')}
                    className={`pb-2 px-3 transition border-b-2 ${
                      activeTab === 'locations'
                        ? 'border-amber-500 text-amber-300'
                        : 'border-transparent text-slate-400 hover:text-slate-200'
                    }`}
                  >
                    🏛️ {isTa ? 'மையங்கள்' : 'Academies'} ({districtDetail.locations.length})
                  </button>
                  <button
                    onClick={() => setActiveTab('events')}
                    className={`pb-2 px-3 transition border-b-2 ${
                      activeTab === 'events'
                        ? 'border-amber-500 text-amber-300'
                        : 'border-transparent text-slate-400 hover:text-slate-200'
                    }`}
                  >
                    🎭 {isTa ? 'நிகழ்வுகள்' : 'Events'} ({districtDetail.events.length})
                  </button>
                </div>

                {/* Tab Contents */}
                {activeTab === 'artForms' && (
                  <div className="space-y-3">
                    {districtDetail.artForms.length === 0 ? (
                      <p className="text-xs text-slate-500 italic py-4 text-center">
                        {isTa ? 'இம்மாவட்டத்தில் பதிவு செய்யப்பட்ட கலைகள் இல்லை.' : 'No published art forms registered for this district yet.'}
                      </p>
                    ) : (
                      districtDetail.artForms.map((art) => (
                        <Link
                          key={art.id}
                          to={`/art-forms/${art.slug}`}
                          className="block p-3 rounded-xl bg-slate-950/60 border border-slate-900 hover:border-amber-500/40 transition group"
                        >
                          <div className="flex items-center justify-between">
                            <span className="font-bold text-sm text-slate-200 group-hover:text-amber-300">
                              {isTa ? art.nameTa : art.name}
                            </span>
                            <span className="text-[10px] bg-slate-800 text-amber-400 px-2 py-0.5 rounded">
                              {art.categoryName}
                            </span>
                          </div>
                          <p className="text-xs text-slate-400 mt-1 line-clamp-2">
                            {isTa ? art.shortDescriptionTa : art.shortDescription}
                          </p>
                        </Link>
                      ))
                    )}
                  </div>
                )}

                {activeTab === 'locations' && (
                  <div className="space-y-3">
                    {districtDetail.locations.length === 0 ? (
                      <p className="text-xs text-slate-500 italic py-4 text-center">
                        {isTa ? 'இம்மாவட்டத்தில் பொது பயிற்சி மையங்கள் இல்லை.' : 'No public training academies recorded for this district.'}
                      </p>
                    ) : (
                      districtDetail.locations.map((loc) => (
                        <div key={loc.id} className="p-3.5 rounded-xl bg-slate-950/60 border border-slate-900 text-xs space-y-1.5">
                          <h4 className="font-bold text-amber-300 text-sm">{isTa ? loc.nameTa : loc.name}</h4>
                          <p className="text-slate-300">{isTa ? loc.descriptionTa : loc.description}</p>
                          <p className="text-slate-400">📍 {isTa ? loc.addressTa : loc.address}</p>

                          {loc.provenanceSource && (
                            <p className="text-[10px] text-slate-500 italic mt-1 border-t border-slate-900 pt-1">
                              Source: {loc.provenanceSource}
                            </p>
                          )}
                        </div>
                      ))
                    )}
                  </div>
                )}

                {activeTab === 'events' && (
                  <div className="space-y-3">
                    {districtDetail.events.length === 0 ? (
                      <p className="text-xs text-slate-500 italic py-4 text-center">
                        {isTa ? 'இம்மாவட்டத்தில் விழாக்கள் எதுவும் இல்லை.' : 'No cultural festivals recorded for this district.'}
                      </p>
                    ) : (
                      districtDetail.events.map((ev) => (
                        <div key={ev.id} className="p-3.5 rounded-xl bg-slate-950/60 border border-slate-900 text-xs space-y-1.5">
                          <h4 className="font-bold text-rose-300 text-sm">{isTa ? ev.titleTa : ev.title}</h4>
                          <p className="text-amber-400 font-semibold">🗓️ {isTa ? ev.eventPeriodTa : ev.eventPeriod}</p>
                          <p className="text-slate-300">{isTa ? ev.descriptionTa : ev.description}</p>
                          {ev.venueName && <p className="text-slate-400">📍 {isTa ? ev.venueNameTa : ev.venueName}</p>}

                          {ev.provenanceSource && (
                            <p className="text-[10px] text-slate-500 italic mt-1 border-t border-slate-900 pt-1">
                              Source: {ev.provenanceSource}
                            </p>
                          )}
                        </div>
                      ))
                    )}
                  </div>
                )}
              </div>
            ) : (
              <div className="py-16 text-center text-slate-500 space-y-2">
                <MapPin className="w-8 h-8 mx-auto text-slate-700" />
                <p className="text-xs">
                  {isTa
                    ? 'வரைபடத்தில் அல்லது பட்டியலில் ஒரு மாவட்டத்தைத் தேர்ந்தெடுக்கவும்.'
                    : 'Select a district on the map or list to view its heritage profile.'}
                </p>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
};

export default HeritageMapPage;

