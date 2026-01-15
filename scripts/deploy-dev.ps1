# scripts/deploy-dev.ps1

# 1. Determine Project Root
$projectRoot = Resolve-Path "$PSScriptRoot/.."
Push-Location $projectRoot

Write-Host "[START] Starting DEV deployment (JVM Mode)..." -ForegroundColor Green

try {
    # 2. Build the JAR (Skip tests for speed)
    Write-Host "[BUILD] Building JAR..."
    ./mvnw clean package -DskipTests

    if ($LASTEXITCODE -ne 0) {
        Write-Error "Maven build failed."
        exit 1
    }

    # 3. Set Env & Deploy
    $env:DEPLOY_MODE="jvm"
    Write-Host "[DEPLOY] Deploying to Docker Compose..."
    docker-compose up -d --build

    # 4. Output Access URLs
    Write-Host "[SUCCESS] Dev environment is running!" -ForegroundColor Green
    Write-Host "   - App API:     http://localhost:8080"
    Write-Host "   - Swagger UI:  http://localhost:8080/q/swagger-ui"
    Write-Host "   - Grafana:     http://localhost:3000"
    Write-Host "   - Prometheus:  http://localhost:9090"
    Write-Host "   - Mailpit:     http://localhost:8025 (Email Sandbox)"

    Write-Host "`n[DEBUG] Actual Container Ports:" -ForegroundColor Cyan
    docker-compose ps --format "table {{.Name}}\t{{.Status}}\t{{.Ports}}"

} finally {
    Pop-Location
}