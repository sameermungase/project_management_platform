#!/bin/bash

# Smart Project Platform - Deployment Script
# This script helps you deploy the application quickly

set -e

echo "======================================"
echo "Smart Project Platform Deployment"
echo "======================================"
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored messages
print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_info() {
    echo -e "${YELLOW}ℹ $1${NC}"
}

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    print_error "Docker is not installed. Please install Docker first."
    exit 1
fi
print_success "Docker is installed"

# Check if Docker Compose is installed
if ! command -v docker-compose &> /dev/null; then
    print_error "Docker Compose is not installed. Please install Docker Compose first."
    exit 1
fi
print_success "Docker Compose is installed"

echo ""
print_info "Select deployment option:"
echo "1) Development (with logs)"
echo "2) Production (detached mode)"
echo "3) Stop all services"
echo "4) Remove all containers and volumes"
echo "5) View logs"
echo "6) Rebuild and restart"
read -p "Enter option (1-6): " option

case $option in
    1)
        print_info "Starting development environment..."
        docker-compose down
        docker-compose up --build
        ;;
    2)
        print_info "Starting production environment..."
        docker-compose down
        docker-compose up -d --build
        echo ""
        print_success "Services started successfully!"
        echo ""
        print_info "Access the application at:"
        echo "  Frontend: http://localhost"
        echo "  Backend API: http://localhost:8080"
        echo "  API Docs: http://localhost:8080/swagger-ui.html"
        echo ""
        print_info "To view logs, run: docker-compose logs -f"
        ;;
    3)
        print_info "Stopping all services..."
        docker-compose down
        print_success "All services stopped"
        ;;
    4)
        print_info "Removing all containers and volumes..."
        read -p "Are you sure? This will delete all data! (yes/no): " confirm
        if [ "$confirm" == "yes" ]; then
            docker-compose down -v
            print_success "All containers and volumes removed"
        else
            print_info "Operation cancelled"
        fi
        ;;
    5)
        print_info "Viewing logs (press Ctrl+C to exit)..."
        docker-compose logs -f
        ;;
    6)
        print_info "Rebuilding and restarting services..."
        docker-compose down
        docker-compose up -d --build --force-recreate
        print_success "Services rebuilt and restarted"
        echo ""
        print_info "To view logs, run: docker-compose logs -f"
        ;;
    *)
        print_error "Invalid option"
        exit 1
        ;;
esac

echo ""
print_success "Operation completed!"
