$wordPath = "C:\Proyecto-Final-ADFS-y-DABD1\Cadena de Hoteles\Informe-Pruebas-Unitarias.docx"
$mdPath   = "C:\Proyecto-Final-ADFS-y-DABD1\Cadena de Hoteles\INFORME-WORD-ACTUALIZADO.md"

$CL_H1       = 0x694F59
$CL_H2       = 0x006080
$CL_H3       = 0xA984A8
$CL_H4       = 0x006080
$CL_BOLD     = 0x694F59
$CL_THEAD_BG = 0x694F59
$CL_TEVEN_BG = 0xF0E8F0
$CL_WHITE    = 0xFFFFFF
$CL_BLACK    = 0x000000

Write-Host "=== MIKU INN - ACTUALIZANDO INFORME ===" -ForegroundColor Magenta

if (-not (Test-Path $wordPath)) { Write-Host "ERROR: No existe $wordPath" -ForegroundColor Red; exit 1 }
if (-not (Test-Path $mdPath))   { Write-Host "ERROR: No existe $mdPath"   -ForegroundColor Red; exit 1 }

$mdLines = Get-Content $mdPath -Encoding UTF8
Write-Host "Markdown leido: $($mdLines.Count) lineas" -ForegroundColor Cyan

try {
    $word = New-Object -ComObject Word.Application
} catch {
    Write-Host "ERROR: Word no disponible" -ForegroundColor Red; exit 1
}
$word.Visible       = $false
$word.DisplayAlerts = 0

function Set-NormalFont { param($sel)
    $sel.Font.Name      = "Calibri"
    $sel.Font.Size      = 11
    $sel.Font.Bold      = $false
    $sel.Font.Italic    = $false
    $sel.Font.Color     = 0
    $sel.ParagraphFormat.Alignment        = 3
    $sel.ParagraphFormat.SpaceAfter       = 6
    $sel.ParagraphFormat.SpaceBefore      = 0
    $sel.ParagraphFormat.LineSpacingRule  = 0
    $sel.ParagraphFormat.KeepWithNext     = $false
    $sel.ParagraphFormat.KeepTogether     = $true
    $sel.ParagraphFormat.WidowControl     = $true
}

function Set-Heading { param($sel, [int]$level)
    $sizes    = @(0, 22, 16, 13, 12)
    $colors   = @(0, $CL_H1, $CL_H2, $CL_H3, $CL_H4)
    $sel.Font.Name      = "Calibri"
    $sel.Font.Size      = $sizes[$level]
    $sel.Font.Bold      = $true
    $sel.Font.Italic    = $false
    $sel.Font.Color     = $colors[$level]
    $sel.ParagraphFormat.Alignment       = 0
    $sel.ParagraphFormat.SpaceAfter      = 6
    $sel.ParagraphFormat.SpaceBefore     = $(if ($level -le 2) {14} else {10})
    $sel.ParagraphFormat.KeepWithNext    = $true
    $sel.ParagraphFormat.KeepTogether    = $true
}

function Write-Inline { param($sel, [string]$text)
    $parts = $text -split '(\*\*[^*]+\*\*)'
    foreach ($p in $parts) {
        if ($p -match '^\*\*(.+)\*\*$') {
            $sel.Font.Bold  = $true
            $sel.Font.Color = $CL_BOLD
            $sel.TypeText($matches[1])
            $sel.Font.Bold  = $false
            $sel.Font.Color = 0
        } elseif ($p -ne '') {
            $sel.TypeText($p)
        }
    }
}

function Flush-Table { param($doc, $sel, $rows)
    if ($rows.Count -eq 0) { return }
    $dataRows = $rows | Where-Object { $_ -notmatch '^\|[\s\-\|:]+$' }
    if ($dataRows.Count -eq 0) { return }
    $cols = ($dataRows[0] -split '\|' | Where-Object { $_ -ne '' }).Count
    if ($cols -lt 1) { return }

    $tbl = $doc.Tables.Add($sel.Range, $dataRows.Count, $cols)
    $tbl.Borders.Enable = $true

    for ($b = 1; $b -le 6; $b++) {
        try {
            $tbl.Borders.Item($b).Color     = $CL_H2
            $tbl.Borders.Item($b).LineWidth = 8
        } catch {}
    }

    for ($r = 0; $r -lt $dataRows.Count; $r++) {
        $cells = $dataRows[$r] -split '\|' | Where-Object { $_ -ne '' }
        for ($c = 0; $c -lt [math]::Min($cells.Count, $cols); $c++) {
            $ct = $cells[$c].Trim() -replace '\*\*(.+?)\*\*','$1' -replace '`(.+?)`','$1'
            try {
                $cell = $tbl.Cell($r+1, $c+1)
                $cell.Range.Text = $ct
                if ($r -eq 0) {
                    $cell.Shading.BackgroundPatternColor = $CL_THEAD_BG
                    $cell.Range.Font.Color = $CL_WHITE
                    $cell.Range.Font.Bold  = $true
                    $cell.Range.Font.Size  = 10
                } else {
                    if ($r % 2 -eq 0) {
                        $cell.Shading.BackgroundPatternColor = $CL_TEVEN_BG
                    } else {
                        $cell.Shading.BackgroundPatternColor = $CL_WHITE
                    }
                    $cell.Range.Font.Color = $CL_BLACK
                    $cell.Range.Font.Bold  = $false
                    $cell.Range.Font.Size  = 9
                }
                $cell.Range.Font.Name = "Calibri"
            } catch {}
        }
    }

    try { $tbl.AutoFitBehavior(2) } catch {}
    $sel.SetRange($tbl.Range.End, $tbl.Range.End)
    $sel.TypeParagraph()
}

