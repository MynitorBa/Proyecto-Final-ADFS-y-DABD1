$out = "C:\Users\memit\AppData\Local\Temp\claude\C--Windows-System32\f9516fff-39fa-45b6-b8e8-63d39f10aba2\tasks\bs2z1b41s.output"
$seen = 0
for ($i = 0; $i -lt 120; $i++) {
    Start-Sleep -Seconds 5
    if (Test-Path $out) {
        $lines = Get-Content $out -ErrorAction SilentlyContinue
        if ($lines -and $lines.Count -gt $seen) {
            $lines[$seen..($lines.Count - 1)] | ForEach-Object { Write-Output $_ }
            $seen = $lines.Count
            foreach ($line in $lines) {
                if ($line -match "EXIT_TESTS:|Tests.*passed|Tests.*failed|npm error") {
                    exit 0
                }
            }
        }
    }
}
Write-Output "POLL_TIMEOUT"
