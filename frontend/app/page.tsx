import React from 'react';
import Link from 'next/link';
import { SignedIn, SignedOut, SignInButton } from '@clerk/nextjs';
import { PlusCircleIcon, GlobeAltIcon, ListBulletIcon, SparklesIcon } from '@heroicons/react/24/outline';

export default function HomePage() {
  return (
    <div className="min-h-[calc(100vh-4rem)]">
      <SignedOut>
        <div className="max-w-5xl mx-auto px-4 py-16 sm:px-6 lg:px-8">
          <div className="text-center">
            <div className="flex justify-center mb-8">
              <div className="text-8xl animate-bounce">✈️</div>
            </div>
            <h1 className="text-5xl font-extrabold text-gray-900 mb-6 tracking-tight">
              Flight Spotter Logbook
            </h1>
            <p className="text-xl text-gray-600 mb-10 max-w-2xl mx-auto leading-relaxed">
              Record and share your aircraft sightings with fellow aviation enthusiasts. 
              Track planes, airports, and build your personal aviation diary.
            </p>
            <SignInButton mode="modal">
              <button className="px-8 py-4 bg-blue-600 text-white rounded-xl hover:bg-blue-700 transition-all transform hover:scale-105 font-semibold text-lg shadow-lg hover:shadow-xl">
                Get Started - Sign In
              </button>
            </SignInButton>
            
            <div className="mt-20 grid grid-cols-1 md:grid-cols-3 gap-8 text-left">
              <div className="bg-white rounded-xl p-6 shadow-lg hover:shadow-xl transition-shadow">
                <div className="text-blue-600 mb-4">
                  <PlusCircleIcon className="h-12 w-12" />
                </div>
                <h3 className="text-xl font-bold mb-2">Log Sightings</h3>
                <p className="text-gray-600">
                  Easily record aircraft details, airports, and your observations with smart autocomplete.
                </p>
              </div>
              <div className="bg-white rounded-xl p-6 shadow-lg hover:shadow-xl transition-shadow">
                <div className="text-blue-600 mb-4">
                  <SparklesIcon className="h-12 w-12" />
                </div>
                <h3 className="text-xl font-bold mb-2">Auto-Fill Data</h3>
                <p className="text-gray-600">
                  Get aircraft metadata automatically - registration, model, and airline information.
                </p>
              </div>
              <div className="bg-white rounded-xl p-6 shadow-lg hover:shadow-xl transition-shadow">
                <div className="text-blue-600 mb-4">
                  <GlobeAltIcon className="h-12 w-12" />
                </div>
                <h3 className="text-xl font-bold mb-2">Share & Explore</h3>
                <p className="text-gray-600">
                  Share your sightings publicly or keep them private. Explore what others have spotted.
                </p>
              </div>
            </div>
          </div>
        </div>
      </SignedOut>

      <SignedIn>
        <div className="max-w-7xl mx-auto px-4 py-12 sm:px-6 lg:px-8">
          <div className="text-center mb-12">
            <h1 className="text-4xl font-extrabold text-gray-900 mb-4">
              Welcome Back! ✈️
            </h1>
            <p className="text-lg text-gray-600">
              Ready to log your next aircraft sighting?
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 max-w-5xl mx-auto">
            <Link href="/sightings/new" className="group">
              <div className="bg-white rounded-xl p-8 shadow-lg hover:shadow-2xl transition-all transform hover:-translate-y-1 border-2 border-transparent hover:border-blue-500">
                <div className="text-blue-600 mb-4 flex justify-center">
                  <PlusCircleIcon className="h-16 w-16 group-hover:scale-110 transition-transform" />
                </div>
                <h2 className="text-2xl font-bold text-center mb-2">Add Sighting</h2>
                <p className="text-gray-600 text-center">
                  Log a new aircraft sighting with smart autocomplete
                </p>
              </div>
            </Link>

            <Link href="/sightings/me" className="group">
              <div className="bg-white rounded-xl p-8 shadow-lg hover:shadow-2xl transition-all transform hover:-translate-y-1 border-2 border-transparent hover:border-green-500">
                <div className="text-green-600 mb-4 flex justify-center">
                  <ListBulletIcon className="h-16 w-16 group-hover:scale-110 transition-transform" />
                </div>
                <h2 className="text-2xl font-bold text-center mb-2">My Sightings</h2>
                <p className="text-gray-600 text-center">
                  View and manage your aircraft logbook
                </p>
              </div>
            </Link>

            <Link href="/explore" className="group">
              <div className="bg-white rounded-xl p-8 shadow-lg hover:shadow-2xl transition-all transform hover:-translate-y-1 border-2 border-transparent hover:border-purple-500">
                <div className="text-purple-600 mb-4 flex justify-center">
                  <GlobeAltIcon className="h-16 w-16 group-hover:scale-110 transition-transform" />
                </div>
                <h2 className="text-2xl font-bold text-center mb-2">Explore</h2>
                <p className="text-gray-600 text-center">
                  Discover public sightings from the community
                </p>
              </div>
            </Link>
          </div>
        </div>
      </SignedIn>
    </div>
  );
}