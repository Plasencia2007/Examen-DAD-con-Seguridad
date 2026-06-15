@echo off
setlocal

echo === Arranque de microservicios ===

REM 1. Registry Server (Eureka, 8761) - esperar 35s
echo Iniciando ms-lib-registry-server (Eureka, 8761) ...
start "ms-lib-registry-server (8761)" cmd /k "cd /d "%~dp0ms-lib-registry-server" && mvnw spring-boot:run"
timeout /t 35 /nobreak

REM 2. Config Server (Config, 8888) - esperar 25s
echo Iniciando ms-lib-config-server (Config, 8888) ...
start "ms-lib-config-server (8888)" cmd /k "cd /d "%~dp0ms-lib-config-server" && mvnw spring-boot:run"
timeout /t 25 /nobreak

REM 3. Seguridad (8081) - esperar 30s
echo Iniciando ms-seguridad (8081) ...
start "ms-seguridad (8081)" cmd /k "cd /d "%~dp0ms-seguridad" && mvnw spring-boot:run"
timeout /t 30 /nobreak

REM 4. Reserva (8082) - esperar 25s
echo Iniciando ms-reserva (8082) ...
start "ms-reserva (8082)" cmd /k "cd /d "%~dp0ms-reserva" && mvnw spring-boot:run"
timeout /t 25 /nobreak

REM 5. API Gateway (8080)
echo Iniciando ms-lib-api-gateway (8080) ...
start "ms-lib-api-gateway (8080)" cmd /k "cd /d "%~dp0ms-lib-api-gateway" && mvnw spring-boot:run"

echo.
echo === Todos los servicios fueron lanzados ===
echo Eureka:  http://localhost:8761
echo Gateway: http://localhost:8080
echo Usuario inicial: admin / admin123

endlocal
