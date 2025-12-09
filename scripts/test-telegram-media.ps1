# scripts/test-telegram-media.ps1
param (
    [string]$type = "photo", # Options: "photo" or "document"
    [string]$recipient = "1898155128"
)

$url = "http://localhost:8080/api/v1/notifications/send"

# 1. Define Payloads based on type
if ($type -eq "photo") {
    $mediaUrl = "https://images.unsplash.com/photo-1519681393784-d120267933ba?auto=format&fit=crop&w=800&q=80"
    $caption = "[PHOTO] Here is a scenery photo sent via Quarkus!"
} else {
    # Default to PDF for document test
    $mediaUrl = "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf"
    $caption = "[DOC] Here is your requested document."
}

Write-Host "[INFO] Sending Telegram $type to $recipient..." -ForegroundColor Cyan

# 2. Construct JSON (Now includes "message" to satisfy validation)
$json = @"
{
  "channel": "TELEGRAM",
  "recipient": "$recipient",
  "message": "$caption",
  "data": {
    "message_type": "$type",
    "media_url": "$mediaUrl",
    "caption": "$caption",
    "typing_indicator": "true"
  }
}
"@

# 3. Send Request
try {
    $response = Invoke-RestMethod -Uri $url -Method Post -Body $json -ContentType "application/json"

    Write-Host "[SUCCESS] Request Accepted!" -ForegroundColor Green
    Write-Host "Notification ID: $($response.notificationId)"
    Write-Host "Check your Telegram App now."
} catch {
    Write-Host "[FAIL] Request Failed." -ForegroundColor Red
    Write-Host $_.Exception.Message
    try {
        $errorBody = $_.Exception.Response.GetResponseStream() | %{ [System.IO.StreamReader]::new($_).ReadToEnd() }
        Write-Host $errorBody -ForegroundColor Yellow
    } catch {}
}