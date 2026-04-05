function Clean-Package {
    param([string]$pkg, [string]$name)
    
    $lines = go doc -all $pkg
    $result = [System.Collections.Generic.List[string]]::new()
    $seen = [System.Collections.Generic.HashSet[string]]::new()
    
    $result.Add("# $name")
    $result.Add("")

    # Separar descripción del package del resto del contenido
    $inDescription = $true
    $codeStarted = $false
    $consecutiveEmpty = 0

    foreach ($line in $lines) {
        $trimmed = $line.Trim()

        # Saltar la línea "package X"
        if ($trimmed -match "^[Pp]ackage ") { continue }

        # Mientras estamos en la descripción (texto plano antes del primer bloque)
        if ($inDescription) {
            if ($trimmed -match "^(FUNCTIONS|TYPES|VARIABLES|CONSTANTS|METHODS)$") {
                $inDescription = $false
                if ($codeStarted) {
                    $result.Add('```')
                    $codeStarted = $false
                }
                $result.Add("")
                $result.Add("## $trimmed")
                $result.Add("")
                $result.Add('```go')
                $codeStarted = $true
                $consecutiveEmpty = 0
                continue
            }

            # Texto de descripción va como markdown normal
            if ($trimmed -eq "") {
                if ($consecutiveEmpty -eq 0) { $result.Add("") }
                $consecutiveEmpty++
            } else {
                $consecutiveEmpty = 0
                if (-not $seen.Contains($trimmed)) {
                    $seen.Add($trimmed) | Out-Null
                    $result.Add($trimmed)
                }
            }
            continue
        }

        # Dentro del bloque de código detectar nuevas secciones
        if ($trimmed -match "^(FUNCTIONS|TYPES|VARIABLES|CONSTANTS|METHODS)$") {
            if ($codeStarted) { $result.Add('```') }
            $result.Add("")
            $result.Add("## $trimmed")
            $result.Add("")
            $result.Add('```go')
            $codeStarted = $true
            $consecutiveEmpty = 0
            continue
        }

        # Colapsar líneas vacías dentro del bloque de código
        if ($trimmed -eq "") {
            if ($consecutiveEmpty -lt 1) { $result.Add("") }
            $consecutiveEmpty++
        } else {
            $consecutiveEmpty = 0
            if (-not $seen.Contains($trimmed)) {
                $seen.Add($trimmed) | Out-Null
                $result.Add($line)
            }
        }
    }

    if ($codeStarted) { $result.Add('```') }

    $result | Out-File -Encoding utf8 "docs-go-md/$name.md"
    Write-Host "Generado: $name.md"
}

Clean-Package "agencia-viajes/internal/controllers"  "Controllers"
Clean-Package "agencia-viajes/internal/services"     "Services"
Clean-Package "agencia-viajes/internal/repositories" "Repositories"
Clean-Package "agencia-viajes/internal/dto"          "DTO"
Clean-Package "agencia-viajes/internal/models"       "Models"
Clean-Package "agencia-viajes/internal/middlewares"  "Middlewares"
Clean-Package "agencia-viajes/internal/helpers"      "Helpers"
Clean-Package "agencia-viajes/internal/config"       "Config"
Clean-Package "agencia-viajes/pkg/database"          "Database"
Clean-Package "agencia-viajes/cmd/server"            "Server"

Write-Host "Documentacion generada correctamente"