# Flight Spotter Logbook

A full-stack web application for aviation enthusiasts to record, manage, and share aircraft sightings. Built with Spring Boot, Next.js, and PostgreSQL.

## Overview

Flight Spotter Logbook allows users to document their aircraft sightings with detailed information including airport locations, dates, flight numbers, and photos. The application automatically enriches sighting data using the OpenSky Network API and provides a social platform for aviation enthusiasts to share their experiences.

## Features

- **User Authentication**: Secure authentication via Clerk with role-based access (USER, ADMIN)
- **Sighting Management**: Create, edit, and delete aircraft sightings with detailed information
- **Photo Upload**: Support for multiple photos per sighting, stored securely on Cloudinary
- **Data Enrichment**: Automatic enhancement of sighting data using OpenSky Network API
- **Personal & Explore Feeds**: View your own sightings and discover public sightings from other users
- **Admin Dashboard**: Moderation tools and basic analytics for administrators
- **Rate Limiting**: Built-in protection against API abuse with Bucket4j
- **Responsive Design**: Modern UI built with Tailwind CSS

## Technology Stack

### Backend
- **Framework**: Spring Boot 3.1.5
- **Language**: Java 17
- **Database**: PostgreSQL with Flyway migrations
- **Authentication**: JWT validation via Clerk
- **Image Storage**: Cloudinary
- **External API**: OpenSky Network API for aircraft data
- **Rate Limiting**: Bucket4j (100 requests/hour for users, 20 for anonymous)
- **Build Tool**: Maven 3.8.6

### Frontend
- **Framework**: Next.js 15.5.9
- **Language**: TypeScript
- **UI Library**: React 18.3.1
- **Styling**: Tailwind CSS
- **Authentication**: Clerk
- **HTTP Client**: Axios
- **Testing**: Jest + React Testing Library

### Infrastructure
- **Database**: PostgreSQL 15
- **Containerization**: Docker + Docker Compose
- **Deployment**: Render (recommended), Heroku, or Vercel

## Repository Structure

```
flight-spotter-logbook/
├── backend/                    # Spring Boot API
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/          # Application source code
│   │   │   └── resources/     # Configuration files
│   │   └── test/              # Unit and integration tests
│   ├── Dockerfile
│   ├── Procfile               # Heroku configuration
│   ├── system.properties      # Java version for Heroku
│   └── pom.xml                # Maven configuration
│
├── frontend/                   # Next.js application
│   ├── app/                   # Next.js 13+ app directory
│   │   ├── components/        # React components
│   │   ├── admin/            # Admin dashboard
│   │   ├── explore/          # Explore feed
│   │   └── sightings/        # Sighting management
│   ├── public/               # Static assets
│   ├── Dockerfile
│   ├── package.json
│   └── tsconfig.json
│
├── infra/                     # Infrastructure configuration
│   ├── docker-compose.yml    # Local development setup
│   └── .env.example          # Environment variables template
│
├── .github/
│   └── workflows/
│       └── ci-cd.yml         # CI/CD pipeline
│
└── README.md                  # This file
```

## Prerequisites

- **Docker & Docker Compose**: For containerized development
- **Node.js**: v18+ (if running frontend locally)
- **Java**: JDK 17+ (if running backend locally)
- **Maven**: 3.8+ (if running backend locally)

### Required External Services

1. **Clerk** (Authentication)
   - Sign up at https://clerk.com
   - Create an application and get API keys
   - Required keys: `NEXT_PUBLIC_CLERK_PUBLISHABLE_KEY`, `CLERK_SECRET_KEY`, `JWKS_URI`

2. **Cloudinary** (Image Storage)
   - Sign up at https://cloudinary.com
   - Get credentials from Settings > Access Keys
   - Required: `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY`, `CLOUDINARY_API_SECRET`

3. **OpenSky Network** (Optional - Aircraft Data)
   - Register at https://opensky-network.org
   - Provides higher rate limits for authenticated users
   - Required: `OPENSKY_CLIENT_ID`, `OPENSKY_CLIENT_SECRET`

## Quick Start

### 1. Clone the Repository

```bash
git clone git@github.com:franciscoAbreuTecnico/Flight-Spotter-Logbook.git
cd Flight-Spotter-Logbook
```

### 2. Set Up Environment Variables

**Backend Configuration:**
```bash
cd backend
cp .env.example .env
# Edit .env with your actual values
```

**Frontend Configuration:**
```bash
cd frontend
cp .env.example .env
# Edit .env with your Clerk keys
```

