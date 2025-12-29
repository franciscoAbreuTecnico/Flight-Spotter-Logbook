'use client';

import React, { useState } from 'react';
import Link from 'next/link';
import { useAuth, useUser } from '@clerk/nextjs';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';

interface SightingDto {
  id: number;
  airportIataOrIcao: string;
  timestamp: string;
  ownerUserId: string;
  visibility: string;
  aircraftModel?: string;
  registration?: string;
  airline?: string;
}

export default function AdminPage() {
  const { getToken } = useAuth();
  const { user } = useUser();
  const queryClient = useQueryClient();
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(50);
  
  // Fetch user's role from backend (secure)
  const { data: userRole, isLoading: roleLoading } = useQuery({
    queryKey: ['userRole'],
    queryFn: async () => {
      const token = await getToken();
      const res = await fetch(
        `${process.env.NEXT_PUBLIC_BACKEND_URL}/api/admin/roles/me`,
        {
          headers: { Authorization: `Bearer ${token}` },
        }
      );
      if (!res.ok) {
        console.error('Role fetch failed:', res.status);
        return 'USER'; // Default to USER if error
      }
      const roleText = await res.text();
      console.log('Fetched role:', roleText, 'Type:', typeof roleText);
      return roleText.trim(); // Trim whitespace
    },
    enabled: !!user,
  });
  
  const { data, error, isLoading } = useQuery({
    queryKey: ['adminSightings', page, pageSize],
    queryFn: async () => {
      const token = await getToken();
      const res = await fetch(
        `${process.env.NEXT_PUBLIC_BACKEND_URL}/api/sightings/admin/all?page=${page}&size=${pageSize}`,
        {
          headers: { Authorization: `Bearer ${token}` },
        }
      );
      if (!res.ok) {
        const errorText = await res.text();
        throw new Error(`Failed to fetch sightings: ${res.status} - ${errorText}`);
      }
      return res.json();
    },
    enabled: !!user && userRole === 'ADMIN',
  });

  const deleteMutation = useMutation({
    mutationFn: async (id: number) => {
      const token = await getToken();
      const res = await fetch(`${process.env.NEXT_PUBLIC_BACKEND_URL}/api/sightings/${id}`, {
        method: 'DELETE',
        headers: { Authorization: `Bearer ${token}` },
      });
      if (!res.ok) throw new Error('Failed to delete sighting');
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['adminSightings'] });
    },
  });

  if (roleLoading) {
    return (
      <div className="max-w-6xl mx-auto p-4">
        <div className="animate-pulse">Checking permissions...</div>
      </div>
    );
  }
  
  console.log('Current userRole:', userRole, 'Is Admin?', userRole === 'ADMIN');
  
  if (userRole !== 'ADMIN') {
    return (
      <div className="max-w-4xl mx-auto p-4">
        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
          <strong>Access Restricted:</strong> This page is only accessible to administrators.
          <div className="mt-2 text-sm">Your role: {userRole || 'undefined'}</div>
        </div>
      </div>
    );
  }

  if (isLoading) {
    return (
      <div className="max-w-6xl mx-auto p-4">
        <div className="animate-pulse">Loading admin dashboard...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="max-w-6xl mx-auto p-4">
        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
          <strong>Error:</strong> {String(error)}
        </div>
      </div>
    );
  }

  const sightings = data?.content ?? [];
  const totalElements = data?.totalElements ?? 0;
  const totalPages = data?.totalPages ?? 0;

  return (
    <div className="max-w-6xl mx-auto p-4">
      <div className="mb-6">
        <h1 className="text-3xl font-bold mb-2">Admin Dashboard</h1>
        <p className="text-gray-600">
          Manage all sightings across the platform
        </p>
      </div>

      {/* Statistics */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
        <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
          <div className="text-sm text-blue-600 font-medium">Total Sightings</div>
          <div className="text-2xl font-bold text-blue-900">{totalElements}</div>
        </div>
        <div className="bg-green-50 border border-green-200 rounded-lg p-4">
          <div className="text-sm text-green-600 font-medium">Current Page</div>
          <div className="text-2xl font-bold text-green-900">{page + 1} / {totalPages || 1}</div>
        </div>
        <div className="bg-purple-50 border border-purple-200 rounded-lg p-4">
          <div className="text-sm text-purple-600 font-medium">Per Page</div>
          <div className="text-2xl font-bold text-purple-900">{pageSize}</div>
        </div>
      </div>

      {/* Controls */}
      <div className="mb-4 flex justify-between items-center">
        <div className="flex items-center gap-2">
          <label className="text-sm font-medium">Page size:</label>
          <select
            value={pageSize}
            onChange={(e) => {
              setPageSize(Number(e.target.value));
              setPage(0);
            }}
            className="border rounded px-2 py-1"
          >
            <option value="25">25</option>
            <option value="50">50</option>
            <option value="100">100</option>
          </select>
        </div>
        
        <div className="flex gap-2">
          <button
            onClick={() => setPage(Math.max(0, page - 1))}
            disabled={page === 0}
            className="px-3 py-1 bg-gray-200 rounded disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-300"
          >
            Previous
          </button>
          <button
            onClick={() => setPage(Math.min(totalPages - 1, page + 1))}
            disabled={page >= totalPages - 1}
            className="px-3 py-1 bg-gray-200 rounded disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-300"
          >
            Next
          </button>
        </div>
      </div>

      {/* Table */}
      {sightings.length === 0 ? (
        <div className="text-center py-8 text-gray-500">
          No sightings found.
        </div>
      ) : (
        <div className="overflow-x-auto bg-white rounded-lg shadow">
          <table className="w-full text-left border-collapse">
            <thead className="bg-gray-50">
              <tr>
                <th className="border-b px-4 py-3 text-sm font-semibold text-gray-700">ID</th>
                <th className="border-b px-4 py-3 text-sm font-semibold text-gray-700">Airport</th>
                <th className="border-b px-4 py-3 text-sm font-semibold text-gray-700">Aircraft</th>
                <th className="border-b px-4 py-3 text-sm font-semibold text-gray-700">Registration</th>
                <th className="border-b px-4 py-3 text-sm font-semibold text-gray-700">Date</th>
                <th className="border-b px-4 py-3 text-sm font-semibold text-gray-700">Owner</th>
                <th className="border-b px-4 py-3 text-sm font-semibold text-gray-700">Visibility</th>
                <th className="border-b px-4 py-3 text-sm font-semibold text-gray-700">Actions</th>
              </tr>
            </thead>
            <tbody>
              {sightings.map((s: SightingDto) => (
                <tr key={s.id} className="border-b hover:bg-gray-50">
                  <td className="px-4 py-3 text-sm">{s.id}</td>
                  <td className="px-4 py-3 text-sm font-medium">{s.airportIataOrIcao}</td>
                  <td className="px-4 py-3 text-sm">{s.aircraftModel || '-'}</td>
                  <td className="px-4 py-3 text-sm">{s.registration || '-'}</td>
                  <td className="px-4 py-3 text-sm">
                    {new Date(s.timestamp).toLocaleDateString()}
                  </td>
                  <td className="px-4 py-3 text-sm text-gray-600">
                    {s.ownerUserId.substring(0, 8)}...
                  </td>
                  <td className="px-4 py-3 text-sm">
                    <span
                      className={`px-2 py-1 rounded text-xs font-medium ${
                        s.visibility === 'PUBLIC'
                          ? 'bg-green-100 text-green-800'
                          : 'bg-yellow-100 text-yellow-800'
                      }`}
                    >
                      {s.visibility}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-sm space-x-2">
                    <Link
                      href={`/sightings/${s.id}`}
                      className="text-blue-600 hover:text-blue-800 underline"
                    >
                      View
                    </Link>
                    <button
                      onClick={() => {
                        if (confirm(`Are you sure you want to delete sighting #${s.id}?`)) {
                          deleteMutation.mutate(s.id);
                        }
                      }}
                      disabled={deleteMutation.isPending}
                      className="text-red-600 hover:text-red-800 underline disabled:opacity-50"
                    >
                      {deleteMutation.isPending ? 'Deleting...' : 'Delete'}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Pagination info */}
      <div className="mt-4 text-sm text-gray-600 text-center">
        Showing {page * pageSize + 1} to {Math.min((page + 1) * pageSize, totalElements)} of {totalElements} total sightings
      </div>
    </div>
  );
}