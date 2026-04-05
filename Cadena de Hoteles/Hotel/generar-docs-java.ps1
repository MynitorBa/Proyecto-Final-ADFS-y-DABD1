$src = "src\main\java\org\example"
$out = "docs-java"

if (-not (Test-Path $out)) { New-Item -ItemType Directory -Path $out | Out-Null }

$paquetes = @("controllers", "services", "repositories", "dtos", "models", "helpers", "config", "data")

foreach ($pkg in $paquetes) {
    $name = $pkg.Substring(0,1).ToUpper() + $pkg.Substring(1)
    $files = Get-ChildItem "$src\$pkg\*.java" -Recurse -ErrorAction SilentlyContinue
    if (-not $files) {
        Write-Host "Sin archivos: $pkg"
        continue
    }

    $result = [System.Collections.Generic.List[string]]::new()
    $result.Add("# $name")
    $result.Add("")

    foreach ($file in $files) {
        $lines = Get-Content $file.FullName
        $className = $file.BaseName

        $result.Add("## $className")
        $result.Add("")

        # Extraer el primer bloque /** */ que precede a la declaracion de clase
        $classDoc   = [System.Collections.Generic.List[string]]::new()
        $inDoc      = $false
        $foundClass = $false
        foreach ($line in $lines) {
            $trimmed = $line.Trim()
            if (-not $foundClass) {
                if ($trimmed -eq "/**") { $inDoc = $true; $classDoc.Clear(); continue }
                if ($trimmed -eq "*/")  { $inDoc = $false; continue }
                if ($inDoc -and $trimmed.StartsWith("*")) {
                    $text = $trimmed.TrimStart('*').Trim()
                    if ($text -ne "") { $classDoc.Add($text) }
                }
                if ($trimmed -match "^(public class|public interface|public enum)") {
                    $foundClass = $true
                }
            }
        }

        # Separar descripcion general de etiquetas @param/@return/@throws
        if ($classDoc.Count -gt 0) {
            $desc = $classDoc | Where-Object { $_ -notmatch "^@" }
            $tags = $classDoc | Where-Object { $_ -match "^@" }

            if ($desc) {
                $result.Add("> " + ($desc -join " "))
                $result.Add("")
            }
            if ($tags) {
                foreach ($tag in $tags) {
                    $result.Add("- ``$tag``")
                }
                $result.Add("")
            }
        }

        # Extraer cada metodo con su Javadoc individual
        $metodos = [System.Collections.Generic.List[object]]::new()
        $bufDoc  = [System.Collections.Generic.List[string]]::new()
        $inDoc   = $false

        foreach ($line in $lines) {
            $trimmed = $line.Trim()

            if ($trimmed -eq "/**") {
                $inDoc  = $true
                $bufDoc = [System.Collections.Generic.List[string]]::new()
                continue
            }
            if ($trimmed -eq "*/") {
                $inDoc = $false
                continue
            }
            if ($inDoc -and $trimmed.StartsWith("*")) {
                $text = $trimmed.TrimStart('*').Trim()
                if ($text -ne "") { $bufDoc.Add($text) }
                continue
            }

            # Detectar firma de metodo publico o protegido
            if ($trimmed -match "^(public|protected)\s+.*(void|int|long|double|float|boolean|String|List|Map|Optional|Object|Response|Javalin|Connection)") {
                $firma = $trimmed -replace "\s*\{.*$", ""
                $metodos.Add([PSCustomObject]@{
                    Firma = $firma.Trim()
                    Doc   = $bufDoc.ToArray()
                })
                $bufDoc = [System.Collections.Generic.List[string]]::new()
            } elseif (-not $inDoc -and $trimmed -ne "") {
                $bufDoc = [System.Collections.Generic.List[string]]::new()
            }
        }

        # Renderizar cada metodo
        foreach ($m in $metodos) {
            $descM = $m.Doc | Where-Object { $_ -notmatch "^@" }
            $tagsM = $m.Doc | Where-Object { $_ -match "^@" }

            $result.Add('```java')
            $result.Add($m.Firma)
            $result.Add('```')
            $result.Add("")

            if ($descM) {
                $result.Add(($descM -join " "))
                $result.Add("")
            }

            if ($tagsM) {
                foreach ($tag in $tagsM) {
                    if ($tag -match "^@param\s+(\S+)\s+(.+)") {
                        $result.Add("- **Param** ``" + $Matches[1] + "`` - " + $Matches[2])
                    } elseif ($tag -match "^@return\s+(.+)") {
                        $result.Add("- **Returns** - " + $Matches[1])
                    } elseif ($tag -match "^@throws\s+(\S+)\s+(.+)") {
                        $result.Add("- **Throws** ``" + $Matches[1] + "`` - " + $Matches[2])
                    } else {
                        $result.Add("- ``$tag``")
                    }
                }
                $result.Add("")
            }

            $result.Add("---")
            $result.Add("")
        }
    }

    [System.IO.File]::WriteAllText("$out\$name.md", ($result -join "`n"), [System.Text.Encoding]::UTF8)
    Write-Host "Generado: $name.md"
}

# Generar README.md
$readme = @"
# Miku Inn - Documentacion Tecnica Backend

> Backend Java/Javalin para la cadena hotelera Miku Inn. Proyecto final UNIS 2026.

## Tecnologias

- Java / Javalin
- Oracle Database
- Arquitectura por capas: controllers, services, repositories

## Estructura

- controllers - Endpoints REST
- services - Logica de negocio
- repositories - Acceso a datos
- models - Entidades
- dtos - Objetos de transferencia
- helpers - Utilidades
- config - Configuracion del servidor
- data - Conexion a base de datos
"@

[System.IO.File]::WriteAllText("$out\README.md", $readme, [System.Text.Encoding]::UTF8)
Write-Host "Generado: README.md"

Write-Host "Documentacion Java generada"