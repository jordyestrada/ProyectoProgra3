# Script para iniciar la aplicación con configuración de Gmail
# Uso: .\start-with-gmail.ps1

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Iniciando Reservas Municipales" -ForegroundColor Cyan
Write-Host "  con Gmail SMTP configurado" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Configuración de Gmail (ya está en application-dev.yml)
Write-Host "[✓] Gmail SMTP: smtp.gmail.com:587" -ForegroundColor Green
Write-Host "[✓] Usuario: reservas.muni.pz@gmail.com" -ForegroundColor Green
Write-Host "[✓] App Password configurado" -ForegroundColor Green
Write-Host ""

# Activar perfil dev
$env:SPRING_PROFILES_ACTIVE = 'dev'
Write-Host "[✓] Perfil activo: dev" -ForegroundColor Green
Write-Host ""

Write-Host "Iniciando aplicación..." -ForegroundColor Yellow
Write-Host "Presiona Ctrl+C para detener" -ForegroundColor Yellow
Write-Host ""

# Iniciar aplicación
.\mvnw.cmd spring-boot:run
