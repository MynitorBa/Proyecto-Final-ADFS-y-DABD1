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

# main.js por separado - no es .svelte, se documenta manualmente
$mainMd = @"
# Main

Punto de entrada de la aplicacion AirLine Broom.

Usa el API mount() de Svelte 5 para montar el componente raiz App
en el elemento #app definido en index.html.

## app

Instancia montada de la aplicacion Svelte adjunta al elemento #app del DOM.

Type: object

## Dependencias

- svelte: API de montaje (mount)
- App.svelte: Componente raiz que contiene el router y el layout global
"@

Set-Content "docs-svelte/Main.md" $mainMd -Encoding UTF8
Write-Host "OK: Main.md"

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