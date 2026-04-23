# Script generar Word Miku Inn con diseno uniforme (estructura Miku Inn original)

$wordPath = "C:\Proyecto-Final-ADFS-y-DABD1\Cadena de Hoteles\Informe-Pruebas-Unitarias.docx"
$mdPath   = "C:\Proyecto-Final-ADFS-y-DABD1\Cadena de Hoteles\INFORME-COMPLETO-PRUEBAS-UNITARIAS.md"

if (-not (Test-Path $mdPath)) {
    Write-Host "ERROR: No se encontro el archivo Markdown." -ForegroundColor Red
    exit 1
}

# ── Borrar Word anterior ─────────────────────────────────────────────────────
if (Test-Path $wordPath) {
    Remove-Item $wordPath -Force
    Write-Host "Word anterior eliminado." -ForegroundColor Yellow
}

# ── COLORES MIKU INN (formato BGR de Word COM) ────────────────────────────────
# Paleta morado/lila — Cadena de Hoteles Miku Inn
$wdAuto         = -16777216   # wdColorAutomatic — sin sombreado

$CL_H1          = 0x594F69   # Morado oscuro     (RGB #694F59)
$CL_H2          = 0x600080   # Violeta profundo  (RGB #800060)
$CL_H3          = 0xA884A9   # Morado claro      (RGB #A984A8)
$CL_H4          = 0xC8A0C0   # Morado muy claro  (RGB #C0A0C8)
$CL_SEPARATOR   = 0xA884A9   # Igual que H3

$CL_TH_BG       = 0x594F69   # Header tabla bg   (RGB #694F59)
$CL_TH_FG       = 0xFFFFFF   # Texto tabla header (blanco)
$CL_TR_WHITE    = 0xFFFFFF   # Fila blanca
$CL_TR_LILA     = 0xF0E8F0   # Fila lila claro   (RGB #F0E8F0)
$CL_TBL_BORDER  = 0x600080   # Borde tabla       (RGB #800060)

$CL_CODE_BG     = 0xF5F0F8   # Fondo bloque codigo (lila muy claro)
$CL_CODE_FG     = 0x594F69   # Texto codigo       (morado oscuro)

Write-Host "Generando Word Miku Inn con diseno uniforme + colores morados..." -ForegroundColor Cyan

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
    $dataRows = @($tableLines | Where-Object { $_ -notmatch '^\|[\s\-\|:]+\|$' })
    $rows     = $dataRows.Count
    if ($rows -lt 1) { return }

    $parts = ($dataRows[0] -split '\|') | Where-Object { $_.Trim() -ne '' }
    $cols  = $parts.Count
    if ($cols -lt 1) { return }

    $sel   = $doc.Application.Selection
    $range = $sel.Range.Duplicate
    $range.Collapse(0)

    $tbl = $doc.Tables.Add($range, $rows, $cols)

    $tbl.PreferredWidthType = 2
    $tbl.PreferredWidth     = 100

    $tbl.Borders.OutsideLineStyle = 1
    $tbl.Borders.OutsideColor     = $CL_TBL_BORDER
    $tbl.Borders.InsideLineStyle  = 1
    $tbl.Borders.InsideColor      = $CL_TBL_BORDER

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
                $c.Shading.BackgroundPatternColor = $CL_TH_BG
                $c.Range.Font.Color = $CL_TH_FG
                $c.Range.Font.Bold  = $true
            } else {
                $bg = if ($rowIdx % 2 -eq 0) { $CL_TR_WHITE } else { $CL_TR_LILA }
                $c.Shading.BackgroundPatternColor = $bg
                $c.Range.Font.Color = 0x000000
                $c.Range.Font.Bold  = $false
            }
            $colIdx++
        }
        $rowIdx++
    }

    $endR = $tbl.Range.Duplicate
    $endR.Collapse(0)
    $endR.Select()
    $doc.Application.Selection.TypeParagraph()
}

# ── Procesamiento principal ───────────────────────────────────────────────────
try {
    $doc = $word.Documents.Add()
    $sel = $word.Selection

    # Margenes identicos a Miku Inn original
    $doc.PageSetup.TopMargin    = 72
    $doc.PageSetup.BottomMargin = 72
    $doc.PageSetup.LeftMargin   = 90
    $doc.PageSetup.RightMargin  = 90

    Reset-Format $sel

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

        # ── Cierre tabla ─────────────────────────────────────────────────
        if ($inTable -and ($line -eq '' -or ($line -notmatch '^\|'))) {
            Render-Table $doc $tableLines
            $sel        = $word.Selection
            $tableLines = @()
            $inTable    = $false
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
Write-Host "----------------------------------------" -ForegroundColor DarkMagenta
Write-Host " MIKU INN - Informe de Tests"              -ForegroundColor Magenta
Write-Host "----------------------------------------" -ForegroundColor DarkMagenta
Write-Host '  Diseno  : Uniforme (estructura Miku Inn)'
Write-Host '  Colores : Miku Inn (morado/lila)'
Write-Host '  Tests   : 937 (847 backend + 90 frontend)'
Write-Host '  Estado  : 937/937 CORRECTO (100%)'
Write-Host "----------------------------------------" -ForegroundColor DarkMagenta
