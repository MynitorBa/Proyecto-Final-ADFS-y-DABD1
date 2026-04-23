# Script generar Word Broom AirLine con diseno de Miku Inn

$wordPath = "C:\Proyecto-Final-ADFS-y-DABD1\Aero Linea\Informe-Pruebas-Unitarias-AeroLinea.docx"
$mdPath   = "C:\Proyecto-Final-ADFS-y-DABD1\Aero Linea\INFORME-COMPLETO-TESTS-AEROLINEA.md"

if (-not (Test-Path $mdPath)) {
    Write-Host "ERROR: No se encontro el archivo Markdown." -ForegroundColor Red
    exit 1
}

# ── Borrar Word anterior ─────────────────────────────────────────────────────
if (Test-Path $wordPath) {
    Remove-Item $wordPath -Force
    Write-Host "Word anterior eliminado." -ForegroundColor Yellow
}

# ── COLORES BROOM AIRLINE (formato BGR de Word COM) ──────────────────────────
# Miku Inn usaba: H1=#694F59 H2=#006080 H3=#A984A8 Tabla=#694F59 Border=#006080
# Broom AirLine sustituye con tonos cafe/naranja
$wdAuto         = -16777216   # wdColorAutomatic — sin sombreado

$CL_H1          = 0x181A1C   # Negro cafe oscuro  (RGB #1C1A18)
$CL_H2          = 0x4A6B8B   # Cafe dorado        (RGB #8B6B4A)
$CL_H3          = 0x3A4A5C   # Cafe oscuro        (RGB #5C4A3A)
$CL_H4          = 0x4A8BB0   # Naranja cafe       (RGB #B08B4A)
$CL_SEPARATOR   = 0x3A4A5C   # Igual que H3

$CL_TH_BG       = 0x4A6B8B   # Header tabla bg    (RGB #8B6B4A)
$CL_TH_FG       = 0xFFFFFF   # Header tabla texto (blanco)
$CL_TR_WHITE    = 0xFFFFFF   # Fila blanca
$CL_TR_BEIGE    = 0xEAEFF2   # Fila beige claro   (RGB #F2EFEA)
$CL_TBL_BORDER  = 0x3A4A5C   # Borde tabla        (RGB #5C4A3A)

$CL_CODE_BG     = 0xEBF0F5   # Fondo bloque codigo (gris azulado claro)
$CL_CODE_FG     = 0x181A1C   # Texto codigo

Write-Host "Generando Word con diseno Miku Inn + colores Broom AirLine..." -ForegroundColor Cyan

$word = New-Object -ComObject Word.Application
$word.Visible = $false

# ── Helpers ──────────────────────────────────────────────────────────────────

function Reset-Format($s) {
    $s.Style = "Normal"
    $s.Font.Name    = "Calibri"
    $s.Font.Size    = 11
    $s.Font.Bold    = $false
    $s.Font.Italic  = $false
    $s.Font.Color   = 0x000000
    $s.Shading.BackgroundPatternColor = $wdAuto
    $s.ParagraphFormat.SpaceBefore    = 0
    $s.ParagraphFormat.SpaceAfter     = 6
    $s.ParagraphFormat.LeftIndent     = 0
}

function Write-H1($s, $text) {
    Reset-Format $s
    $s.Font.Size    = 22
    $s.Font.Bold    = $true
    $s.Font.Color   = $CL_H1
    $s.ParagraphFormat.SpaceBefore = 14
    $s.ParagraphFormat.SpaceAfter  = 6
    $s.TypeText($text)
    $s.TypeParagraph()
    Reset-Format $s
}

function Write-H2($s, $text) {
    Reset-Format $s
    $s.Font.Size    = 16
    $s.Font.Bold    = $true
    $s.Font.Color   = $CL_H2
    $s.ParagraphFormat.SpaceBefore = 14
    $s.ParagraphFormat.SpaceAfter  = 6
    $s.TypeText($text)
    $s.TypeParagraph()
    Reset-Format $s
}

function Write-H3($s, $text) {
    Reset-Format $s
    $s.Font.Size    = 13
    $s.Font.Bold    = $true
    $s.Font.Color   = $CL_H3
    $s.ParagraphFormat.SpaceBefore = 10
    $s.ParagraphFormat.SpaceAfter  = 6
    $s.TypeText($text)
    $s.TypeParagraph()
    Reset-Format $s
}

function Write-H4($s, $text) {
    Reset-Format $s
    $s.Font.Size    = 12
    $s.Font.Bold    = $true
    $s.Font.Color   = $CL_H4
    $s.ParagraphFormat.SpaceBefore = 8
    $s.ParagraphFormat.SpaceAfter  = 4
    $s.TypeText($text)
    $s.TypeParagraph()
    Reset-Format $s
}

