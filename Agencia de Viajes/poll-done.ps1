$signal = "C:\Proyecto-Final-ADFS-y-DABD1\Agencia de Viajes\install-done.txt"
for ($i = 0; $i -lt 100; $i++) {
    if (Test-Path $signal) {
        $content = Get-Content $signal
        Write-Output $content
        exit 0
    }
    Start-Sleep -Seconds 3
}
Write-Output "TIMEOUT"
exit 1
