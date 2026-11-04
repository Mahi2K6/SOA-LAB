# Sends 9 requests through the API gateway and counts which instance answered each one.
# Run in PowerShell:  .\test-load-balancing.ps1

$counts = @{}
Write-Host "Instances registered in Eureka (as seen by the gateway):"
Invoke-RestMethod http://localhost:8080/gateway/services/tourist-service | ConvertTo-Json -Depth 5
Write-Host ""

for ($i = 1; $i -le 9; $i++) {
    $r = Invoke-RestMethod http://localhost:8080/api/tourist/instance
    Write-Host ("Request {0} -> {1}" -f $i, $r.servedBy)
    $counts[$r.servedBy] = 1 + [int]$counts[$r.servedBy]
}

Write-Host ""
Write-Host "Distribution:"
$counts.GetEnumerator() | Sort-Object Name | ForEach-Object { Write-Host ("  {0} : {1} requests" -f $_.Name, $_.Value) }
