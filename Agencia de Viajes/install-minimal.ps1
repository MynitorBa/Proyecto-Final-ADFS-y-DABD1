Set-Location "C:\Proyecto-Final-ADFS-y-DABD1\Agencia de Viajes\Movent"
Write-Host "Instalando vitest@3 + jsdom..."
npm install --save-dev vitest@3 jsdom@26
$code = $LASTEXITCODE
Write-Host "EXIT:$code"
if ($code -eq 0) {
    Write-Host "VITEST_OK"
} else {
    Write-Host "VITEST_FAIL"
}
