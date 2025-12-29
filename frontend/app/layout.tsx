import '@/app/globals.css';
import { ClerkProvider } from '@clerk/nextjs';
import React from 'react';
import Providers from './providers';
import Navigation from './components/Navigation';

export const metadata = {
  title: 'Flight Spotter Logbook',
  description: 'Record and share aircraft sightings',
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <ClerkProvider>
      <html lang="en">
        <body className="min-h-screen bg-gradient-to-br from-gray-50 to-blue-50 text-gray-900">
          <Providers>
            <Navigation />
            <main className="pb-8">
              {children}
            </main>
          </Providers>
        </body>
      </html>
    </ClerkProvider>
  );
}