function Write-Separator($s) {
    Reset-Format $s
    $s.Font.Color = $CL_SEPARATOR
    $s.TypeText("____________________________________________________________")
    $s.TypeParagraph()
    Reset-Format $s
}

function Write-InlineText($s, $line) {
    # Parsear **bold**, `code`, *italic* inline
    $remaining = $line
    $pattern   = '(\*\*[^*]+\*\*|`[^`]+`|\*[^*]+\*)'
    $segments  = [System.Text.RegularExpressions.Regex]::Split($remaining, $pattern)
    foreach ($seg in $segments) {
        if ($seg -eq '') { continue }
        if ($seg -match '^\*\*(.+)\*\*$') {
            $s.Font.Bold = $true
            $s.TypeText($matches[1])
            $s.Font.Bold = $false
        } elseif ($seg -match '^`(.+)`$') {
            $prev = $s.Font.Name; $prevSz = $s.Font.Size; $prevCl = $s.Font.Color
            $s.Font.Name  = "Consolas"
            $s.Font.Size  = 9.5
            $s.Font.Color = $CL_CODE_FG
            $s.TypeText($matches[1])
            $s.Font.Name  = $prev
            $s.Font.Size  = $prevSz
            $s.Font.Color = $prevCl
        } elseif ($seg -match '^\*(.+)\*$') {
            $s.Font.Italic = $true
            $s.TypeText($matches[1])
            $s.Font.Italic = $false
        } else {
            $s.TypeText($seg)
        }
    }
}

function Render-Table($doc, $tableLines) {
    # Filtrar separador y obtener filas de datos
    $dataRows = @($tableLines | Where-Object { $_ -notmatch '^\|[\s\-\|:]+\|$' })
    $rows     = $dataRows.Count
    if ($rows -lt 1) { return }

    # Contar columnas desde primera fila de datos
    $parts = ($dataRows[0] -split '\|') | Where-Object { $_.Trim() -ne '' }
    $cols  = $parts.Count
    if ($cols -lt 1) { return }

    # Agregar tabla al rango actual del cursor
    $sel   = $doc.Application.Selection
    $range = $sel.Range.Duplicate
    $range.Collapse(0)   # wdCollapseEnd

    $tbl = $doc.Tables.Add($range, $rows, $cols)

    # Tabla ancho completo (como Miku Inn: PreferredWidthType=2=wdPreferredWidthPercent, 100%)
    $tbl.PreferredWidthType = 2
    $tbl.PreferredWidth     = 100

    # Bordes
    $tbl.Borders.OutsideLineStyle = 1
    $tbl.Borders.OutsideColor     = $CL_TBL_BORDER
    $tbl.Borders.InsideLineStyle  = 1
    $tbl.Borders.InsideColor      = $CL_TBL_BORDER

    # Relleno de celdas
    $rowIdx = 1
    foreach ($tLine in $dataRows) {
        $cells  = ($tLine -split '\|') | Where-Object { $_.Trim() -ne '' }
        $colIdx = 1
        foreach ($cellVal in $cells) {
            if ($colIdx -gt $cols) { break }
            $txt = ($cellVal.Trim()) -replace '\*\*([^*]+)\*\*', '$1'
            $c   = $tbl.Cell($rowIdx, $colIdx)
            $c.Range.Text = $txt
            $c.Range.Font.Name = "Calibri"
            $c.Range.Font.Size = 10

            if ($rowIdx -eq 1) {
                # Header: fondo cafe dorado, texto blanco, negrita
                $c.Shading.BackgroundPatternColor = $CL_TH_BG
                $c.Range.Font.Color = $CL_TH_FG
                $c.Range.Font.Bold  = $true
            } else {
                # Filas alternas: par=blanco, impar=beige (igual que Miku Inn)
                $bg = if ($rowIdx % 2 -eq 0) { $CL_TR_WHITE } else { $CL_TR_BEIGE }
                $c.Shading.BackgroundPatternColor = $bg
                $c.Range.Font.Color = 0x000000
                $c.Range.Font.Bold  = $false
            }
            $colIdx++
        }
        $rowIdx++
    }

    # Mover cursor despues de la tabla
    $endR = $tbl.Range.Duplicate
    $endR.Collapse(0)
    $endR.Select()
    $doc.Application.Selection.TypeParagraph()
}

