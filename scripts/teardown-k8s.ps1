# scripts/teardown-k8s.ps1

# 1. Determine Project Root (One level up from this script)
$projectRoot = Resolve-Path "$PSScriptRoot/.."

Write-Host "[DELETE] NUKING Kubernetes Environment..." -ForegroundColor Red

# 2. Delete App
kubectl delete deployment notification-service --ignore-not-found
kubectl delete service notification-service --ignore-not-found

# 3. Delete Tools
kubectl delete deployment ngrok --ignore-not-found
kubectl delete service ngrok-dashboard --ignore-not-found

# 4. Delete Infrastructure (Postgres & Redis)
# Uses the correct path relative to the script location
$infraFile = Join-Path $projectRoot "k8s-infra.yaml"
Write-Host "[DELETE] Removing infrastructure defined in: $infraFile"
kubectl delete -f $infraFile --ignore-not-found

# 5. Delete Secrets
kubectl delete secret notification-env-secret --ignore-not-found

Write-Host "[DONE] Cluster is clean." -ForegroundColor Yellow