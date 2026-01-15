# 🚀 Render Deployment Guide - Flight Spotter Logbook

This guide walks you through deploying the Flight Spotter Logbook application to Render.

## Prerequisites

Before starting, ensure you have:

1. **GitHub Repository**: Your code pushed to GitHub (✅ https://github.com/franciscoAbreuTecnico/Flight-Spotter-Logbook)
2. **Render Account**: Sign up at [render.com](https://render.com)
3. **Clerk Account**: For authentication - [clerk.com](https://clerk.com)
4. **Cloudinary Account**: For image storage - [cloudinary.com](https://cloudinary.com)
5. **OpenSky Network Account** (Optional): For aircraft data enrichment - [opensky-network.org](https://opensky-network.org)

---

## 🔐 Step 1: Gather Your Credentials

### Clerk Authentication
1. Go to [Clerk Dashboard](https://dashboard.clerk.com)
2. Create a new application or select existing one
3. Navigate to **API Keys**
4. Copy:
   - `Publishable Key` (starts with `pk_test_` or `pk_live_`)
   - `Secret Key` (starts with `sk_test_` or `sk_live_`)
5. Navigate to **API Keys → Advanced → JWKS URL**
6. Copy the JWKS URL (format: `https://your-instance.clerk.accounts.dev/.well-known/jwks.json`)

### Cloudinary
1. Go to [Cloudinary Console](https://console.cloudinary.com)
2. Navigate to **Settings → Access Keys**
3. Copy:
   - `Cloud Name`
   - `API Key`
   - `API Secret`

### OpenSky Network (Optional)
1. Register at [OpenSky Network](https://opensky-network.org/index.php?option=com_users&view=registration)
2. Your credentials are your email and password

---

## 📦 Step 2: Deploy Using Render Blueprint (Recommended)

### Option A: One-Click Deploy

1. Push the latest changes to GitHub:
   ```bash
   git add .
   git commit -m "Add Render deployment configuration"
   git push origin main
   ```

2. Go to [Render Dashboard](https://dashboard.render.com)

3. Click **New** → **Blueprint**

4. Connect your GitHub repository: `franciscoAbreuTecnico/Flight-Spotter-Logbook`

5. Select the `render.yaml` file

6. Render will create:
   - PostgreSQL Database (`flight-spotter-db`)
   - Backend Service (`flight-spotter-backend`)
   - Frontend Service (`flight-spotter-frontend`)

7. **Wait for the database to be created first** (this takes 2-3 minutes)

---

## 🔧 Step 3: Configure Environment Variables

After the services are created, you need to set the environment variables.

### Backend Environment Variables

Go to **flight-spotter-backend** → **Environment**:

| Variable | Value | Description |
|----------|-------|-------------|
| `JWKS_URI` | `https://your-instance.clerk.accounts.dev/.well-known/jwks.json` | From Clerk Dashboard |
| `ROLE_CLAIM_FIELD` | `role` | JWT claim for user role |
| `CORS_ALLOWED_ORIGINS` | `https://flight-spotter-frontend.onrender.com` | Your frontend URL (get after frontend deploys) |
| `CLOUDINARY_CLOUD_NAME` | `your_cloud_name` | From Cloudinary |
| `CLOUDINARY_API_KEY` | `your_api_key` | From Cloudinary |
| `CLOUDINARY_API_SECRET` | `your_api_secret` | From Cloudinary |
| `OPENSKY_CLIENT_ID` | `your_email@example.com` | Optional - OpenSky |
| `OPENSKY_CLIENT_SECRET` | `your_password` | Optional - OpenSky |

### Frontend Environment Variables

Go to **flight-spotter-frontend** → **Environment**:

| Variable | Value | Description |
|----------|-------|-------------|
| `NEXT_PUBLIC_BACKEND_URL` | `https://flight-spotter-backend.onrender.com` | Your backend URL |
| `NEXT_PUBLIC_CLERK_PUBLISHABLE_KEY` | `pk_test_...` or `pk_live_...` | From Clerk |
| `CLERK_SECRET_KEY` | `sk_test_...` or `sk_live_...` | From Clerk |

---

## 🔄 Step 4: Deploy Sequence

1. **Database deploys first** (automatic)
2. **Backend deploys** after database is ready
3. **Frontend deploys** after backend is ready

### Important: Update URLs After First Deploy

Once both services have URLs:

1. Go to **flight-spotter-backend** → **Environment**
2. Update `CORS_ALLOWED_ORIGINS` to: `https://flight-spotter-frontend.onrender.com`
3. Click **Save Changes** (this triggers a redeploy)

4. Go to **flight-spotter-frontend** → **Environment**
5. Update `NEXT_PUBLIC_BACKEND_URL` to: `https://flight-spotter-backend.onrender.com`
6. Click **Save Changes** (this triggers a redeploy)

---

## 🔒 Step 5: Configure Clerk for Production

1. Go to [Clerk Dashboard](https://dashboard.clerk.com)
2. Navigate to **Domains**
3. Add your Render frontend URL: `https://flight-spotter-frontend.onrender.com`
4. Configure **JWT Templates** if needed:
   - Ensure the `role` claim is included in tokens
   - Or adjust `ROLE_CLAIM_FIELD` env var accordingly

---

## ✅ Step 6: Verify Deployment

### Check Backend Health
```bash
curl https://flight-spotter-backend.onrender.com/actuator/health
```
Expected response:
```json
{"status":"UP"}
```

### Check API Documentation
Visit: `https://flight-spotter-backend.onrender.com/swagger-ui.html`

### Check Frontend
Visit: `https://flight-spotter-frontend.onrender.com`

---

## 🐛 Troubleshooting

### Backend won't start
1. Check **Logs** in Render dashboard
2. Verify all required environment variables are set
3. Ensure database connection is working

### Database connection errors
1. Render auto-injects `JDBC_DATABASE_URL` from the database
2. The connection string is in JDBC format, which Spring handles

### CORS errors
1. Ensure `CORS_ALLOWED_ORIGINS` includes your frontend URL
2. Format: `https://flight-spotter-frontend.onrender.com` (no trailing slash)

### Frontend can't reach backend
1. Check `NEXT_PUBLIC_BACKEND_URL` is set correctly
2. Ensure backend is running and healthy
3. Check browser console for specific errors

### Authentication not working
1. Verify Clerk keys are correct (pk_* and sk_*)
2. Ensure `JWKS_URI` points to correct Clerk instance
3. Check Clerk dashboard for allowed domains

---

## 📋 Manual Deployment (Alternative)

If you prefer not to use Blueprint:

### 1. Create PostgreSQL Database
- Render Dashboard → **New** → **PostgreSQL**
- Name: `flight-spotter-db`
- Database: `flight_spotter_logbook`
- User: `logbook_user`
- Plan: Free

### 2. Create Backend Service
- Render Dashboard → **New** → **Web Service**
- Connect GitHub repo
- **Root Directory**: `backend`
- **Runtime**: Docker
- Set environment variables (including database connection from step 1)

### 3. Create Frontend Service
- Render Dashboard → **New** → **Web Service**
- Connect GitHub repo
- **Root Directory**: `frontend`
- **Runtime**: Docker
- Set environment variables

---

## 🎉 Success!

Your Flight Spotter Logbook should now be live on Render!

- **Frontend**: `https://flight-spotter-frontend.onrender.com`
- **Backend API**: `https://flight-spotter-backend.onrender.com`
- **API Docs**: `https://flight-spotter-backend.onrender.com/swagger-ui.html`

### Free Tier Notes
- Free services spin down after 15 minutes of inactivity
- First request after inactivity may take 30-60 seconds
- Consider upgrading to Starter plan for always-on services

---

## 📝 Environment Variables Quick Reference

### Backend (12 total)
```
JDBC_DATABASE_URL=<auto-injected by Render>
SPRING_DATASOURCE_URL=<auto-injected by Render>
SPRING_DATASOURCE_USERNAME=<auto-injected by Render>
SPRING_DATASOURCE_PASSWORD=<auto-injected by Render>
PORT=8080
JWKS_URI=<your-clerk-jwks-url>
ROLE_CLAIM_FIELD=role
CORS_ALLOWED_ORIGINS=<your-frontend-url>
CLOUDINARY_CLOUD_NAME=<your-cloud-name>
CLOUDINARY_API_KEY=<your-api-key>
CLOUDINARY_API_SECRET=<your-api-secret>
OPENSKY_CLIENT_ID=<optional>
OPENSKY_CLIENT_SECRET=<optional>
```

### Frontend (3 total)
```
NEXT_PUBLIC_BACKEND_URL=<your-backend-url>
NEXT_PUBLIC_CLERK_PUBLISHABLE_KEY=<pk_test_... or pk_live_...>
CLERK_SECRET_KEY=<sk_test_... or sk_live_...>
```
