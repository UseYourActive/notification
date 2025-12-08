# scripts/deploy-k8s.ps1

# 1. Determine Project Root
$projectRoot = Resolve-Path "$PSScriptRoot/.."
$envFile = Join-Path $projectRoot ".env"
$infraFile = Join-Path $projectRoot "k8s-infra.yaml"
$ngrokFile = Join-Path $projectRoot "src/main/kubernetes/k8s-ngrok.yaml"

Write-Host "[START] Starting FULL Kubernetes Deployment..." -ForegroundColor Magenta

# 2. Secrets
Write-Host "[1/5] Refreshing Secrets..."
kubectl delete secret notification-env-secret --ignore-not-found
# Point explicitly to the .env file in the project root
kubectl create secret generic notification-env-secret --from-env-file=$envFile

# 3. Infrastructure
Write-Host "[2/5] Spinning up Database and Cache..."
kubectl apply -f $infraFile

# 4. Application
Write-Host "[3/5] Cleaning old app deployment..."
kubectl delete deployment notification-service --ignore-not-found

Write-Host "[4/5] Building & Deploying Application..."
# We must run Maven from the project root, not the scripts folder
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

# 5. Tools
Write-Host "[5/5] Launching Ngrok Tunnel..."
if (Test-Path $ngrokFile) {
    kubectl apply -f $ngrokFile
} else {
    Write-Host "Warning: Ngrok YAML not found at $ngrokFile" -ForegroundColor Yellow
}

Write-Host "[SUCCESS] Deployment Complete!" -ForegroundColor Green
Write-Host "   - App UI:      http://localhost:<NodePort>/q/swagger-ui"
Write-Host "   - Ngrok UI:    http://localhost:30040"
Write-Host "   - Monitor:     Run 'k9s' to see everything"