# generar-docs-svelte.ps1
$archivos = Get-ChildItem -Recurse src -Include *.svelte

foreach ($archivo in $archivos) {
    $contenido = Get-Content $archivo.FullName -Raw
    
    if ($contenido -match '(?s)<script[^>]*>(.*?)</script>') {
        $script = $matches[1]
        $temp = "$env:TEMP\temp_svelte.js"
        Set-Content $temp $script -Encoding UTF8
        
        $nombre = $archivo.BaseName
        documentation build $temp -f md > "docs-svelte/$nombre.md"
        Write-Host "OK: $nombre.md"
    } else {
        Write-Host "Sin script: $($archivo.Name)"
    }
}

$readme = @"
# Documentacion Svelte - AirLine Broom

Documentacion autogenerada de los componentes Svelte del proyecto AirLine Broom.

## Requisitos

- PowerShell
- documentation.js instalado globalmente: npm i -g documentation

## Generar documentacion

Ejecutar desde la raiz del proyecto:

./generar-docs-svelte.ps1
"@

Set-Content "docs-svelte/README.md" $readme -Encoding UTF8
Write-Host "OK: README.md generado"