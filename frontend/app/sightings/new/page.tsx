'use client';

import React, { useState, useCallback } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@clerk/nextjs';
import Autocomplete from '../../components/Autocomplete';
import PhotoUpload from '../../components/PhotoUpload';

interface Airport {
  icao: string;
  iata: string;
  name: string;
  city: string;
  country: string;
  latitude: number;
  longitude: number;
}

interface Aircraft {
  icao24: string;
  callsign: string;
  originCountry: string;
  registration?: string;
  model?: string;
  manufacturer?: string;
  operator?: string;
}

interface FormState {
  timestamp: string;
  airportIataOrIcao: string;
  locationText: string;
  airline: string;
  callsign: string;
  icao24: string;
  registration: string;
  aircraftModel: string;
  notes: string;
  visibility: 'PUBLIC' | 'PRIVATE';
}

export default function NewSightingPage() {
  const router = useRouter();
  const { getToken } = useAuth();
  const [form, setForm] = useState<FormState>({
    timestamp: new Date().toISOString().slice(0, 16),
    airportIataOrIcao: '',
    locationText: '',
    airline: '',
    callsign: '',
    icao24: '',
    registration: '',
    aircraftModel: '',
    notes: '',
    visibility: 'PUBLIC',
  });
  const [selectedAirport, setSelectedAirport] = useState<Airport | null>(null);
  const [selectedPhotos, setSelectedPhotos] = useState<File[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  // Fetch airports from backend
  const fetchAirports = useCallback(async (query: string) => {
    try {
      const res = await fetch(`${process.env.NEXT_PUBLIC_BACKEND_URL}/api/lookup/airports?q=${encodeURIComponent(query)}`);
      if (!res.ok) return [];
      const airports: Airport[] = await res.json();
      return airports.map(a => ({
        value: a.icao,
        label: `${a.iata} / ${a.icao} - ${a.name}`,
        sublabel: `${a.city}, ${a.country}`,
        data: a,
      }));
    } catch {
      return [];
    }
  }, []);

  // Fetch live aircraft from OpenSky (only if airport is selected)
  const fetchAircraft = useCallback(async (query: string) => {
    // Only fetch if an airport has been selected
    if (!selectedAirport) {
      return [];
    }
    
    try {
      // Pass airport coordinates to search nearby aircraft
      const params = new URLSearchParams({
        q: query,
        lat: selectedAirport.latitude.toString(),
        lon: selectedAirport.longitude.toString(),
      });
      
      const res = await fetch(`${process.env.NEXT_PUBLIC_BACKEND_URL}/api/lookup/aircraft?${params}`);
      if (!res.ok) return [];
      const aircraft: Aircraft[] = await res.json();
      return aircraft.map(a => {
        // Build a descriptive label with available data
        const parts = [a.callsign.trim()];
        if (a.registration) parts.push(`(${a.registration})`);
        
        const sublabelParts = [`ICAO24: ${a.icao24}`];
        if (a.model) sublabelParts.push(a.model);
        if (a.operator) sublabelParts.push(a.operator);
        if (a.originCountry) sublabelParts.push(a.originCountry);
        
        return {
          value: a.callsign,
          label: parts.join(' '),
          sublabel: sublabelParts.join(' • '),
          data: a,
        };
      });
    } catch {
      return [];
    }
  }, [selectedAirport]);

  const handleAirportSelect = (value: string, data?: Airport) => {
    setForm(prev => ({
      ...prev,
      airportIataOrIcao: value,
      locationText: data ? `${data.city}, ${data.country}` : prev.locationText,
    }));
    setSelectedAirport(data || null);
  };

  const handleAircraftSelect = (value: string, data?: Aircraft) => {
    setForm(prev => ({
      ...prev,
      callsign: value,
      icao24: data?.icao24 || prev.icao24,
      // Auto-fill registration, model, and airline from metadata
      registration: data?.registration || prev.registration,
      aircraftModel: data?.model 
        ? `${data.manufacturer || ''} ${data.model}`.trim() 
        : prev.aircraftModel,
      airline: data?.operator || prev.airline,
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setIsSubmitting(true);

    try {
      const token = await getToken();
      
      // Step 1: Create the sighting
      const res = await fetch(`${process.env.NEXT_PUBLIC_BACKEND_URL}/api/sightings`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({
          timestamp: form.timestamp,
          airportIataOrIcao: form.airportIataOrIcao,
          locationText: form.locationText,
          airline: form.airline || undefined,
          callsign: form.callsign || undefined,
          icao24: form.icao24 || undefined,
          registration: form.registration || undefined,
          aircraftModel: form.aircraftModel || undefined,
          notes: form.notes || undefined,
          visibility: form.visibility,
        }),
      });

      if (!res.ok) {
        const errorText = await res.text();
        throw new Error(`Failed to create sighting: ${res.status} - ${errorText}`);
      }

      const sighting = await res.json();
      const sightingId = sighting.id;

      // Step 2: Upload photos if any were selected
      if (selectedPhotos.length > 0) {
        for (const photo of selectedPhotos) {
          const formData = new FormData();
          formData.append('file', photo);

          try {
            await fetch(`${process.env.NEXT_PUBLIC_BACKEND_URL}/api/sightings/${sightingId}/photos`, {
              method: 'POST',
              headers: {
                Authorization: `Bearer ${token}`,
              },
              body: formData,
            });
          } catch (photoError) {
            console.error('Failed to upload photo:', photoError);
            // Continue with other photos even if one fails
          }
        }
      }

      // Redirect to the sighting detail page
      router.push(`/sightings/${sightingId}`);
    } catch (err: any) {
      setError(err.message);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="max-w-xl mx-auto p-4">
      <h1 className="text-2xl font-bold mb-4">Adicionar Avistamento</h1>
      
      {error && (
        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded mb-4">
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-4">
        {/* Date/Time */}
        <div>
          <label className="block mb-1 text-sm font-medium text-gray-700">
            Data e hora <span className="text-red-500">*</span>
          </label>
          <input
            type="datetime-local"
            name="timestamp"
            value={form.timestamp}
            onChange={handleChange}
            className="w-full border rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
            required
          />
        </div>

        {/* Airport Autocomplete */}
        <Autocomplete
          value={form.airportIataOrIcao}
          onChange={handleAirportSelect}
          fetchOptions={fetchAirports}
          label="Aeroporto"
          placeholder="Pesquise por código IATA/ICAO, nome ou cidade..."
          required
          minChars={2}
        />

        {/* Location (auto-filled from airport) */}
        <div>
          <label className="block mb-1 text-sm font-medium text-gray-700">
            Localização
          </label>
          <input
            type="text"
            name="locationText"
            value={form.locationText}
            onChange={handleChange}
            placeholder="Preenchido automaticamente com base no aeroporto"
            className="w-full border rounded px-3 py-2 bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500"
            readOnly={!!selectedAirport}
          />
          {selectedAirport && (
            <p className="text-xs text-gray-500 mt-1">
              Coordenadas: {selectedAirport.latitude.toFixed(4)}°, {selectedAirport.longitude.toFixed(4)}°
            </p>
          )}
        </div>

        {/* Aircraft Autocomplete - only enabled if airport is selected */}
        <Autocomplete
          value={form.callsign}
          onChange={handleAircraftSelect}
          fetchOptions={fetchAircraft}
          label="Callsign (Avião)"
          placeholder={selectedAirport ? "Pesquise por callsign..." : "Selecione um aeroporto primeiro"}
          minChars={3}
          debounceMs={800}
          disabled={!selectedAirport}
        />
        {!selectedAirport && (
          <p className="text-sm text-gray-500 -mt-2">
            Selecione um aeroporto para pesquisar aviões próximos
          </p>
        )}

        {/* ICAO24 (auto-filled from aircraft) */}
        <div>
          <label className="block mb-1 text-sm font-medium text-gray-700">
            ICAO24
          </label>
          <input
            type="text"
            name="icao24"
            value={form.icao24}
            onChange={handleChange}
            placeholder="Preenchido automaticamente ou insira manualmente"
            className="w-full border rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        {/* Registration */}
        <div>
          <label className="block mb-1 text-sm font-medium text-gray-700">
            Matrícula
          </label>
          <input
            type="text"
            name="registration"
            value={form.registration}
            onChange={handleChange}
            placeholder="Ex: CS-TVA, G-EUPP"
            className="w-full border rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        {/* Aircraft Model */}
        <div>
          <label className="block mb-1 text-sm font-medium text-gray-700">
            Modelo do Avião
          </label>
          <input
            type="text"
            name="aircraftModel"
            value={form.aircraftModel}
            onChange={handleChange}
            placeholder="Ex: Airbus A320, Boeing 737-800"
            className="w-full border rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        {/* Airline */}
        <div>
          <label className="block mb-1 text-sm font-medium text-gray-700">
            Companhia Aérea
          </label>
          <input
            type="text"
            name="airline"
            value={form.airline}
            onChange={handleChange}
            placeholder="Ex: TAP, Ryanair, EasyJet"
            className="w-full border rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        {/* Visibility */}
        <div>
          <label className="block mb-1 text-sm font-medium text-gray-700">
            Visibilidade
          </label>
          <select
            name="visibility"
            value={form.visibility}
            onChange={handleChange}
            className="w-full border rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            <option value="PUBLIC">Pública</option>
            <option value="PRIVATE">Privada</option>
          </select>
        </div>

        {/* Photo Upload */}
        <PhotoUpload
          onPhotosChange={setSelectedPhotos}
          maxPhotos={5}
        />

        {/* Notes */}
        <div>
          <label className="block mb-1 text-sm font-medium text-gray-700">
            Notas
          </label>
          <textarea
            name="notes"
            value={form.notes}
            onChange={handleChange}
            placeholder="Observações adicionais sobre o avistamento..."
            className="w-full border rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
            rows={4}
          />
        </div>

        {/* Submit Button */}
        <button
          type="submit"
          disabled={isSubmitting}
          className={`w-full px-4 py-2 text-white rounded font-medium ${
            isSubmitting
              ? 'bg-blue-400 cursor-not-allowed'
              : 'bg-blue-600 hover:bg-blue-700'
          }`}
        >
          {isSubmitting ? (
            <span className="flex items-center justify-center">
              <svg className="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
              A guardar...
            </span>
          ) : (
            'Guardar Avistamento'
          )}
        </button>
      </form>
    </div>
  );
}