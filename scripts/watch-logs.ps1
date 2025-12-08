# scripts/watch-logs.ps1
Write-Host "[INFO] Watching Application Logs (Ctrl+C to stop)..." -ForegroundColor Cyan

# filters for your specific app logs or errors
docker logs -f notification-app | Select-String "bg.sit.sit.si", "ERROR", "WARN"