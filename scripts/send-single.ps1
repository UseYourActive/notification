# scripts/send-single.ps1
param (
    [string]$channel = "TELEGRAM",
    [string]$recipient = "1898155128"
)

$url = "http://localhost:8080/api/v1/notifications/send"
$json = @"
{
  "channel": "$channel",
  "recipient": "$recipient",
  "templateName": "telegram/daily_reminder",
  "data": {
    "firstName": "DevUser",
    "taskCount": "1",
    "messageCount": "0",
    "nextEvent": "Manual Test"
  }
}
"@

Write-Host "[INFO] Sending 1 $channel notification to $recipient..."
try {
    $response = Invoke-RestMethod -Uri $url -Method Post -Body $json -ContentType "application/json"
    Write-Host "[SUCCESS]" -ForegroundColor Green
    $response | Format-List
} catch {
    Write-Host "[ERROR] Failed:" -ForegroundColor Red
    Write-Host $_.Exception.Message
    # Try to read the error body if available
    try { $_.Exception.Response.GetResponseStream() | %{ [System.IO.StreamReader]::new($_).ReadToEnd() } } catch {}
}