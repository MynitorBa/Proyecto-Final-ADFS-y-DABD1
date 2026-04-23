$out = "docs-go-md\Tests.md"

Write-Host "Corriendo tests Go..." -ForegroundColor Cyan
$rawOutput = go test ./test/... -v 2>&1
$rawOutput | ForEach-Object { Write-Host $_ }
Write-Host "Tests finalizados." -ForegroundColor Cyan

$pass  = ($rawOutput | Select-String "--- PASS").Count
$fail  = ($rawOutput | Select-String "--- FAIL").Count
$total = $pass + $fail
$estado = if ($fail -gt 0) { "CON FALLOS" } else { "TODO VERDE" }
$fecha  = Get-Date -Format "dd/MM/yyyy HH:mm:ss"

$lines = [System.Collections.Generic.List[string]]::new()
$lines.Add("# Tests Go - Agencia de Viajes")
$lines.Add("")
$lines.Add("> Reporte de pruebas unitarias generado automaticamente.")
$lines.Add("")
$lines.Add("## Resumen")
$lines.Add("")
$lines.Add("- Estado: **$estado**")
$lines.Add("- Total: $total")
$lines.Add("- Pasaron: $pass")
$lines.Add("- Fallaron: $fail")
$lines.Add("- Ejecutado: $fecha")
$lines.Add("")
$lines.Add("---")
$lines.Add("")
$lines.Add("## Detalle")
$lines.Add("")

$currentSuite = ""
foreach ($line in $rawOutput) {
    if ($line -match "^=== RUN\s+Test[^/]+$") {
        $suite = ($line -replace "=== RUN\s+", "").Trim()
        if ($suite -ne $currentSuite) {
            $currentSuite = $suite
            $lines.Add("### $suite")
            $lines.Add("")
        }
    }
    if ($line -match "--- (PASS|FAIL): (.+) \((.+)\)") {
        $e = $Matches[1]; $n = $Matches[2]; $d = $Matches[3]
        $lines.Add("**[$e]** ``$n`` ($d)")
        $lines.Add("")
    }
}

$lines.Add("---")
$lines.Add("")
$lines.Add("_Generado por generar-tests-go.ps1_")

[System.IO.File]::WriteAllText($out, ($lines -join "`n"), [System.Text.Encoding]::UTF8)
Write-Host "Generado: $out ($total tests, $pass pasaron, $fail fallaron)" -ForegroundColor Green