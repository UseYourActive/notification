# scripts/clear-redis.ps1
# Requires the 'redis-notification-service' container to be running
Write-Host "[INFO] Flushing Redis Cache..." -ForegroundColor Yellow

try {
    # Execute redis-cli inside the docker container
    docker exec redis-notification-service redis-cli FLUSHALL
    if ($LASTEXITCODE -eq 0) {
        Write-Host "[OK] Redis flushed successfully." -ForegroundColor Green
    } else {
        Write-Error "[FAIL] Failed to flush Redis."
    }
} catch {
    Write-Error "[ERROR] Could not connect to Redis container. Is it running?"
}