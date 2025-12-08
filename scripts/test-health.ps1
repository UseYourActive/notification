# scripts/test-health.ps1
$url = "http://localhost:8080/q/health"
Write-Host "[INFO] Checking System Health..." -ForegroundColor Cyan

try {
    $response = Invoke-RestMethod -Uri $url -Method Get
    if ($response.status -eq "UP") {
        Write-Host "[OK] System is UP" -ForegroundColor Green
        $response.checks | ForEach-Object {
            $symbol = if ($_.status -eq "UP") { "[OK]" } else { "[FAIL]" }
            Write-Host "   $symbol $($_.name)"
        }
    } else {
        Write-Host "[FAIL] System is DOWN" -ForegroundColor Red
    }
} catch {
    Write-Host "[ERROR] API is unreachable. Is the container running?" -ForegroundColor Red
}