# scripts/scale-k8s.ps1
param (
    [Parameter(Position=0)]
    [int]$amount = 5
)

$deployment = "notification-service"

Write-Host "[SCALE] Scaling $deployment to $amount replicas..." -ForegroundColor Cyan

# 1. Execute Command
try {
    kubectl scale deployment $deployment --replicas=$amount

    if ($LASTEXITCODE -ne 0) {
        Write-Error "Failed to scale. Is Kubernetes running?"
        exit 1
    }
    Write-Host "[OK] Command sent." -ForegroundColor Green
} catch {
    Write-Error "Error: $_"
    exit 1
}

# 2. Watch Status
Write-Host "Watching pods... (Press Ctrl+C to exit)" -ForegroundColor Yellow
kubectl get pods -l app=notification-service -w