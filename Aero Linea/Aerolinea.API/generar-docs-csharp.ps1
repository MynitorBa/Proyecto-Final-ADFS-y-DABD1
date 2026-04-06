$src = ".\Aerolinea.API"
    $out = "docs-csharp"
    if (-not (Test-Path $out)) { New-Item -ItemType Directory -Path $out | Out-Null }
    $paquetes = @("Controllers", "Services", "Repositories", "DTOs", "Models", "Helpers", "Data")
    foreach ($pkg in $paquetes) {
        $name = $pkg
        $files = Get-ChildItem "$src\$pkg\*.cs" -Recurse -ErrorAction SilentlyContinue
        if (-not $files) { Write-Host "Sin archivos: $pkg"; continue }
        $result = [System.Collections.Generic.List[string]]::new()
        $result.Add("# $name"); $result.Add("")
        foreach ($file in $files) {
            $lines = Get-Content $file.FullName
            $className = $file.BaseName
            $result.Add("## $className"); $result.Add("")
            $bufDoc = [System.Collections.Generic.List[string]]::new()
            $foundClass = $false
            $classDoc = @()
            foreach ($line in $lines) {
                $trimmed = $line.Trim()
                if (-not $foundClass) {
                    if ($trimmed -match "^///\s?(.*)") {
                        $text = $Matches[1].Trim() -replace "<summary>","" -replace "</summary>","" -replace "<remarks>","" -replace "</remarks>","" -replace '<param name="([^"]+)">','@param $1 ' -replace "</param>","" -replace "<returns>","@return " -replace "</returns>","" -replace '<exception cref="([^"]+)">','@throws $1 ' -replace "</exception>","" -replace "<c>","``" -replace "</c>","``" -replace "<[^>]+>",""
                        $text = $text.Trim(); if ($text -ne "") { $bufDoc.Add($text) }
                    } elseif ($trimmed -match "^(public|internal|abstract|sealed|static).*class\s" -or $trimmed -match "^(public|internal).*interface\s" -or $trimmed -match "^(public|internal).*enum\s") {
                        $foundClass = $true; $classDoc = $bufDoc.ToArray(); $bufDoc = [System.Collections.Generic.List[string]]::new()
                    } elseif ($trimmed -ne "" -and $trimmed -notmatch "^\[" -and $trimmed -notmatch "^using " -and $trimmed -notmatch "^namespace ") {
                        $bufDoc = [System.Collections.Generic.List[string]]::new()
                    }
                }
            }
            if ($classDoc.Count -gt 0) {
                $desc = $classDoc | Where-Object { $_ -notmatch "^@" }
                if ($desc) { $result.Add("> " + ($desc -join " ")); $result.Add("") }
            }
            $metodos = [System.Collections.Generic.List[object]]::new()
            $bufDoc = [System.Collections.Generic.List[string]]::new()
            foreach ($line in $lines) {
                $trimmed = $line.Trim()
                if ($trimmed -match "^///\s?(.*)") {
                    $text = $Matches[1].Trim() -replace "<summary>","" -replace "</summary>","" -replace "<remarks>","" -replace "</remarks>","" -replace '<param name="([^"]+)">','@param $1 ' -replace "</param>","" -replace "<returns>","@return " -replace "</returns>","" -replace '<exception cref="([^"]+)">','@throws $1 ' -replace "</exception>","" -replace "<c>","``" -replace "</c>","``" -replace "<[^>]+>",""
                    $text = $text.Trim(); if ($text -ne "") { $bufDoc.Add($text) }; continue
                }
                if (($trimmed -match "^(public|protected|internal)\s+(static\s+)?(override\s+)?(async\s+)?(virtual\s+)?(Task|IActionResult|ActionResult|void|int|long|double|float|bool|string|List|Dictionary|IEnumerable|var|Object|HttpResponseMessage|IResult)") -or
                ($trimmed -match "^(public|protected|internal)\s+(static\s+)?(override\s+)?(async\s+)?(virtual\s+)?(SqlConnection|OracleConnection|IDbConnection|byte\[\])")) {
                    if ($trimmed -match "\{\s*get;" -or $trimmed -match "=>") { $bufDoc = [System.Collections.Generic.List[string]]::new(); continue }
                    $firma = $trimmed -replace "\s*\{.*$",""
                    $metodos.Add([PSCustomObject]@{ Firma = $firma.Trim(); Doc = $bufDoc.ToArray() })
                    $bufDoc = [System.Collections.Generic.List[string]]::new()
                } elseif ($trimmed -ne "" -and $trimmed -notmatch "^///" -and $trimmed -notmatch "^\[" -and $trimmed -notmatch "^{" -and $trimmed -notmatch "^}") {
                    $bufDoc = [System.Collections.Generic.List[string]]::new()
                }
            }
            foreach ($m in $metodos) {
                $descM = $m.Doc | Where-Object { $_ -notmatch "^@" }
                $tagsM = $m.Doc | Where-Object { $_ -match "^@" }
                $result.Add('```csharp'); $result.Add($m.Firma); $result.Add('```'); $result.Add("")
                if ($descM) { $result.Add(($descM -join " ")); $result.Add("") }
                if ($tagsM) { foreach ($tag in $tagsM) {
                    if ($tag -match "^@param\s+(\S+)\s+(.+)") { $result.Add("- **Param** ``" + $Matches[1] + "`` - " + $Matches[2]) }
                    elseif ($tag -match "^@return\s+(.+)") { $result.Add("- **Returns** - " + $Matches[1]) }
                    elseif ($tag -match "^@throws\s+(\S+)\s+(.+)") { $result.Add("- **Throws** ``" + $Matches[1] + "`` - " + $Matches[2]) }
                    else { $result.Add("- ``$tag``") }
                }; $result.Add("") }
                $result.Add("---"); $result.Add("")
            }
        }
        [System.IO.File]::WriteAllText("$out\$name.md", ($result -join "`n"), [System.Text.Encoding]::UTF8)
        Write-Host "Generado: $name.md"
    }

    # Generar Program.md desde Program.cs
    if (Test-Path "$src\Program.cs") {
        $lines = Get-Content "$src\Program.cs"
        $result = [System.Collections.Generic.List[string]]::new()
        $result.Add("# Program"); $result.Add("")
        $result.Add("## Program.cs"); $result.Add("")
        $result.Add("> Punto de entrada de la aplicacion. Configura el servidor, registra middleware, controllers y servicios."); $result.Add("")
        $bufDoc = [System.Collections.Generic.List[string]]::new()
        foreach ($line in $lines) {
            $trimmed = $line.Trim()
            if ($trimmed -match "^///\s?(.*)") {
                $text = $Matches[1].Trim() -replace "<summary>","" -replace "</summary>","" -replace "<remarks>","" -replace "</remarks>","" -replace '<param name="([^"]+)">','@param $1 ' -replace "</param>","" -replace "<returns>","@return " -replace "</returns>","" -replace '<exception cref="([^"]+)">','@throws $1 ' -replace "</exception>","" -replace "<c>","``" -replace "</c>","``" -replace "<[^>]+>",""
                $text = $text.Trim(); if ($text -ne "") { $bufDoc.Add($text) }; continue
            }
            if (($trimmed -match "^(app|builder|context)\." -or $trimmed -match "^var ") -and $trimmed.Length -gt 10) {
                $result.Add('```csharp'); $result.Add($trimmed); $result.Add('```'); $result.Add("")
                if ($bufDoc.Count -gt 0) {
                    $result.Add(($bufDoc -join " ")); $result.Add("")
                    $bufDoc = [System.Collections.Generic.List[string]]::new()
                }
            } elseif ($trimmed -ne "" -and $trimmed -notmatch "^///") {
                $bufDoc = [System.Collections.Generic.List[string]]::new()
            }
        }
        [System.IO.File]::WriteAllText("$out\Program.md", ($result -join "`n"), [System.Text.Encoding]::UTF8)
        Write-Host "Generado: Program.md"
    } else {
        Write-Host "Sin archivo: Program.cs"
    }

    # Generar README.md
    $readme = "# Broom AirLine - Documentacion Tecnica Backend`n`n> Backend C# ASP.NET para la aerolinea Broom AirLine. Proyecto final UNIS 2026.`n`n## Tecnologias`n`n- C# / ASP.NET Core`n- Oracle Database`n- Arquitectura por capas: Controllers, Services, Repositories`n`n## Estructura`n`n- Controllers - Endpoints REST`n- Services - Logica de negocio`n- Repositories - Acceso a datos`n- Models - Entidades`n- DTOs - Objetos de transferencia`n- Helpers - Utilidades`n- Data - Conexion a base de datos`n- Program - Punto de entrada de la aplicacion"
    [System.IO.File]::WriteAllText("$out\README.md", $readme, [System.Text.Encoding]::UTF8)
    Write-Host "Generado: README.md"

    Write-Host ""
    Write-Host "Documentacion C# generada. Ejecuta: docsify serve docs-csharp"