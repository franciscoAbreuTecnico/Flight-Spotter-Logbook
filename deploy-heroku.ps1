# Heroku Deployment Script for Flight Spotter Logbook
# This script automates the deployment of both backend and frontend to Heroku

param(
    [string]$BackendAppName = "flight-spotter-backend-fa",
    [string]$FrontendAppName = "flight-spotter-frontend-fa"
)

Write-Host "=== Flight Spotter Logbook - Heroku Deployment ===" -ForegroundColor Cyan
Write-Host ""

# Check if Heroku CLI is installed
Write-Host "Checking Heroku CLI..." -ForegroundColor Yellow
try {
    $herokuVersion = heroku --version
    Write-Host "✓ Heroku CLI is installed: $herokuVersion" -ForegroundColor Green
} catch {
    Write-Host "✗ Heroku CLI is not installed!" -ForegroundColor Red
    Write-Host "Please install from: https://devcenter.heroku.com/articles/heroku-cli" -ForegroundColor Yellow
    exit 1
}

Write-Host ""
Write-Host "=== Step 1: Create Heroku Apps ===" -ForegroundColor Cyan

# Create backend app
Write-Host "Creating backend app: $BackendAppName..." -ForegroundColor Yellow
heroku create $BackendAppName 2>$null
if ($LASTEXITCODE -ne 0) {
    Write-Host "Backend app may already exist or name is taken. Continuing..." -ForegroundColor Yellow
}

# Create frontend app
Write-Host "Creating frontend app: $FrontendAppName..." -ForegroundColor Yellow
heroku create $FrontendAppName 2>$null
if ($LASTEXITCODE -ne 0) {
    Write-Host "Frontend app may already exist or name is taken. Continuing..." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "=== Step 2: Add PostgreSQL Database ===" -ForegroundColor Cyan
Write-Host "Adding PostgreSQL to backend..." -ForegroundColor Yellow
heroku addons:create heroku-postgresql:essential-0 -a $BackendAppName 2>$null
if ($LASTEXITCODE -ne 0) {
    Write-Host "Database addon may already exist. Continuing..." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "=== Step 3: Set Environment Variables ===" -ForegroundColor Cyan
Write-Host ""
Write-Host "Please provide the following environment variables:" -ForegroundColor Yellow
Write-Host ""

# Clerk
$jwksUri = Read-Host "Clerk JWKS URI (https://your-clerk.clerk.accounts.dev/.well-known/jwks.json)"
heroku config:set JWKS_URI="$jwksUri" -a $BackendAppName
heroku config:set ROLE_CLAIM_FIELD="publicMetadata.role" -a $BackendAppName

# Cloudinary
$cloudName = Read-Host "Cloudinary Cloud Name"
$cloudApiKey = Read-Host "Cloudinary API Key"
$cloudApiSecret = Read-Host "Cloudinary API Secret"
heroku config:set CLOUDINARY_CLOUD_NAME="$cloudName" -a $BackendAppName
heroku config:set CLOUDINARY_API_KEY="$cloudApiKey" -a $BackendAppName
heroku config:set CLOUDINARY_API_SECRET="$cloudApiSecret" -a $BackendAppName

# OpenSky
$openskyId = Read-Host "OpenSky Client ID (your email)"
$openskySecret = Read-Host "OpenSky Client Secret"
heroku config:set OPENSKY_CLIENT_ID="$openskyId" -a $BackendAppName
heroku config:set OPENSKY_CLIENT_SECRET="$openskySecret" -a $BackendAppName

# Clerk Frontend
$clerkPubKey = Read-Host "Clerk Publishable Key (pk_test_...)"
$clerkSecret = Read-Host "Clerk Secret Key (sk_test_...)"
heroku config:set NEXT_PUBLIC_CLERK_PUBLISHABLE_KEY="$clerkPubKey" -a $FrontendAppName
heroku config:set CLERK_SECRET_KEY="$clerkSecret" -a $FrontendAppName

# Set backend URL for frontend
$backendUrl = "https://$BackendAppName.herokuapp.com"
heroku config:set NEXT_PUBLIC_BACKEND_URL="$backendUrl" -a $FrontendAppName

# Set CORS for backend
$frontendUrl = "https://$FrontendAppName.herokuapp.com"
heroku config:set CORS_ALLOWED_ORIGINS="$frontendUrl,http://localhost:3000" -a $BackendAppName

Write-Host ""
Write-Host "=== Step 4: Add Git Remotes ===" -ForegroundColor Cyan
git remote remove heroku-backend 2>$null
git remote remove heroku-frontend 2>$null
heroku git:remote -a $BackendAppName -r heroku-backend
heroku git:remote -a $FrontendAppName -r heroku-frontend
Write-Host "✓ Git remotes configured" -ForegroundColor Green

Write-Host ""
Write-Host "=== Step 5: Add Buildpack for Frontend ===" -ForegroundColor Cyan
heroku buildpacks:clear -a $FrontendAppName
heroku buildpacks:add heroku/nodejs -a $FrontendAppName
Write-Host "✓ Node.js buildpack added to frontend" -ForegroundColor Green

Write-Host ""
Write-Host "=== Step 6: Deploy Backend ===" -ForegroundColor Cyan
Write-Host "Deploying backend to Heroku..." -ForegroundColor Yellow
git subtree push --prefix backend heroku-backend main
if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ Backend deployed successfully!" -ForegroundColor Green
} else {
    Write-Host "✗ Backend deployment failed!" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "=== Step 7: Deploy Frontend ===" -ForegroundColor Cyan
Write-Host "Deploying frontend to Heroku..." -ForegroundColor Yellow
git subtree push --prefix frontend heroku-frontend main
if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ Frontend deployed successfully!" -ForegroundColor Green
} else {
    Write-Host "✗ Frontend deployment failed!" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "=== Deployment Complete! ===" -ForegroundColor Green
Write-Host ""
Write-Host "Your applications are now live:" -ForegroundColor Cyan
Write-Host "  Backend:  https://$BackendAppName.herokuapp.com" -ForegroundColor White
Write-Host "  Frontend: https://$FrontendAppName.herokuapp.com" -ForegroundColor White
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host "1. Update Clerk dashboard with new URLs" -ForegroundColor White
Write-Host "2. Test the application: heroku open -a $FrontendAppName" -ForegroundColor White
Write-Host "3. Monitor logs: heroku logs --tail -a $BackendAppName" -ForegroundColor White
Write-Host ""
Write-Host "To view backend logs: heroku logs --tail -a $BackendAppName" -ForegroundColor Cyan
Write-Host "To view frontend logs: heroku logs --tail -a $FrontendAppName" -ForegroundColor Cyan
Write-Host ""
