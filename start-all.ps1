# start-all.ps1
# Arranque de microservicios en el orden obligatorio.
# Cada modulo se lanza en una NUEVA ventana de PowerShell con su wrapper Maven.

$Root = $PSScriptRoot

function Start-Module {
    param(
        [string]$Name,
        [int]$Port,
        [ConsoleColor]$Color = 'Cyan'
    )

    $ModulePath = Join-Path -Path $Root -ChildPath $Name

    Write-Host "Iniciando servicio '$Name' en el puerto $Port ..." -ForegroundColor $Color

    Start-Process powershell -ArgumentList '-NoExit', '-NoProfile', '-Command', "Set-Location -LiteralPath '$ModulePath'; .\mvnw spring-boot:run"
}

Write-Host "=== Arranque de microservicios ===" -ForegroundColor Yellow

# 1. Registry Server (Eureka, 8761) - esperar 35s
Start-Module -Name 'ms-lib-registry-server' -Port 8761 -Color 'Green'
Start-Sleep -Seconds 35

# 2. Config Server (Config, 8888) - esperar 25s
Start-Module -Name 'ms-lib-config-server' -Port 8888 -Color 'Green'
Start-Sleep -Seconds 25

# 3. Seguridad (8081) - esperar 30s
Start-Module -Name 'ms-seguridad' -Port 8081 -Color 'Cyan'
Start-Sleep -Seconds 30

# 4. Reserva (8082) - esperar 25s
Start-Module -Name 'ms-reserva' -Port 8082 -Color 'Cyan'
Start-Sleep -Seconds 25

# 5. API Gateway (8080)
Start-Module -Name 'ms-lib-api-gateway' -Port 8080 -Color 'Magenta'

Write-Host ""
Write-Host "=== Todos los servicios fueron lanzados ===" -ForegroundColor Yellow
Write-Host "Eureka:  http://localhost:8761" -ForegroundColor Green
Write-Host "Gateway: http://localhost:8080" -ForegroundColor Magenta
Write-Host "Usuario inicial: admin / admin123" -ForegroundColor White
