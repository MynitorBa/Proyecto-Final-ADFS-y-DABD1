Set-Location "C:\Proyecto-Final-ADFS-y-DABD1\Agencia de Viajes\Movent"
Write-Host "Instalando vitest@3..."
npm install --save-dev vitest@3 jsdom@26
$code = $LASTEXITCODE
"EXIT:$code" | Out-File "C:\Proyecto-Final-ADFS-y-DABD1\Agencia de Viajes\install-done.txt" -Encoding UTF8
Write-Host "DONE EXIT:$code"
