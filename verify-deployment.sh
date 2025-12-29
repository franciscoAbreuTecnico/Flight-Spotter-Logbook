#!/bin/bash
# Pre-Deployment Verification Script
# Run this before deploying to Heroku

set -e  # Exit on any error

echo "======================================"
echo "Flight Spotter Logbook - Pre-Deployment Check"
echo "======================================"
echo ""

# Check if we're in the right directory
if [ ! -f "backend/pom.xml" ] || [ ! -f "frontend/package.json" ]; then
    echo "Error: Please run this script from the project root directory"
    exit 1
fi

echo "Directory structure looks good"
echo ""

# Check Java version
echo "Checking Java version..."
java -version 2>&1 | grep "version" || {
    echo "Java not found. Install Java 17+"
    exit 1
}
echo "Java installed"
echo ""

# Check Maven
echo "Checking Maven..."
mvn -version > /dev/null 2>&1 || {
    echo "Maven not found. Install Maven"
    exit 1
}
echo "Maven installed"
echo ""

# Check Node.js
echo "Checking Node.js..."
node -v > /dev/null 2>&1 || {
    echo "Node.js not found. Install Node.js"
    exit 1
}
echo "Node.js installed"
echo ""

# Check npm
echo "Checking npm..."
npm -v > /dev/null 2>&1 || {
    echo "npm not found. Install npm"
    exit 1
}
echo "npm installed"
echo ""

# Check if git is initialized
echo "Checking git..."
if [ ! -d ".git" ]; then
    echo " Warning: Git not initialized. Run 'git init'"
else
    echo "Git initialized"
fi
echo ""

# Check if Heroku CLI is installed
echo "Checking Heroku CLI..."
heroku --version > /dev/null 2>&1 || {
    echo " Warning: Heroku CLI not installed"
    echo "   Install from: https://devcenter.heroku.com/articles/heroku-cli"
}
if command -v heroku > /dev/null 2>&1; then
    echo "Heroku CLI installed"
fi
echo ""

# Run backend tests
echo "======================================"
echo "Running Backend Tests..."
echo "======================================"
cd backend
mvn test -q || {
    echo "Backend tests failed! Fix tests before deploying."
    exit 1
}
echo "All backend tests passed!"
cd ..
echo ""

# Run frontend tests
echo "======================================"
echo "Running Frontend Tests..."
echo "======================================"
cd frontend
npm test -- --passWithNoTests > /dev/null 2>&1 || {
    echo "Frontend tests failed! Fix tests before deploying."
    exit 1
}
echo "All frontend tests passed!"
cd ..
echo ""

# Check for required files
echo "======================================"
echo "Checking Required Files..."
echo "======================================"

required_files=(
    "backend/Procfile"
    "backend/system.properties"
    "backend/pom.xml"
    "backend/src/main/resources/application.yml"
    "frontend/package.json"
    "frontend/next.config.js"
    "README.md"
)

for file in "${required_files[@]}"; do
    if [ ! -f "$file" ]; then
        echo "Missing required file: $file"
        exit 1
    fi
done
echo "All required files present"
echo ""

# Check environment variable examples
echo "======================================"
echo "Checking Environment Setup..."
echo "======================================"

if [ ! -f "backend/.env.example" ]; then
    echo " Warning: backend/.env.example not found"
else
    echo "Backend .env.example exists"
fi

if [ ! -f "frontend/.env.example" ]; then
    echo " Warning: frontend/.env.example not found"
else
    echo "Frontend .env.example exists"
fi
echo ""

# Check if .env files are in .gitignore
if grep -q "\.env" .gitignore 2>/dev/null; then
    echo ".env files are in .gitignore"
else
    echo " Warning: .env files should be in .gitignore"
fi
echo ""

# Summary
echo "======================================"
echo "Pre-Deployment Check Complete!"
echo "======================================"
echo ""
echo "Next Steps:"
echo "1. Ensure you have environment variables ready (check .env.example files)"
echo "2. Create a Heroku account if you haven't already"
echo "3. Follow HEROKU-QUICK-START.md for deployment"
echo ""
echo " Ready to deploy!"
