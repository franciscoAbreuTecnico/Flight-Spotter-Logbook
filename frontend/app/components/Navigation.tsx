'use client';

import Link from 'next/link';
import { usePathname, useRouter } from 'next/navigation';
import { SignedIn, SignedOut, SignInButton, UserButton } from '@clerk/nextjs';
import { HomeIcon, PlusIcon, ListBulletIcon, GlobeAltIcon, ArrowLeftIcon } from '@heroicons/react/24/outline';

export default function Navigation() {
  const pathname = usePathname();
  const router = useRouter();
  const showBackButton = pathname !== '/';

  const navItems = [
    { href: '/', label: 'Home', icon: HomeIcon },
    { href: '/sightings/new', label: 'New', icon: PlusIcon },
    { href: '/sightings/me', label: 'My Sightings', icon: ListBulletIcon },
    { href: '/explore', label: 'Explore', icon: GlobeAltIcon },
  ];

  return (
    <nav className="bg-white shadow-md sticky top-0 z-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          {/* Left section */}
          <div className="flex items-center space-x-4">
            {showBackButton && (
              <button
                onClick={() => router.back()}
                className="p-2 rounded-lg hover:bg-gray-100 transition-colors"
                aria-label="Go back"
              >
                <ArrowLeftIcon className="h-5 w-5 text-gray-600" />
              </button>
            )}
            <Link href="/" className="flex items-center space-x-2">
              <div className="text-2xl">✈️</div>
              <span className="font-bold text-xl text-gray-900 hidden sm:block">Flight Spotter</span>
            </Link>
          </div>

          {/* Center section - Navigation Links */}
          <SignedIn>
            <div className="hidden md:flex items-center space-x-1">
              {navItems.map((item) => {
                const Icon = item.icon;
                const isActive = pathname === item.href || pathname.startsWith(item.href + '/');
                return (
                  <Link
                    key={item.href}
                    href={item.href}
                    className={`flex items-center space-x-2 px-4 py-2 rounded-lg transition-all ${
                      isActive
                        ? 'bg-blue-50 text-blue-600 font-semibold'
                        : 'text-gray-700 hover:bg-gray-100'
                    }`}
                  >
                    <Icon className="h-5 w-5" />
                    <span>{item.label}</span>
                  </Link>
                );
              })}
            </div>
          </SignedIn>

          {/* Right section */}
          <div className="flex items-center space-x-4">
            <SignedOut>
              <SignInButton mode="modal">
                <button className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors font-medium">
                  Sign In
                </button>
              </SignInButton>
            </SignedOut>
            <SignedIn>
              <UserButton afterSignOutUrl="/" />
            </SignedIn>
          </div>
        </div>

        {/* Mobile navigation */}
        <SignedIn>
          <div className="md:hidden flex items-center justify-around pb-3 border-t border-gray-200 pt-2">
            {navItems.map((item) => {
              const Icon = item.icon;
              const isActive = pathname === item.href || pathname.startsWith(item.href + '/');
              return (
                <Link
                  key={item.href}
                  href={item.href}
                  className={`flex flex-col items-center space-y-1 px-3 py-2 rounded-lg transition-all ${
                    isActive
                      ? 'text-blue-600 font-semibold'
                      : 'text-gray-600'
                  }`}
                >
                  <Icon className="h-6 w-6" />
                  <span className="text-xs">{item.label}</span>
                </Link>
              );
            })}
          </div>
        </SignedIn>
      </div>
    </nav>
  );
}
