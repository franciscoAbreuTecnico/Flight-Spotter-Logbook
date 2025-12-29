'use client';

import React, { useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { useAuth, useUser } from '@clerk/nextjs';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { PencilIcon, TrashIcon, CalendarIcon, MapPinIcon, InformationCircleIcon, PhotoIcon } from '@heroicons/react/24/outline';

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

interface PhotoDto {
  id: number;
  secureUrl: string;
}

export default function SightingDetailPage() {
  const params = useParams();
  const id = params?.id as string;
  const router = useRouter();
  const { getToken } = useAuth();
  const { user } = useUser();
  const queryClient = useQueryClient();
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);

  const { data: sighting, error: sightingError, isLoading: loadingSighting } = useQuery({
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

  const { data: photos, error: photosError, isLoading: loadingPhotos } = useQuery({
    queryKey: ['photos', id],
    queryFn: async () => {
      const token = await getToken();
      const res = await fetch(`${process.env.NEXT_PUBLIC_BACKEND_URL}/api/sightings/${id}/photos`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      if (!res.ok) throw new Error('Failed to fetch photos');
      return res.json() as Promise<PhotoDto[]>;
    },
    enabled: !!id
  });

  const deleteMutation = useMutation({
    mutationFn: async () => {
      const token = await getToken();
      const res = await fetch(`${process.env.NEXT_PUBLIC_BACKEND_URL}/api/sightings/${id}`, {
        method: 'DELETE',
        headers: { Authorization: `Bearer ${token}` },
      });
      if (!res.ok) throw new Error('Failed to delete sighting');
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['sightings'] });
      router.push('/sightings/me');
    },
  });

  const retryEnrichmentMutation = useMutation({
    mutationFn: async () => {
      const token = await getToken();
      const res = await fetch(`${process.env.NEXT_PUBLIC_BACKEND_URL}/api/sightings/${id}/enrich`, {
        method: 'POST',
        headers: { Authorization: `Bearer ${token}` },
      });
      if (!res.ok) throw new Error('Failed to retry enrichment');
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['sighting', id] });
    },
  });

  // Fetch user's role from backend (secure)
  const { data: userRole } = useQuery({
    queryKey: ['userRole'],
    queryFn: async () => {
      const token = await getToken();
      const res = await fetch(
        `${process.env.NEXT_PUBLIC_BACKEND_URL}/api/admin/roles/me`,
        {
          headers: { Authorization: `Bearer ${token}` },
        }
      );
      if (!res.ok) return 'USER';
      const roleText = await res.text();
      return roleText.trim();
    },
    enabled: !!user,
  });

  const handleDelete = () => {
    if (showDeleteConfirm) {
      deleteMutation.mutate();
    } else {
      setShowDeleteConfirm(true);
    }
  };

  const handleEdit = () => {
    router.push(`/sightings/${id}/edit`);
  };

  const handleRetryEnrichment = () => {
    retryEnrichmentMutation.mutate();
  };

  if (loadingSighting || loadingPhotos) {
    return (
      <div className="max-w-5xl mx-auto px-4 py-8 sm:px-6 lg:px-8">
        <div className="animate-pulse space-y-6">
          <div className="h-8 bg-gray-200 rounded w-1/3"></div>
          <div className="bg-white rounded-xl p-6 space-y-4">
            <div className="h-6 bg-gray-200 rounded w-3/4"></div>
            <div className="h-4 bg-gray-200 rounded w-1/2"></div>
            <div className="h-4 bg-gray-200 rounded w-2/3"></div>
          </div>
        </div>
      </div>
    );
  }

  if (sightingError) {
    return (
      <div className="max-w-5xl mx-auto px-4 py-8 sm:px-6 lg:px-8">
        <div className="bg-red-50 border border-red-200 rounded-xl p-6">
          <p className="text-red-700 font-medium">Error loading sighting: {String(sightingError)}</p>
        </div>
      </div>
    );
  }

  if (!sighting) {
    return (
      <div className="max-w-5xl mx-auto px-4 py-8 sm:px-6 lg:px-8">
        <div className="bg-yellow-50 border border-yellow-200 rounded-xl p-6 text-center">
          <p className="text-yellow-700 font-medium">Sighting not found</p>
        </div>
      </div>
    );
  }

  // Check if current user is the owner or admin
  const isOwner = user?.id === sighting.ownerUserId;
  const isAdmin = userRole === 'ADMIN';

  return (
    <div className="max-w-5xl mx-auto px-4 py-8 sm:px-6 lg:px-8">
      {/* Header with action buttons */}
      <div className="mb-6">
        <div className="flex flex-col sm:flex-row sm:justify-between sm:items-center gap-4 mb-4">
          <h1 className="text-3xl font-extrabold text-gray-900">Sighting Details</h1>
          {isOwner && (
            <div className="flex gap-3">
              <button
                onClick={handleEdit}
                className="flex items-center space-x-2 px-5 py-2.5 bg-blue-600 text-white rounded-xl hover:bg-blue-700 transition-all transform hover:scale-105 font-medium shadow-md"
              >
                <PencilIcon className="h-5 w-5" />
                <span>Edit</span>
              </button>
              {!showDeleteConfirm ? (
                <button
                  onClick={handleDelete}
                  disabled={deleteMutation.isPending}
                  className="flex items-center space-x-2 px-5 py-2.5 bg-red-600 text-white rounded-xl hover:bg-red-700 transition-all transform hover:scale-105 font-medium shadow-md disabled:opacity-50"
                >
                  <TrashIcon className="h-5 w-5" />
                  <span>Delete</span>
                </button>
              ) : (
                <div className="flex gap-2">
                  <button
                    onClick={handleDelete}
                    disabled={deleteMutation.isPending}
                    className="flex items-center space-x-2 px-5 py-2.5 bg-red-700 text-white rounded-xl hover:bg-red-800 transition-all font-medium shadow-md disabled:opacity-50"
                  >
                    <TrashIcon className="h-5 w-5" />
                    <span>{deleteMutation.isPending ? 'Deleting...' : 'Confirm Delete'}</span>
                  </button>
                  <button
                    onClick={() => setShowDeleteConfirm(false)}
                    className="px-5 py-2.5 bg-gray-400 text-white rounded-xl hover:bg-gray-500 transition-all font-medium shadow-md"
                  >
                    Cancel
                  </button>
                </div>
              )}
            </div>
          )}
        </div>

        {/* Enrichment Status Banner */}
        {isOwner && sighting.enrichmentStatus && sighting.enrichmentStatus !== 'ENRICHED' && (
          <div className="bg-amber-50 border border-amber-200 rounded-xl p-4 flex items-center justify-between">
            <div className="flex items-center space-x-3">
              <InformationCircleIcon className="h-6 w-6 text-amber-600" />
              <div>
                <p className="text-sm font-medium text-amber-900">Enrichment Status: {sighting.enrichmentStatus}</p>
                <p className="text-xs text-amber-700 mt-1">
                  {sighting.enrichmentStatus === 'FAILED' 
                    ? 'Enrichment failed. Click retry to attempt again.' 
                    : sighting.enrichmentStatus === 'ENRICHING' 
                    ? 'Enrichment in progress...' 
                    : 'Enrichment not started yet.'}
                </p>
              </div>
            </div>
            {sighting.enrichmentStatus === 'FAILED' && (
              <button
                onClick={handleRetryEnrichment}
                disabled={retryEnrichmentMutation.isPending}
                className="flex items-center space-x-2 px-4 py-2 bg-amber-600 text-white rounded-lg hover:bg-amber-700 transition-all font-medium shadow-sm disabled:opacity-50"
              >
                <span>{retryEnrichmentMutation.isPending ? 'Retrying...' : 'Retry'}</span>
              </button>
            )}
          </div>
        )}
      </div>

      {/* Main content grid */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left column - Main details */}
        <div className="lg:col-span-2 space-y-6">
          {/* Flight Details Card */}
          <div className="bg-white rounded-xl shadow-lg p-6 border border-gray-200">
            <h2 className="text-xl font-bold text-gray-900 mb-4 flex items-center">
              <InformationCircleIcon className="h-6 w-6 mr-2 text-blue-600" />
              Flight Information
            </h2>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <label className="text-sm font-semibold text-gray-600 uppercase tracking-wide">Airport</label>
                <p className="text-lg font-bold text-gray-900 mt-1">{sighting.airportIataOrIcao}</p>
              </div>
              
              <div>
                <label className="text-sm font-semibold text-gray-600 uppercase tracking-wide flex items-center">
                  <CalendarIcon className="h-4 w-4 mr-1" />
                  Date & Time
                </label>
                <p className="text-lg text-gray-900 mt-1">
                  {new Date(sighting.timestamp).toLocaleString('en-US', {
                    dateStyle: 'medium',
                    timeStyle: 'short'
                  })}
                </p>
              </div>

              {sighting.locationText && (
                <div>
                  <label className="text-sm font-semibold text-gray-600 uppercase tracking-wide flex items-center">
                    <MapPinIcon className="h-4 w-4 mr-1" />
                    Location
                  </label>
                  <p className="text-lg text-gray-900 mt-1">{sighting.locationText}</p>
                </div>
              )}

              {sighting.callsign && (
                <div>
                  <label className="text-sm font-semibold text-gray-600 uppercase tracking-wide">Callsign</label>
                  <p className="text-lg font-mono font-bold text-gray-900 mt-1">{sighting.callsign}</p>
                </div>
              )}

              {sighting.airline && (
                <div>
                  <label className="text-sm font-semibold text-gray-600 uppercase tracking-wide">Airline</label>
                  <p className="text-lg text-gray-900 mt-1">{sighting.airline}</p>
                </div>
              )}

              {sighting.registration && (
                <div>
                  <label className="text-sm font-semibold text-gray-600 uppercase tracking-wide">Registration</label>
                  <p className="text-lg font-mono font-bold text-gray-900 mt-1">{sighting.registration}</p>
                </div>
              )}

              {sighting.icao24 && (
                <div>
                  <label className="text-sm font-semibold text-gray-600 uppercase tracking-wide">ICAO24</label>
                  <p className="text-lg font-mono text-gray-900 mt-1">{sighting.icao24}</p>
                </div>
              )}

              {sighting.aircraftModel && (
                <div className="sm:col-span-2">
                  <label className="text-sm font-semibold text-gray-600 uppercase tracking-wide">Aircraft Model</label>
                  <p className="text-lg font-bold text-gray-900 mt-1">{sighting.aircraftModel}</p>
                </div>
              )}
            </div>
          </div>

          {/* Notes Card */}
          {sighting.notes && (
            <div className="bg-white rounded-xl shadow-lg p-6 border border-gray-200">
              <h2 className="text-xl font-bold text-gray-900 mb-3">Notes</h2>
              <p className="text-gray-700 leading-relaxed whitespace-pre-wrap">{sighting.notes}</p>
            </div>
          )}

          {/* Photos Section */}
          <div className="bg-white rounded-xl shadow-lg p-6 border border-gray-200">
            <h2 className="text-xl font-bold text-gray-900 mb-4 flex items-center">
              <PhotoIcon className="h-6 w-6 mr-2 text-blue-600" />
              Photos
            </h2>
            {photosError && (
              <div className="bg-red-50 border border-red-200 rounded-lg p-4">
                <p className="text-red-700">Error loading photos: {String(photosError)}</p>
              </div>
            )}
            {photos && photos.length > 0 ? (
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                {photos.map((photo) => (
                  <div key={photo.id} className="relative aspect-video bg-gray-200 rounded-lg overflow-hidden shadow-md hover:shadow-xl transition-shadow">
                    {/* eslint-disable-next-line @next/next/no-img-element */}
                    <img 
                      src={photo.secureUrl} 
                      alt="Sighting photo" 
                      className="object-cover w-full h-full" 
                    />
                    {/* Delete button - only show if owner or admin */}
                    {(sighting.ownerUserId === user?.id || isAdmin) && (
                      <button
                        onClick={async () => {
                          if (!confirm('Delete this photo?')) return;
                          try {
                            const token = await getToken();
                            const res = await fetch(
                              `${process.env.NEXT_PUBLIC_BACKEND_URL}/api/sightings/${id}/photos/${photo.id}`,
                              {
                                method: 'DELETE',
                                headers: { Authorization: `Bearer ${token}` },
                              }
                            );
                            if (res.ok) {
                              queryClient.invalidateQueries({ queryKey: ['photos', id] });
                            } else {
                              alert('Failed to delete photo');
                            }
                          } catch (error) {
                            console.error('Error deleting photo:', error);
                            alert('Error deleting photo');
                          }
                        }}
                        className="absolute top-2 right-2 bg-red-600 text-white p-2 rounded-full hover:bg-red-700 shadow-lg transition-colors"
                        title="Delete photo"
                      >
                        <TrashIcon className="h-5 w-5" />
                      </button>
                    )}
                  </div>
                ))}
              </div>
            ) : (
              <div className="text-center py-8 text-gray-500">
                <PhotoIcon className="h-16 w-16 mx-auto mb-3 text-gray-300" />
                <p>No photos available for this sighting</p>
              </div>
            )}
          </div>
        </div>

        {/* Right column - Metadata */}
        <div className="space-y-6">
          <div className="bg-gradient-to-br from-blue-50 to-blue-100 rounded-xl shadow-lg p-6 border border-blue-200">
            <h3 className="text-lg font-bold text-gray-900 mb-4">Metadata</h3>
            <div className="space-y-3">
              <div>
                <label className="text-xs font-semibold text-gray-600 uppercase tracking-wide">Visibility</label>
                <p className="text-sm mt-1">
                  <span className={`inline-flex items-center px-3 py-1 rounded-full text-sm font-medium ${
                    sighting.visibility === 'PUBLIC' 
                      ? 'bg-green-100 text-green-800' 
                      : 'bg-gray-100 text-gray-800'
                  }`}>
                    {sighting.visibility}
                  </span>
                </p>
              </div>
              <div>
                <label className="text-xs font-semibold text-gray-600 uppercase tracking-wide">Enrichment Status</label>
                <p className="text-sm mt-1">
                  <span className={`inline-flex items-center px-3 py-1 rounded-full text-sm font-medium ${
                    sighting.enrichmentStatus === 'COMPLETE'
                      ? 'bg-green-100 text-green-800'
                      : sighting.enrichmentStatus === 'PENDING'
                      ? 'bg-yellow-100 text-yellow-800'
                      : 'bg-gray-100 text-gray-800'
                  }`}>
                    {sighting.enrichmentStatus}
                  </span>
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}