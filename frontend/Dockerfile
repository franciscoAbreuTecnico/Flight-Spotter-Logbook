# Stage 1: build the Next.js frontend
FROM node:20-alpine AS build
WORKDIR /app

# Install dependencies based on the package.json and package-lock.json
COPY package*.json ./
RUN npm install

# Copy the rest of the source code (excluding .env files per .dockerignore)
COPY . .

# Set environment variables for build time
ARG NEXT_PUBLIC_BACKEND_URL=http://localhost:8080
ARG NEXT_PUBLIC_CLERK_PUBLISHABLE_KEY
ENV NEXT_PUBLIC_BACKEND_URL=$NEXT_PUBLIC_BACKEND_URL
ENV NEXT_PUBLIC_CLERK_PUBLISHABLE_KEY=$NEXT_PUBLIC_CLERK_PUBLISHABLE_KEY

# Build the app
RUN npm run build

# Stage 2: production image for running the app
FROM node:20-alpine AS runner
WORKDIR /app
ENV NODE_ENV=production

# Copy the standalone output
COPY --from=build /app/.next/standalone ./
COPY --from=build /app/.next/static ./.next/static
COPY --from=build /app/public ./public

# Expose the port that Next.js uses
EXPOSE 3000

# Set host to allow external connections
ENV HOSTNAME="0.0.0.0"

# Start the Next.js application
CMD ["node", "server.js"]