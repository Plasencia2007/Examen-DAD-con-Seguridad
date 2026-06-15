# stop-all.ps1
# Detiene los microservicios liberando los puertos que estan en escucha.

$Ports = @(8761, 8888, 8081, 8082, 8080)

Write-Host "=== Deteniendo microservicios ===" -ForegroundColor Yellow

foreach ($p in $Ports) {
    try {
        $connections = Get-NetTCPConnection -LocalPort $p -State Listen -ErrorAction Stop

        $pids = $connections | Select-Object -ExpandProperty OwningProcess -Unique

        if (-not $pids) {
            Write-Host "Puerto $p libre. Continuando..." -ForegroundColor DarkGray
            continue
        }

        foreach ($procId in $pids) {
            try {
                Stop-Process -Id $procId -Force -ErrorAction Stop
                Write-Host "Puerto $p : proceso PID $procId detenido." -ForegroundColor Green
            }
            catch {
                Write-Host "Puerto $p : no se pudo detener el PID $procId. $($_.Exception.Message)" -ForegroundColor Red
            }
        }
    }
    catch {
        Write-Host "Puerto $p libre (sin conexiones en escucha). Continuando..." -ForegroundColor DarkGray
    }
}

Write-Host "=== Proceso de detencion finalizado ===" -ForegroundColor Yellow
