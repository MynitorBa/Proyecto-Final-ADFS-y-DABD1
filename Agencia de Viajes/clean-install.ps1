Set-Location "C:\Proyecto-Final-ADFS-y-DABD1\Agencia de Viajes\Movent"

Write-Host "Eliminando node_modules corrompido..."
if (Test-Path "node_modules") {
    Remove-Item -Recurse -Force "node_modules"
    Write-Host "node_modules eliminado."
}

Write-Host "Instalando dependencias base..."
npm install
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR en npm install base. EXIT:$LASTEXITCODE"
    exit $LASTEXITCODE
}

Write-Host "Instalando vitest@3 + jsdom..."
npm install --save-dev vitest@3 jsdom@26
$code = $LASTEXITCODE
Write-Host "EXIT:$code"

if ($code -eq 0) {
    Write-Host "INSTALL_OK"
    if (Test-Path "node_modules\.bin\vitest") {
        Write-Host "VITEST_BINARY_FOUND"
    } else {
        Write-Host "VITEST_BINARY_NOT_FOUND"
    }
} else {
    Write-Host "INSTALL_FAIL"
}
