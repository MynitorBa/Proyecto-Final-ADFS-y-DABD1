Set-Location "C:\Proyecto-Final-ADFS-y-DABD1\Agencia de Viajes\Movent"
$output = npm install --save-dev vitest jsdom 2>&1
$output | Out-File "C:\Proyecto-Final-ADFS-y-DABD1\Agencia de Viajes\npm-install.log" -Encoding UTF8
Write-Host "EXIT:$LASTEXITCODE"
Write-Host "--- LAST 20 LINES ---"
$output | Select-Object -Last 20
