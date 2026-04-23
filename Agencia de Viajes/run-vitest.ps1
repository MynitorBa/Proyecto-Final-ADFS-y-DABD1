Set-Location "C:\Proyecto-Final-ADFS-y-DABD1\Agencia de Viajes\Movent"

Write-Host "Paso 1: npm install..."
npm install
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR npm install: $LASTEXITCODE"
    exit 1
}
Write-Host "npm install OK"

Write-Host ""
Write-Host "Paso 2: Ejecutando tests..."
node node_modules\vitest\vitest.mjs run --reporter=verbose
Write-Host "EXIT_TESTS:$LASTEXITCODE"
