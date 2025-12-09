# scripts/teardown-k8s.ps1

# 1. Determine Project Root (One level up from this script)
$projectRoot = Resolve-Path "$PSScriptRoot/.."

Write-Host "[DELETE] NUKING Kubernetes Environment..." -ForegroundColor Red

# 2. Delete App
Write-Host " -> Deleting Application..."
kubectl delete deployment notification-service --ignore-not-found
kubectl delete service notification-service --ignore-not-found

# 3. Delete Tools (Ngrok)
Write-Host " -> Deleting Tools..."
kubectl delete deployment ngrok --ignore-not-found
kubectl delete service ngrok-dashboard --ignore-not-found

# 4. Delete Monitoring (Grafana & Prometheus)
$monitorFile = Join-Path $projectRoot "src/main/kubernetes/k8s-monitoring.yaml"
if (Test-Path $monitorFile) {
    Write-Host " -> Deleting Monitoring Stack..."
    kubectl delete -f $monitorFile --ignore-not-found
}

# 5. Delete ConfigMaps (Grafana & Prometheus Configs)
Write-Host " -> Cleaning up ConfigMaps..."
kubectl delete configmap prometheus-config --ignore-not-found
kubectl delete configmap grafana-datasources --ignore-not-found
kubectl delete configmap grafana-dashboards-prov --ignore-not-found
kubectl delete configmap grafana-dashboards-json --ignore-not-found

# 6. Delete Infrastructure (Postgres & Redis)
$infraFile = Join-Path $projectRoot "k8s-infra.yaml"
Write-Host " -> Removing Infrastructure defined in: $infraFile"
kubectl delete -f $infraFile --ignore-not-found

# 7. Delete Secrets
Write-Host " -> Deleting Secrets..."
kubectl delete secret notification-env-secret --ignore-not-found

Write-Host "[DONE] Cluster is completely clean." -ForegroundColor Yellow