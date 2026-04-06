# generar-docs-svelte.ps1
$archivos = Get-ChildItem -Recurse src -Include *.svelte

foreach ($archivo in $archivos) {
    $contenido = Get-Content $archivo.FullName -Raw
    
    # Extrae solo el bloque <script>
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

# Generar Main.md desde main.js de forma estatica y limpia
if (Test-Path "src\main.js") {
    $contenido = Get-Content "src\main.js" -Raw
    $block = "# Main`n`n## main.js`n`n> Punto de entrada de la aplicacion. Monta el componente raiz App en el elemento DOM con id app usando el API mount de Svelte 5.`n`n" + '```js' + "`n" + $contenido.Trim() + "`n" + '```'
    [System.IO.File]::WriteAllText("docs-svelte\Main.md", $block, [System.Text.Encoding]::UTF8)
    Write-Host "OK: Main.md"
} else {
    Write-Host "Sin archivo: main.js"
}

# Generar README.md
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