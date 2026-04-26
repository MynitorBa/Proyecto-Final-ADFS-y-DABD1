$javaHome = "$env:USERPROFILE\.jdks\ms-17.0.18"
$mvnCmd = "mvn"

Write-Host "Corriendo tests..."
$env:JAVA_HOME = $javaHome
& $mvnCmd test "-Dfile.encoding=UTF-8"
Write-Host "Tests finalizados."

$reportsDir = "target\surefire-reports"
$out        = "docs-java\Tests.md"

if (-not (Test-Path $reportsDir)) {
    Write-Host "No se encontraron reportes en $reportsDir"
    exit 1
}

$xmlFiles = Get-ChildItem "$reportsDir\TEST-*.xml" -ErrorAction SilentlyContinue
if (-not $xmlFiles) {
    Write-Host "No hay archivos TEST-*.xml en $reportsDir"
    exit 1
}

$result      = [System.Collections.Generic.List[string]]::new()
$totalTests  = 0
$totalPass   = 0
$totalFail   = 0
$totalSkip   = 0
$totalTimeMs = 0
$suiteBlocks = [System.Collections.Generic.List[string]]::new()

foreach ($file in $xmlFiles) {
    [xml]$xml  = Get-Content $file.FullName -Encoding UTF8
    $suite     = $xml.testsuite
    $suiteName = $suite.name -replace "org\.example\.", ""
    $tests     = [int]$suite.tests
    $failures  = [int]$suite.failures
    $errors    = [int]$suite.errors
    $skipped   = if ($suite.skipped) { [int]$suite.skipped } else { 0 }
    $timeMs    = [math]::Round([double]$suite.time * 1000)
    $passed    = $tests - $failures - $errors - $skipped

    $totalTests  += $tests
    $totalPass   += $passed
    $totalFail   += ($failures + $errors)
    $totalSkip   += $skipped
    $totalTimeMs += $timeMs

    $block = [System.Collections.Generic.List[string]]::new()
    $block.Add("### $suiteName")
    $block.Add("")
    $block.Add("- Tests: $tests")
    $block.Add("- Pasaron: $passed")
    $block.Add("- Fallaron: $($failures + $errors)")
    $block.Add("")

    foreach ($tc in $suite.testcase) {
        $tcName   = $tc.name
        $tcTimeMs = [math]::Round([double]$tc.time * 1000)
        $tcDur    = if ($tcTimeMs -ge 1000) { "$([math]::Round($tcTimeMs/1000,1))s" } else { "${tcTimeMs}ms" }

        if ($tc.failure -or $tc.error) {
            $estado = "FALLO"
        } elseif ($tc.skipped -ne $null) {
            $estado = "SALTADO"
        } else {
            $estado = "PASO"
        }

        $block.Add("**[$estado]** $tcName ``($tcDur)``")
        $block.Add("")
    }

    $block.Add("---")
    $block.Add("")
    $suiteBlocks.Add(($block -join "`n"))
}

$fechaEjecucion = Get-Date -Format "dd/MM/yyyy HH:mm:ss"
$durTotal       = if ($totalTimeMs -ge 1000) { "$([math]::Round($totalTimeMs/1000,1))s" } else { "${totalTimeMs}ms" }
$estadoGlobal   = if ($totalFail -gt 0) { "CON FALLOS" } else { "TODO VERDE" }

$result.Add("# Tests")
$result.Add("")
$result.Add("> Reporte de pruebas unitarias generado automaticamente desde JUnit 5 + Mockito.")
$result.Add("")
$result.Add("## Resumen")
$result.Add("")
$result.Add("- Estado: $estadoGlobal")
$result.Add("- Total: $totalTests")
$result.Add("- Pasaron: $totalPass")
$result.Add("- Fallaron: $totalFail")
$result.Add("- Saltados: $totalSkip")
$result.Add("- Duracion: $durTotal")
$result.Add("- Ejecutado: $fechaEjecucion")
$result.Add("")
$result.Add("---")
$result.Add("")
$result.Add("## Suites")
$result.Add("")

foreach ($b in $suiteBlocks) {
    $result.Add($b)
}

[System.IO.File]::WriteAllText($out, ($result -join "`n"), [System.Text.Encoding]::UTF8)
Write-Host "Generado: $out ($totalTests tests, $totalPass pasaron, $totalFail fallaron)"