try {
    Write-Host "Abriendo documento original..." -ForegroundColor Cyan
    $doc = $word.Documents.Open($wordPath)

    $doc.PageSetup.TopMargin    = 72
    $doc.PageSetup.BottomMargin = 72
    $doc.PageSetup.LeftMargin   = 90
    $doc.PageSetup.RightMargin  = 90

    Write-Host "Borrando contenido anterior..." -ForegroundColor Yellow
    $doc.Content.Delete()
    $sel = $word.Selection

    $inCode  = $false
    $inTable = $false
    $tRows   = @()
    $count   = 0

    foreach ($raw in $mdLines) {
        $line = $raw.TrimEnd()
        $count++
        if ($count % 100 -eq 0) { Write-Host "  Procesadas $count lineas..." -ForegroundColor Gray }

        if ($line -match '^```') {
            if ($inCode) {
                Set-NormalFont $sel
                $inCode = $false
            } else {
                if ($inTable) { Flush-Table $doc $sel $tRows; $tRows = @(); $inTable = $false }
                $inCode = $true
                $sel.Font.Name  = "Consolas"
                $sel.Font.Size  = 9
                $sel.Font.Bold  = $false
                $sel.Font.Color = $CL_BLACK
                $sel.ParagraphFormat.Alignment = 0
            }
            continue
        }
        if ($inCode) { $sel.TypeText($line); $sel.TypeParagraph(); continue }

        if ($line -match '^\|') { $tRows += $line; $inTable = $true; continue }
        if ($inTable) { Flush-Table $doc $sel $tRows; $tRows = @(); $inTable = $false }

        $clean = $line -replace '`(.+?)`','$1'

        if    ($line -match '^#### (.+)$') { Set-Heading $sel 4; $sel.TypeText(($matches[1] -replace '\*\*(.+?)\*\*','$1' -replace '`(.+?)`','$1')); $sel.TypeParagraph(); Set-NormalFont $sel }
        elseif($line -match '^### (.+)$')  { Set-Heading $sel 3; $sel.TypeText(($matches[1] -replace '\*\*(.+?)\*\*','$1' -replace '`(.+?)`','$1')); $sel.TypeParagraph(); Set-NormalFont $sel }
        elseif($line -match '^## (.+)$')   { Set-Heading $sel 2; $sel.TypeText(($matches[1] -replace '\*\*(.+?)\*\*','$1' -replace '`(.+?)`','$1')); $sel.TypeParagraph(); Set-NormalFont $sel }
        elseif($line -match '^# (.+)$')    { Set-Heading $sel 1; $sel.TypeText(($matches[1] -replace '\*\*(.+?)\*\*','$1' -replace '`(.+?)`','$1')); $sel.TypeParagraph(); Set-NormalFont $sel }
        elseif($line -match '^---+$') {
            Set-NormalFont $sel
            $sel.Font.Color = $CL_H3
            $sel.TypeText("____________________________________________________________")
            $sel.Font.Color = 0
            $sel.TypeParagraph()
        }
        elseif($line -match '^[-*] (.+)$') {
            Set-NormalFont $sel
            $sel.ParagraphFormat.LeftIndent = 36
            $sel.TypeText("  - ")
            Write-Inline $sel ($matches[1] -replace '`(.+?)`','$1')
            $sel.TypeParagraph()
            $sel.ParagraphFormat.LeftIndent = 0
        }
        elseif($line -match '^(\d+)\. (.+)$') {
            Set-NormalFont $sel
            $sel.ParagraphFormat.LeftIndent = 36
            $sel.TypeText("  $($matches[1]). ")
            Write-Inline $sel ($matches[2] -replace '`(.+?)`','$1')
            $sel.TypeParagraph()
            $sel.ParagraphFormat.LeftIndent = 0
        }
        elseif($line -eq '') { Set-NormalFont $sel; $sel.TypeParagraph() }
        else { Set-NormalFont $sel; Write-Inline $sel $clean; $sel.TypeParagraph() }
    }

    if ($inTable) { Flush-Table $doc $sel $tRows }

    Write-Host "Contenido insertado: $count lineas" -ForegroundColor Green
    Write-Host "Guardando con tema Miku Inn..." -ForegroundColor Magenta
    $doc.Save()
    $doc.Close()

    Write-Host ""
    Write-Host "WORD ACTUALIZADO - TEMA MIKU INN" -ForegroundColor Magenta
    Write-Host "Titulos H1: Morado oscuro #694F59" -ForegroundColor Cyan
    Write-Host "Titulos H2/H4: Azul oscuro #006080" -ForegroundColor Cyan
    Write-Host "Titulos H3: Morado claro #A984A8" -ForegroundColor Cyan
    Write-Host "Tablas: cabecera morada + filas alternas" -ForegroundColor Cyan
    Write-Host "Ubicacion: $wordPath" -ForegroundColor Green

} catch {
    Write-Host "ERROR en linea $count`: $($_.Exception.Message)" -ForegroundColor Red
    try { $doc.Close($false) } catch {}
} finally {
    $word.Quit()
    [System.Runtime.Interopservices.Marshal]::ReleaseComObject($word) | Out-Null
    Write-Host "Word cerrado." -ForegroundColor Cyan
}
