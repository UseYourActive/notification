# scripts/test-concurrency.ps1

# --- 1. Dynamic Port Detection ---
$baseUrl = "http://localhost:8080"
$k8sPort = $null

Write-Host "[INFO] Detecting Environment..." -ForegroundColor Yellow

# Try connecting to Docker port 8080
try {
    $tcp = New-Object System.Net.Sockets.TcpClient
    $connect = $tcp.BeginConnect("localhost", 8080, $null, $null)
    $success = $connect.AsyncWaitHandle.WaitOne(200, $false)
    if (-not $success) {
        # 8080 is closed, check Kubernetes
        $k8sPort = kubectl get service notification-service -o jsonpath='{.spec.ports[0].nodePort}' 2>$null
    }
    $tcp.Close()
} catch {}

if ($k8sPort) {
    Write-Host "   [K8S] Kubernetes Detected. Target: NodePort $k8sPort" -ForegroundColor Cyan
    $baseUrl = "http://localhost:$k8sPort"
} else {
    Write-Host "   [DEV] Docker/Localhost Detected. Target: Port 8080" -ForegroundColor Cyan
}

# --- 2. Configuration ---
$url = "$baseUrl/api/v1/notifications/send"
$headers = @{ "Content-Type" = "application/json" }

$json = '{
  "channel": "TELEGRAM",
  "recipient": "1898155128",
  "templateName": "telegram/daily_reminder",
  "data": {
    "firstName": "StressTest",
    "taskCount": "5",
    "messageCount": "3",
    "nextEvent": "Concurrent Test"
  }
}'

# --- 3. Execution ---
Write-Host "LAUNCHING 20 parallel requests to: $url" -ForegroundColor Magenta

$jobs = @()
1..20 | ForEach-Object {
    $i = $_
    $jobs += Start-Job -ScriptBlock {
        param($id, $uri, $head, $body)
        try {
            $response = Invoke-RestMethod -Uri $uri -Method Post -Headers $head -Body $body
            return "Request $id : [SUCCESS]"
        } catch {
            return "Request $id : [FAIL] ($($_.Exception.Message))"
        }
    } -ArgumentList $i, $url, $headers, $json
}

# --- 4. Wait & Report ---
Write-Host "Waiting for responses..." -ForegroundColor Yellow
$results = $jobs | Receive-Job -Wait -AutoRemoveJob

# Print results
$results | ForEach-Object {
    if ($_ -match "SUCCESS") {
        Write-Host $_ -ForegroundColor Green
    } else {
        Write-Host $_ -ForegroundColor Red
    }
}

Write-Host "TEST COMPLETE." -ForegroundColor Cyan