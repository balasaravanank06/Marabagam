# Marabagam Platform Local Development Script (Windows PowerShell)

Param (
    [string]$Action = "docker"
)

Write-Host "============================================================" -ForegroundColor Cyan
Write-Host " Marabagam Platform Local Development Script (PowerShell)" -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan

If (!(Test-Path ".env")) {
    Write-Host "Creating .env from .env.example..." -ForegroundColor Yellow
    Copy-Item ".env.example" ".env"
}

Switch ($Action.ToLower()) {
    "docker" {
        Write-Host "Starting Marabagam environment with Docker Compose..." -ForegroundColor Green
        docker compose down --remove-orphans 2>$null
        docker compose build
        docker compose up -d
        Start-Sleep -Seconds 5
        docker compose ps
        Write-Host ""
        Write-Host "Frontend URL:   http://localhost:5173" -ForegroundColor Green
        Write-Host "Backend Health: http://localhost:8080/api/v1/health" -ForegroundColor Green
        Write-Host "AI Service:     http://localhost:8000/health" -ForegroundColor Green
    }
    "stop" {
        Write-Host "Stopping Marabagam Docker containers..." -ForegroundColor Yellow
        docker compose down
    }
    "status" {
        docker compose ps
    }
    Default {
        Write-Host "Usage: .\scripts\dev.ps1 -Action [docker|stop|status]" -ForegroundColor Red
    }
}
