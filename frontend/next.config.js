/**
 * @type {import('next').NextConfig}
 */
const nextConfig = {
  reactStrictMode: true,
  images: {
    // Allow loading images from Cloudinary
    remotePatterns: [
      {
        protocol: 'https',
        hostname: '**.cloudinary.com',
      },
    ],
  },
  // Output standalone for Docker deployment
  output: 'standalone',
};

module.exports = nextConfig;