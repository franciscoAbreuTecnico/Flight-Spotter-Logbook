'use client';

import React, { useState, useCallback, useEffect } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { useAuth, useUser } from '@clerk/nextjs';
import { useQuery, useMutation } from '@tanstack/react-query';
import Autocomplete from '../../../components/Autocomplete';
import PhotoUpload from '../../../components/PhotoUpload';

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

interface SightingDetail {
  id: number;
  ownerUserId: string;
  timestamp: string;
  airportIataOrIcao: string;
  locationText?: string;
  airline?: string;
  callsign?: string;
  icao24?: string;
  registration?: string;
  aircraftModel?: string;
  notes?: string;
  visibility: string;
  enrichmentStatus: string;
}

export default function EditSightingPage() {
  const params = useParams();
  const id = params?.id as string;
  const router = useRouter();
  const { getToken } = useAuth();
  const { user } = useUser();
  const [error, setError] = useState<string | null>(null);
  const [selectedAirport, setSelectedAirport] = useState<Airport | null>(null);
  const [selectedPhotos, setSelectedPhotos] = useState<File[]>([]);

  const { data: sighting, isLoading } = useQuery({
    queryKey: ['sighting', id],
    queryFn: async () => {
      const token = await getToken();
      const res = await fetch(`${process.env.NEXT_PUBLIC_BACKEND_URL}/api/sightings/${id}`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      if (!res.ok) throw new Error('Failed to fetch sighting');
      return res.json() as Promise<SightingDetail>;
    },
    enabled: !!id
  });

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

  // Pre-fill form when sighting data loads
  useEffect(() => {
    if (sighting) {
      // Check if current user is the owner
      if (user?.id !== sighting.ownerUserId) {
        router.push(`/sightings/${id}`);
        return;
      }

      setForm({
        timestamp: new Date(sighting.timestamp).toISOString().slice(0, 16),
        airportIataOrIcao: sighting.airportIataOrIcao || '',
        locationText: sighting.locationText || '',
        airline: sighting.airline || '',
        callsign: sighting.callsign || '',
        icao24: sighting.icao24 || '',
        registration: sighting.registration || '',
        aircraftModel: sighting.aircraftModel || '',
        notes: sighting.notes || '',
        visibility: sighting.visibility as 'PUBLIC' | 'PRIVATE',
      });
    }
  }, [sighting, user, router, id]);

  const updateMutation = useMutation({
    mutationFn: async (data: FormState) => {
      const token = await getToken();
      const res = await fetch(`${process.env.NEXT_PUBLIC_BACKEND_URL}/api/sightings/${id}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(data),
      });
      if (!res.ok) {
        const errorText = await res.text();
        throw new Error(errorText || 'Failed to update sighting');
      }
      return res.json();
    },
    onSuccess: async () => {
      // Upload new photos if any were selected
      if (selectedPhotos.length > 0) {
        const token = await getToken();
        for (const photo of selectedPhotos) {
          const formData = new FormData();
          formData.append('file', photo);

          try {
            await fetch(`${process.env.NEXT_PUBLIC_BACKEND_URL}/api/sightings/${id}/photos`, {
              method: 'POST',
              headers: {
                Authorization: `Bearer ${token}`,
              },
              body: formData,
            });
          } catch (photoError) {
            console.error('Failed to upload photo:', photoError);
          }
        }
      }
      router.push(`/sightings/${id}`);
    },
    onError: (err: Error) => {
      setError(err.message);
    },
  });

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

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

  const fetchAircraft = useCallback(async (query: string) => {
    if (!selectedAirport) {
      return [];
    }
    
    try {
      const params = new URLSearchParams({
        q: query,
        lat: selectedAirport.latitude.toString(),
        lon: selectedAirport.longitude.toString(),
      });
      
      const res = await fetch(`${process.env.NEXT_PUBLIC_BACKEND_URL}/api/lookup/aircraft?${params}`);
      if (!res.ok) return [];
      const aircraft: Aircraft[] = await res.json();
      return aircraft.map(a => {
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
      registration: data?.registration || prev.registration,
      aircraftModel: data?.model 
        ? `${data.manufacturer || ''} ${data.model}`.trim() 
        : prev.aircraftModel,
      airline: data?.operator || prev.airline,
    }));
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    updateMutation.mutate(form);
  };

  if (isLoading) return <div className="p-4">A carregar…</div>;
  if (!sighting) return <div className="p-4">Avistamento não encontrado.</div>;

  return (
    <div className="max-w-xl mx-auto p-4">
      <h1 className="text-2xl font-bold mb-4">Editar Avistamento</h1>
      
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
            required
            className="w-full border rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        {/* Airport Autocomplete */}
        <Autocomplete
          value={form.airportIataOrIcao}
          onChange={handleAirportSelect}
          fetchOptions={fetchAirports}
          label="Aeroporto"
          placeholder="Pesquise por ICAO, IATA ou nome..."
          minChars={2}
          required
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

        {/* Aircraft Autocomplete */}
        <Autocomplete
          value={form.callsign}
          onChange={handleAircraftSelect}
          fetchOptions={fetchAircraft}
          label="Callsign (Avião)"
          placeholder={selectedAirport ? "Pesquise por callsign..." : "Selecione um aeroporto primeiro"}
          minChars={2}
          disabled={!selectedAirport}
        />
        {!selectedAirport && (
          <p className="text-sm text-gray-500 -mt-2">
            Selecione um aeroporto para pesquisar aviões próximos
          </p>
        )}

        {/* ICAO24 */}
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

        {/* Notes */}
        <div>
          <label className="block mb-1 text-sm font-medium text-gray-700">
            Notas
          </label>
          <textarea
            name="notes"
            value={form.notes}
            onChange={handleChange}
            rows={4}
            placeholder="Observações adicionais sobre o avistamento..."
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

        {/* Photo Upload - Add new photos */}
        <div>
          <PhotoUpload
            onPhotosChange={setSelectedPhotos}
            maxPhotos={5}
          />
          <p className="text-sm text-gray-500 mt-2">
            Note: Existing photos will remain. You can add up to 5 new photos.
          </p>
        </div>

        <div className="flex gap-2">
          <button
            type="submit"
            disabled={updateMutation.isPending}
            className="flex-1 bg-blue-600 text-white py-2 rounded hover:bg-blue-700 disabled:opacity-50 transition"
          >
            {updateMutation.isPending ? 'A guardar...' : 'Guardar Alterações'}
          </button>
          <button
            type="button"
            onClick={() => router.push(`/sightings/${id}`)}
            className="px-6 bg-gray-400 text-white py-2 rounded hover:bg-gray-500 transition"
          >
            Cancelar
          </button>
        </div>
      </form>
    </div>
  );
}
