# Configuration
$url = "http://localhost:8080/api/v1/notifications/send"
$headers = @{ "Content-Type" = "application/json" }

# The Payload (Replace with your actual Chat ID)
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

Write-Host "🚀 Launching 10 parallel requests to prove concurrency..."

# Launch 20 background jobs simultaneously
1..20 | ForEach-Object {
    $i = $_
    Start-Job -ScriptBlock {
        param($id, $uri, $head, $body)
        try {
            # Send the request
            $response = Invoke-RestMethod -Uri $uri -Method Post -Headers $head -Body $body
            Write-Output "Request $id : Success"
        } catch {
            Write-Output "Request $id : Failed ($($_.Exception.Message))"
        }
    } -ArgumentList $i, $url, $headers, $json | Out-Null
}

Write-Host "✅ All 10 jobs started! Check your IntelliJ Console NOW."