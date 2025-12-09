# scripts/deploy-k8s.ps1

# 1. Determine Project Root
$projectRoot = Resolve-Path "$PSScriptRoot/.."
$envFile = Join-Path $projectRoot ".env"
$infraFile = Join-Path $projectRoot "k8s-infra.yaml"
$monitorFile = Join-Path $projectRoot "src/main/kubernetes/k8s-monitoring.yaml"
$ngrokFile = Join-Path $projectRoot "src/main/kubernetes/k8s-ngrok.yaml"

Write-Host "[START] Starting FULL Kubernetes Deployment..." -ForegroundColor Magenta

# 2. Secrets
Write-Host "[1/6] Refreshing Secrets..."
kubectl delete secret notification-env-secret --ignore-not-found
kubectl create secret generic notification-env-secret --from-env-file=$envFile

# 3. Monitoring ConfigMaps (The Magic Step) 🪄
Write-Host "[2/6] Generating Monitoring Configs..."
# Clean old maps
kubectl delete configmap grafana-datasources --ignore-not-found
kubectl delete configmap grafana-dashboards-prov --ignore-not-found
kubectl delete configmap grafana-dashboards-json --ignore-not-found

# Create new maps from your local files
kubectl create configmap grafana-datasources --from-file="$projectRoot/config/grafana/provisioning/datasources/"
kubectl create configmap grafana-dashboards-prov --from-file="$projectRoot/config/grafana/provisioning/dashboards/"
kubectl create configmap grafana-dashboards-json --from-file="$projectRoot/config/grafana/dashboards-json/"

# 4. Infrastructure
Write-Host "[3/6] Spinning up Database, Cache, and Monitoring..."
kubectl apply -f $infraFile
kubectl apply -f $monitorFile

# 5. Application
Write-Host "[4/6] Cleaning old app deployment..."
kubectl delete deployment notification-service --ignore-not-found

Write-Host "[5/6] Building & Deploying Application..."
Push-Location $projectRoot
try {
    ./mvnw clean package `
        "-Dquarkus.kubernetes.deploy=true" `
        "-Dquarkus.container-image.push=false" `
        "-Dquarkus.kubernetes.namespace=default" `
        "-Dquarkus.kubernetes.prometheus.generate-service-monitor=false" `
        "-DskipTests"
} finally {
    Pop-Location
}

# 6. Tools
Write-Host "[6/6] Launching Ngrok Tunnel..."
if (Test-Path $ngrokFile) {
    kubectl apply -f $ngrokFile
}

$nodePort = kubectl get service notification-service -o jsonpath='{.spec.ports[0].nodePort}'

Write-Host "[SUCCESS] Deployment Complete!" -ForegroundColor Green
Write-Host "   - App UI:      http://localhost:$nodePort/q/swagger-ui"
Write-Host "   - Grafana:     http://localhost:30000"
Write-Host "   - Ngrok UI:    http://localhost:30040"
Write-Host "   - Monitor:     Run 'k9s' to see everything"