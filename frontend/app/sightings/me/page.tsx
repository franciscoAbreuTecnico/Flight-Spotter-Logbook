'use client';

import React from 'react';
import Link from 'next/link';
import { useAuth } from '@clerk/nextjs';
import { useQuery } from '@tanstack/react-query';
import { PlusCircleIcon, EyeIcon, CalendarIcon, MapPinIcon } from '@heroicons/react/24/outline';

interface SightingDto {
  id: number;
  airportIataOrIcao: string;
  timestamp: string;
  airline?: string;
  registration?: string;
  aircraftModel?: string;
  locationText?: string;
  callsign?: string;
}

export default function MyFeedPage() {
  const { getToken } = useAuth();
  const { data, error, isLoading } = useQuery({
    queryKey: ['myFeed'],
    queryFn: async () => {
      const token = await getToken();
      const res = await fetch(`${process.env.NEXT_PUBLIC_BACKEND_URL}/api/sightings/me`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      if (!res.ok) throw new Error('Falha ao obter avistamentos');
      return res.json();
    }
  });

  if (isLoading) {
    return (
      <div className="max-w-5xl mx-auto px-4 py-8 sm:px-6 lg:px-8">
        <div className="animate-pulse space-y-4">
          <div className="h-8 bg-gray-200 rounded w-1/4"></div>
          <div className="space-y-3">
            {[1, 2, 3].map(i => (
              <div key={i} className="h-32 bg-gray-200 rounded-xl"></div>
            ))}
          </div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="max-w-5xl mx-auto px-4 py-8 sm:px-6 lg:px-8">
        <div className="bg-red-50 border border-red-200 rounded-xl p-6">
          <p className="text-red-700 font-medium">Erro: {String(error)}</p>
        </div>
      </div>
    );
  }

  const sightings = data?.content ?? [];

  return (
    <div className="max-w-5xl mx-auto px-4 py-8 sm:px-6 lg:px-8">
      <div className="mb-8 flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-extrabold text-gray-900">My Sightings</h1>
          <p className="text-gray-600 mt-1">
            {sightings.length} {sightings.length === 1 ? 'sighting' : 'sightings'} logged
          </p>
        </div>
        <Link href="/sightings/new">
          <button className="flex items-center space-x-2 px-6 py-3 bg-blue-600 text-white rounded-xl hover:bg-blue-700 transition-all transform hover:scale-105 shadow-lg font-medium">
            <PlusCircleIcon className="h-5 w-5" />
            <span>Add Sighting</span>
          </button>
        </Link>
      </div>

      {sightings.length === 0 ? (
        <div className="bg-white rounded-xl shadow-lg p-12 text-center">
          <div className="text-6xl mb-6">✈️</div>
          <h2 className="text-2xl font-bold text-gray-900 mb-3">No sightings yet</h2>
          <p className="text-gray-600 mb-6">
            Start building your aircraft logbook by adding your first sighting!
          </p>
          <Link href="/sightings/new">
            <button className="inline-flex items-center space-x-2 px-8 py-3 bg-blue-600 text-white rounded-xl hover:bg-blue-700 transition-all font-medium">
              <PlusCircleIcon className="h-5 w-5" />
              <span>Add Your First Sighting</span>
            </button>
          </Link>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {sightings.map((s: SightingDto) => (
            <Link key={s.id} href={`/sightings/${s.id}`} className="group">
              <div className="bg-white rounded-xl shadow-md hover:shadow-xl transition-all transform hover:-translate-y-1 p-6 border-2 border-transparent hover:border-blue-500">
                <div className="flex justify-between items-start mb-4">
                  <div>
                    <h2 className="text-2xl font-bold text-gray-900 group-hover:text-blue-600 transition-colors">
                      {s.airportIataOrIcao}
                    </h2>
                    {s.locationText && (
                      <div className="flex items-center text-gray-600 text-sm mt-1">
                        <MapPinIcon className="h-4 w-4 mr-1" />
                        {s.locationText}
                      </div>
                    )}
                  </div>
                  <div className="text-blue-600 group-hover:scale-110 transition-transform">
                    <EyeIcon className="h-6 w-6" />
                  </div>
                </div>

                <div className="space-y-2">
                  <div className="flex items-center text-gray-600 text-sm">
                    <CalendarIcon className="h-4 w-4 mr-2" />
                    {new Date(s.timestamp).toLocaleString('en-US', {
                      dateStyle: 'medium',
                      timeStyle: 'short'
                    })}
                  </div>
                  
                  {s.callsign && (
                    <p className="text-sm">
                      <span className="font-semibold text-gray-700">Callsign:</span>{' '}
                      <span className="text-gray-900">{s.callsign}</span>
                    </p>
                  )}
                  
                  {s.airline && (
                    <p className="text-sm">
                      <span className="font-semibold text-gray-700">Airline:</span>{' '}
                      <span className="text-gray-900">{s.airline}</span>
                    </p>
                  )}
                  
                  {s.registration && (
                    <p className="text-sm">
                      <span className="font-semibold text-gray-700">Registration:</span>{' '}
                      <span className="text-gray-900 font-mono">{s.registration}</span>
                    </p>
                  )}
                  
                  {s.aircraftModel && (
                    <p className="text-sm text-gray-600 mt-2 font-medium">
                      {s.aircraftModel}
                    </p>
                  )}
                </div>
              </div>
            </Link>
          ))}
        </div>
      )}
    </div>
  );
}