**Infrastructure Configuration (for Docker Compose):**
```bash
cd infra
cp .env.example .env
# Edit .env with all required values
```

### 3. Run with Docker Compose

The easiest way to start the entire stack:

```bash
cd infra
docker-compose up --build
```

This will:
- Start PostgreSQL database
- Run Flyway migrations automatically
- Start the backend API on http://localhost:8080
- Start the frontend on http://localhost:3000

### 4. Access the Application

- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080
- **API Documentation**: http://localhost:8080/swagger-ui.html (if configured)

## Development Setup

### Running Backend Locally (without Docker)

```bash
# Start PostgreSQL only
cd infra
docker-compose up db -d

# Run backend
cd ../backend
./mvnw spring-boot:run
```

### Running Frontend Locally (without Docker)

```bash
cd frontend
npm install
npm run dev
```

## Testing

### Backend Tests

```bash
cd backend
./mvnw test
```

Test results:
- Unit tests: 59/59 passing
- Uses H2 in-memory database for tests
- JUnit 5 + Mockito

### Frontend Tests

```bash
cd frontend
npm test
```

Test results:
- Component tests: 9/9 passing
- Jest + React Testing Library

### Run All Tests

```bash
# Backend
cd backend && ./mvnw test && cd ..

# Frontend
cd frontend && npm test && cd ..
```

Total: 68/68 tests passing

## API Endpoints

### Public Endpoints
- `GET /api/sightings` - List all public sightings (paginated)
- `GET /api/sightings/{id}` - Get single sighting details
- `GET /api/airports/search` - Search airports by ICAO/IATA code

### Authenticated Endpoints
- `POST /api/sightings` - Create new sighting
- `PUT /api/sightings/{id}` - Update own sighting
- `DELETE /api/sightings/{id}` - Delete own sighting
- `GET /api/sightings/me` - List user's own sightings
- `POST /api/photos` - Upload photo for sighting

### Admin Endpoints
- `GET /api/admin/sightings` - List all sightings (including private)
- `DELETE /api/admin/sightings/{id}` - Delete any sighting
- `GET /api/admin/stats` - Get system statistics

## Environment Variables

### Backend (.env)

```bash
# Clerk JWT validation
JWKS_URI=https://your-clerk-instance.clerk.accounts.dev/.well-known/jwks.json
ROLE_CLAIM_FIELD=role

# CORS configuration
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://frontend:3000

# Cloudinary credentials
CLOUDINARY_CLOUD_NAME=your_cloud_name
CLOUDINARY_API_KEY=your_api_key
CLOUDINARY_API_SECRET=your_api_secret

# OpenSky Network API
OPENSKY_CLIENT_ID=your_email@example.com
OPENSKY_CLIENT_SECRET=your_opensky_secret
```

### Frontend (.env)

```bash
# Backend API URL
NEXT_PUBLIC_BACKEND_URL=http://localhost:8080

# Clerk authentication
NEXT_PUBLIC_CLERK_PUBLISHABLE_KEY=pk_test_your_key_here
CLERK_SECRET_KEY=sk_test_your_secret_here
```

## Database Schema

The application uses PostgreSQL with Flyway migrations:

- **sightings**: Aircraft sighting records
- **photos**: Photo metadata linked to sightings
- **users**: User information (managed by Clerk)
- **roles**: Role-based access control

Migrations are located in `backend/src/main/resources/db/migration/`

## Security Features

- JWT-based authentication via Clerk
- Role-based authorization (USER, ADMIN)
- Rate limiting (100 req/hour for users, 20 for anonymous)
- CORS protection
- SQL injection prevention via JPA
- XSS protection via sanitized inputs
- Secure photo upload via Cloudinary signed URLs

## Deployment

### Render Deployment (Recommended)

Render provides a simple, free-tier deployment with a Blueprint file for one-click setup.

#### Prerequisites

