# scripts/deploy-prod.ps1

# 1. Determine Project Root
$projectRoot = Resolve-Path "$PSScriptRoot/.."
Push-Location $projectRoot

Write-Host "[START] Starting PROD deployment (Native Mode)..." -ForegroundColor Cyan

try {
    # 2. Build Native Image
    Write-Host "[BUILD] Compiling Native Binary (this may take a few minutes)..."
    ./mvnw clean package -Pnative "-DskipTests" "-Dquarkus.native.container-build=true"

    if ($LASTEXITCODE -ne 0) {
        Write-Error "Native build failed."
        exit 1
    }

    # 3. Set Env & Deploy
    $env:DEPLOY_MODE="native"
    Write-Host "[DEPLOY] Deploying to Docker Compose..."
    docker-compose up -d --build

    Write-Host "[SUCCESS] Prod environment is running!" -ForegroundColor Cyan
} finally {
    Pop-Location
}