# ── Procesamiento principal ───────────────────────────────────────────────────
try {
    $doc = $word.Documents.Add()
    $sel = $word.Selection

    # Margenes identicos a Miku Inn
    $doc.PageSetup.TopMargin    = 72
    $doc.PageSetup.BottomMargin = 72
    $doc.PageSetup.LeftMargin   = 90
    $doc.PageSetup.RightMargin  = 90

    Reset-Format $sel

    # Leer Markdown
    $mdContent = Get-Content $mdPath -Raw -Encoding UTF8
    $lines     = $mdContent -split "`n"

    $inCodeBlock = $false
    $codeLines   = @()
    $inTable     = $false
    $tableLines  = @()

    foreach ($rawLine in $lines) {
        $line = $rawLine.TrimEnd()

        # ── Cierre bloque codigo ─────────────────────────────────────────
        if ($inCodeBlock -and $line -match '^```') {
            $inCodeBlock = $false
            foreach ($cl in $codeLines) {
                $sel.Font.Name  = "Consolas"
                $sel.Font.Size  = 9
                $sel.Font.Bold  = $false
                $sel.Font.Color = $CL_CODE_FG
                $sel.Shading.BackgroundPatternColor = $CL_CODE_BG
                $sel.ParagraphFormat.LeftIndent  = 36
                $sel.ParagraphFormat.SpaceAfter  = 0
                $sel.ParagraphFormat.SpaceBefore = 0
                $sel.TypeText($cl)
                $sel.TypeParagraph()
            }
            Reset-Format $sel
            $codeLines = @()
            continue
        }

        # ── Apertura bloque codigo ───────────────────────────────────────
        if (-not $inCodeBlock -and $line -match '^```') {
            $inCodeBlock = $true
            $codeLines   = @()
            continue
        }

        # ── Dentro de bloque codigo ──────────────────────────────────────
        if ($inCodeBlock) {
            $codeLines += $line
            continue
        }

        # ── Cierre tabla (linea vacia o linea no-tabla) ──────────────────
        if ($inTable -and ($line -eq '' -or ($line -notmatch '^\|'))) {
            Render-Table $doc $tableLines
            $sel        = $word.Selection
            $tableLines = @()
            $inTable    = $false
            # seguir procesando la linea actual
        }

        # ── Fila de tabla ────────────────────────────────────────────────
        if ($line -match '^\|') {
            $inTable     = $true
            $tableLines += $line
            continue
        }

        # ── Linea vacia ──────────────────────────────────────────────────
        if ($line -eq '') {
            Reset-Format $sel
            $sel.TypeParagraph()
            continue
        }

        # ── Separador horizontal --- ─────────────────────────────────────
        if ($line -match '^---+$') {
            Write-Separator $sel
            continue
        }

        # ── H1 ───────────────────────────────────────────────────────────
        if ($line -match '^# (.+)$') {
            Write-H1 $sel $matches[1]
            continue
        }

        # ── H2 ───────────────────────────────────────────────────────────
        if ($line -match '^## (.+)$') {
            Write-H2 $sel $matches[1]
            continue
        }

        # ── H3 ───────────────────────────────────────────────────────────
        if ($line -match '^### (.+)$') {
            Write-H3 $sel $matches[1]
            continue
        }

        # ── H4 ───────────────────────────────────────────────────────────
        if ($line -match '^#### (.+)$') {
            Write-H4 $sel $matches[1]
            continue
        }

        # ── Texto normal ─────────────────────────────────────────────────
        Reset-Format $sel
        Write-InlineText $sel $line
        $sel.TypeParagraph()
    }

    # ── Tabla pendiente al final ─────────────────────────────────────────
    if ($inTable -and $tableLines.Count -gt 0) {
        Render-Table $doc $tableLines
    }

    # ── Guardar ──────────────────────────────────────────────────────────
    $doc.SaveAs([ref]$wordPath)
    Write-Host ""
    Write-Host "Word generado correctamente:" -ForegroundColor Green
    Write-Host "  $wordPath" -ForegroundColor White

} catch {
    Write-Host ""
    Write-Host "Error: $_" -ForegroundColor Red
    Write-Host $_.ScriptStackTrace -ForegroundColor DarkRed
} finally {
    if ($null -ne $doc) {
        try { $doc.Close([ref]$false) } catch {}
    }
    $word.Quit()
    [System.Runtime.InteropServices.Marshal]::ReleaseComObject($word) | Out-Null
    [System.GC]::Collect()
    [System.GC]::WaitForPendingFinalizers()
}

Write-Host ""
Write-Host "----------------------------------------" -ForegroundColor DarkYellow
Write-Host " BROOM AIRLINE - Informe de Tests"        -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor DarkYellow
Write-Host '  Diseno  : Miku Inn (estructura + espaciado)'
Write-Host '  Colores : Broom AirLine (cafe/naranja)'
Write-Host '  Tests   : 116 (74 backend + 42 frontend)'
Write-Host '  Estado  : 116/116 CORRECTO (100%)'
Write-Host "----------------------------------------" -ForegroundColor DarkYellow