- GitHub account with this repository (fork or clone)
- Render account (https://render.com)
- External service credentials ready (Clerk, Cloudinary)

#### Option 1: One-Click Deploy with Blueprint

1. **Connect GitHub to Render**:
   - Go to https://dashboard.render.com
   - Click **New** → **Blueprint**
   - Connect your GitHub account and select this repository

2. **Review Services**:
   Render will detect `render.yaml` and create:
   - `flight-spotter-db` - PostgreSQL database
   - `flight-spotter-backend` - Spring Boot API
   - `flight-spotter-frontend` - Next.js application

3. **Configure Environment Variables**:
   After creation, go to each service → **Environment** and add:

   **Backend** (`flight-spotter-backend`):
   ```
   JWKS_URI=https://your-clerk-instance.clerk.accounts.dev/.well-known/jwks.json
   CLOUDINARY_CLOUD_NAME=your_cloud_name
   CLOUDINARY_API_KEY=your_api_key
   CLOUDINARY_API_SECRET=your_api_secret
   CORS_ALLOWED_ORIGINS=https://flight-spotter-frontend.onrender.com
   OPENSKY_CLIENT_ID=your_opensky_id (optional)
   OPENSKY_CLIENT_SECRET=your_opensky_secret (optional)
   ```

   **Frontend** (`flight-spotter-frontend`):
   ```
   NEXT_PUBLIC_CLERK_PUBLISHABLE_KEY=pk_test_your_key
   CLERK_SECRET_KEY=sk_test_your_secret
   NEXT_PUBLIC_BACKEND_URL=https://flight-spotter-backend.onrender.com
   ```

4. **Deploy**:
   - Click **Apply** to create all services
   - Wait for builds to complete (5-10 minutes)

#### Option 2: Manual Setup

1. **Create PostgreSQL Database**:
   - Render Dashboard → **New** → **PostgreSQL**
   - Name: `flight-spotter-db`
   - Plan: Free
   - Copy the **Internal Database URL**

2. **Deploy Backend**:
   - **New** → **Web Service**
   - Connect repository, set root directory to `backend`
   - Runtime: Docker
   - Add environment variables (see above)
   - Add `DATABASE_URL` with the internal database URL

3. **Deploy Frontend**:
   - **New** → **Web Service**
   - Connect repository, set root directory to `frontend`
   - Runtime: Docker
   - Add environment variables (see above)

#### Post-Deployment Setup

1. **Configure Clerk Domain**:
   - Go to [Clerk Dashboard](https://dashboard.clerk.com) → **Domains**
   - Add your frontend URL: `https://flight-...-frontend.onrender.com`

2. **Set Up Admin User**:
   Connect to the database and insert your admin role:
   ```bash
   # Get connection string from Render → Database → Connect → External
   psql "your_external_database_url?sslmode=require"
   
   # Insert admin role (get user_id from Clerk Dashboard → Users)
   INSERT INTO user_roles (user_id, role, granted_by, notes) 
   VALUES ('user_xxxxx', 'ADMIN', 'system', 'Initial admin setup');
   ```

3. **Verify Deployment**:
   ```bash
   # Check backend health
   curl https://flight-spotter-backend.onrender.com/actuator/health
   
   # Should return: {"status":"UP"}
   ```

#### Render URLs

After deployment, your application will be available at:
- **Frontend**: `https://flight-spotter-frontend.onrender.com`
- **Backend API**: `https://flight-spotter-backend.onrender.com`
- **Health Check**: `https://flight-spotter-backend.onrender.com/actuator/health`

> **Note**: Free tier services spin down after 15 minutes of inactivity. First request may take 30-60 seconds to wake up.

---

### Alternative: Heroku Deployment

#### Backend Deployment (Heroku)

1. Create Heroku app:
```bash
heroku create flight-spotter-backend
```

2. Add PostgreSQL addon:
```bash
heroku addons:create heroku-postgresql:essential-0
```

3. Set environment variables:
```bash
heroku config:set JWKS_URI="your_jwks_uri"
heroku config:set CLOUDINARY_CLOUD_NAME="your_cloud_name"
# ... (set all required variables)
```

4. Deploy:
```bash
git subtree push --prefix backend heroku main
```

#### Frontend Deployment (Vercel)

1. Import repository in Vercel dashboard
2. Set root directory to `frontend`
3. Add environment variables in Vercel settings
4. Deploy

## Troubleshooting

### Backend won't start
- Check database connection in logs: `heroku logs --tail`
- Verify all environment variables are set: `heroku config`
- Ensure Flyway migrations completed successfully

### Frontend can't connect to backend
- Verify `NEXT_PUBLIC_BACKEND_URL` is correct
- Check CORS configuration in backend
- Inspect browser console for errors

### Authentication issues
- Verify Clerk JWKS URI is correct
- Check JWT token in browser DevTools
- Ensure role claim is properly configured in Clerk

### Database errors
- Check PostgreSQL connection: `heroku pg:info`
- Review Flyway migration logs
- Verify database credentials

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License.

## Support

For issues, questions, or contributions, please open an issue on GitHub.

## Acknowledgments

- OpenSky Network for aircraft data API
- Clerk for authentication services
- Cloudinary for image storage
- Spring Boot and Next.js communities

---

**Version**: 1.0.0  
**Last Updated**: December 2025
