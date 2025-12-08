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

    Write-Host "[SUCCESS] Dev environment is running!" -ForegroundColor Green
} finally {
    Pop-Location
}