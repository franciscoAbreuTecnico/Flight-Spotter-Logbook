'use client';

import React from 'react';
import Link from 'next/link';
import { useQuery } from '@tanstack/react-query';
import { EyeIcon, CalendarIcon, MapPinIcon, GlobeAltIcon } from '@heroicons/react/24/outline';

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

export default function ExplorePage() {
  const { data, error, isLoading } = useQuery({
    queryKey: ['explore'],
    queryFn: async () => {
      const res = await fetch(`${process.env.NEXT_PUBLIC_BACKEND_URL}/api/sightings`);
      if (!res.ok) {
        throw new Error('Failed to fetch sightings');
      }
      return res.json();
    }
  });

  if (isLoading) {
    return (
      <div className="max-w-6xl mx-auto px-4 py-8 sm:px-6 lg:px-8">
        <div className="animate-pulse space-y-4">
          <div className="h-8 bg-gray-200 rounded w-1/4"></div>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {[1, 2, 3, 4, 5, 6].map(i => (
              <div key={i} className="h-48 bg-gray-200 rounded-xl"></div>
            ))}
          </div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="max-w-6xl mx-auto px-4 py-8 sm:px-6 lg:px-8">
        <div className="bg-red-50 border border-red-200 rounded-xl p-6">
          <p className="text-red-700 font-medium">Error: {String(error)}</p>
        </div>
      </div>
    );
  }

  const sightings = data?.content ?? [];

  return (
    <div className="max-w-6xl mx-auto px-4 py-8 sm:px-6 lg:px-8">
      <div className="mb-8">
        <div className="flex items-center space-x-3 mb-2">
          <GlobeAltIcon className="h-8 w-8 text-purple-600" />
          <h1 className="text-3xl font-extrabold text-gray-900">Explore Sightings</h1>
        </div>
        <p className="text-gray-600">
          Discover public aircraft sightings from the community
        </p>
      </div>

      {sightings.length === 0 ? (
        <div className="bg-white rounded-xl shadow-lg p-12 text-center">
          <div className="text-6xl mb-6">🔍</div>
          <h2 className="text-2xl font-bold text-gray-900 mb-3">No public sightings yet</h2>
          <p className="text-gray-600">
            Be the first to share a public sighting with the community!
          </p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {sightings.map((s: SightingDto) => (
            <Link key={s.id} href={`/sightings/${s.id}`} className="group">
              <div className="bg-white rounded-xl shadow-md hover:shadow-xl transition-all transform hover:-translate-y-1 p-6 h-full border-2 border-transparent hover:border-purple-500">
                <div className="flex justify-between items-start mb-4">
                  <div>
                    <h2 className="text-xl font-bold text-gray-900 group-hover:text-purple-600 transition-colors">
                      {s.airportIataOrIcao}
                    </h2>
                    {s.locationText && (
                      <div className="flex items-center text-gray-600 text-sm mt-1">
                        <MapPinIcon className="h-4 w-4 mr-1" />
                        {s.locationText}
                      </div>
                    )}
                  </div>
                  <div className="text-purple-600 group-hover:scale-110 transition-transform">
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
                    <p className="text-sm text-gray-600 mt-2 font-medium truncate">
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