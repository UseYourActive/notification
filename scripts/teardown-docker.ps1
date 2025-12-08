# scripts/teardown-docker.ps1

# 1. Determine Project Root
$projectRoot = Resolve-Path "$PSScriptRoot/.."
Push-Location $projectRoot

Write-Host "[STOP] Stopping Docker Compose Environment..." -ForegroundColor Yellow

try {
    # We set a default mode just so docker-compose doesn't complain about missing variables
    # when reading the YAML file to shut things down.
    $env:DEPLOY_MODE="jvm"

    # 2. Stop and Remove Containers & Networks
    # (Removes the app, postgres, redis, pgadmin, and ngrok)
    docker-compose down

    # OPTIONAL: To delete the Database Data as well, uncomment the line below:
    # docker-compose down --volumes

    Write-Host "[SUCCESS] Environment is down." -ForegroundColor Green
} catch {
    Write-Error "Failed to stop Docker containers."
} finally {
    Pop-Location
}