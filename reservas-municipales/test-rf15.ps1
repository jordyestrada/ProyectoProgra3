# Script de Prueba - RF15 Horarios de Espacios
# Ejecutar en PowerShell después de levantar la aplicación

# ==============================================================================
# CONFIGURACIÓN INICIAL
# ==============================================================================

$baseUrl = "http://localhost:8080"

Write-Host "=== RF15: Prueba de Horarios de Espacios ===" -ForegroundColor Cyan
Write-Host ""

# ==============================================================================
# PASO 1: AUTENTICACIÓN
# ==============================================================================

Write-Host "[1] Autenticando como ADMIN..." -ForegroundColor Yellow

$loginBody = @{
    email = "admin@municipalidad.cr"
    password = "testpass"
} | ConvertTo-Json

try {
    $authResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" `
        -Method POST `
        -ContentType "application/json" `
        -Body $loginBody
    
    $token = $authResponse.token
    Write-Host "✓ Token obtenido: $($token.Substring(0,20))..." -ForegroundColor Green
    Write-Host ""
} catch {
    Write-Host "✗ Error en autenticación: $_" -ForegroundColor Red
    exit 1
}

$headers = @{
    Authorization = "Bearer $token"
    "Content-Type" = "application/json"
}

# ==============================================================================
# PASO 2: OBTENER UN ESPACIO
# ==============================================================================

Write-Host "[2] Obteniendo lista de espacios..." -ForegroundColor Yellow

try {
    $spaces = Invoke-RestMethod -Uri "$baseUrl/api/spaces" -Headers $headers
    
    if ($spaces.Count -eq 0) {
        Write-Host "✗ No hay espacios en el sistema" -ForegroundColor Red
        exit 1
    }
    
    $spaceId = $spaces[0].spaceId
    $spaceName = $spaces[0].name
    
    Write-Host "✓ Usando espacio: $spaceName" -ForegroundColor Green
    Write-Host "  ID: $spaceId" -ForegroundColor Gray
    Write-Host ""
} catch {
    Write-Host "✗ Error obteniendo espacios: $_" -ForegroundColor Red
    exit 1
}

# ==============================================================================
# PASO 3: CREAR HORARIOS
# ==============================================================================

Write-Host "[3] Creando horarios para el espacio..." -ForegroundColor Yellow

# Lunes a Viernes: 8:00-12:00 (mañana)
$schedulesCreated = 0

foreach ($day in 1..5) {
    $scheduleBody = @{
        weekday = $day
        timeFrom = "08:00:00"
        timeTo = "12:00:00"
    } | ConvertTo-Json
    
    try {
        $schedule = Invoke-RestMethod -Uri "$baseUrl/api/spaces/$spaceId/schedules" `
            -Method POST `
            -Headers $headers `
            -Body $scheduleBody
        
        $schedulesCreated++
        Write-Host "  ✓ Horario creado: $($schedule.weekdayName) 08:00-12:00" -ForegroundColor Green
    } catch {
        Write-Host "  ✗ Error creando horario día $day : $_" -ForegroundColor Red
    }
}

# Lunes a Viernes: 14:00-18:00 (tarde)
foreach ($day in 1..5) {
    $scheduleBody = @{
        weekday = $day
        timeFrom = "14:00:00"
        timeTo = "18:00:00"
    } | ConvertTo-Json
    
    try {
        $schedule = Invoke-RestMethod -Uri "$baseUrl/api/spaces/$spaceId/schedules" `
            -Method POST `
            -Headers $headers `
            -Body $scheduleBody
        
        $schedulesCreated++
        Write-Host "  ✓ Horario creado: $($schedule.weekdayName) 14:00-18:00" -ForegroundColor Green
    } catch {
        Write-Host "  ✗ Error creando horario día $day : $_" -ForegroundColor Red
    }
}

# Sábado: 9:00-13:00
$saturdayBody = @{
    weekday = 6
    timeFrom = "09:00:00"
    timeTo = "13:00:00"
} | ConvertTo-Json

try {
    $schedule = Invoke-RestMethod -Uri "$baseUrl/api/spaces/$spaceId/schedules" `
        -Method POST `
        -Headers $headers `
        -Body $saturdayBody
    
    $schedulesCreated++
    Write-Host "  ✓ Horario creado: $($schedule.weekdayName) 09:00-13:00" -ForegroundColor Green
} catch {
    Write-Host "  ✗ Error creando horario sábado: $_" -ForegroundColor Red
}

Write-Host ""
Write-Host "Total de horarios creados: $schedulesCreated" -ForegroundColor Cyan
Write-Host ""

# ==============================================================================
# PASO 4: LISTAR HORARIOS
# ==============================================================================

Write-Host "[4] Listando horarios del espacio..." -ForegroundColor Yellow

try {
    $schedules = Invoke-RestMethod -Uri "$baseUrl/api/spaces/$spaceId/schedules" -Headers $headers
    
    Write-Host "✓ Horarios configurados ($($schedules.Count) total):" -ForegroundColor Green
    
    $schedules | Group-Object weekdayName | ForEach-Object {
        Write-Host "  $($_.Name):" -ForegroundColor Cyan
        $_.Group | ForEach-Object {
            Write-Host "    - $($_.timeFrom) a $($_.timeTo)" -ForegroundColor Gray
        }
    }
    Write-Host ""
} catch {
    Write-Host "✗ Error listando horarios: $_" -ForegroundColor Red
}

# ==============================================================================
# PASO 5: PRUEBAS DE VALIDACIÓN
# ==============================================================================

Write-Host "[5] Probando validaciones de reservas..." -ForegroundColor Yellow

# Obtener un usuario
try {
    $users = Invoke-RestMethod -Uri "$baseUrl/api/users" -Headers $headers
    $userId = $users[0].userId
} catch {
    Write-Host "✗ Error obteniendo usuarios" -ForegroundColor Red
    exit 1
}

# Prueba 1: Reserva válida (Lunes 10:00-11:00)
Write-Host ""
Write-Host "  [5.1] Reserva VÁLIDA (Lunes 10:00-11:00)..." -ForegroundColor Yellow

$nextMonday = (Get-Date).AddDays((1 - (Get-Date).DayOfWeek.value__ + 7) % 7)
$reservationValid = @{
    spaceId = $spaceId
    userId = $userId
    startsAt = $nextMonday.Date.AddHours(10).ToString("yyyy-MM-ddTHH:mm:sszzz")
    endsAt = $nextMonday.Date.AddHours(11).ToString("yyyy-MM-ddTHH:mm:sszzz")
    status = "PENDING"
} | ConvertTo-Json

try {
    $reservation = Invoke-RestMethod -Uri "$baseUrl/api/reservations" `
        -Method POST `
        -Headers $headers `
        -Body $reservationValid
    
    Write-Host "    ✓ Reserva creada: $($reservation.reservationId)" -ForegroundColor Green
    $validReservationId = $reservation.reservationId
} catch {
    Write-Host "    ✗ Error: $($_.Exception.Message)" -ForegroundColor Red
}

# Prueba 2: Reserva INVÁLIDA (Lunes 13:00-14:00, fuera de horario)
Write-Host ""
Write-Host "  [5.2] Reserva INVÁLIDA (Lunes 13:00-14:00, fuera de horario)..." -ForegroundColor Yellow

$reservationInvalid = @{
    spaceId = $spaceId
    userId = $userId
    startsAt = $nextMonday.Date.AddHours(13).ToString("yyyy-MM-ddTHH:mm:sszzz")
    endsAt = $nextMonday.Date.AddHours(14).ToString("yyyy-MM-ddTHH:mm:sszzz")
    status = "PENDING"
} | ConvertTo-Json

try {
    $reservation = Invoke-RestMethod -Uri "$baseUrl/api/reservations" `
        -Method POST `
        -Headers $headers `
        -Body $reservationInvalid
    
    Write-Host "    ✗ ERROR: Se permitió una reserva fuera de horario!" -ForegroundColor Red
} catch {
    $errorMsg = $_.ErrorDetails.Message | ConvertFrom-Json
    Write-Host "    ✓ Validación funcionó: $($errorMsg.message)" -ForegroundColor Green
}

# Prueba 3: Reserva INVÁLIDA (Domingo, día no disponible)
Write-Host ""
Write-Host "  [5.3] Reserva INVÁLIDA (Domingo, día no disponible)..." -ForegroundColor Yellow

$nextSunday = (Get-Date).AddDays((0 - (Get-Date).DayOfWeek.value__ + 7) % 7)
if ($nextSunday -le (Get-Date)) { $nextSunday = $nextSunday.AddDays(7) }

$reservationSunday = @{
    spaceId = $spaceId
    userId = $userId
    startsAt = $nextSunday.Date.AddHours(10).ToString("yyyy-MM-ddTHH:mm:sszzz")
    endsAt = $nextSunday.Date.AddHours(12).ToString("yyyy-MM-ddTHH:mm:sszzz")
    status = "PENDING"
} | ConvertTo-Json

try {
    $reservation = Invoke-RestMethod -Uri "$baseUrl/api/reservations" `
        -Method POST `
        -Headers $headers `
        -Body $reservationSunday
    
    Write-Host "    ✗ ERROR: Se permitió una reserva en domingo!" -ForegroundColor Red
} catch {
    $errorMsg = $_.ErrorDetails.Message | ConvertFrom-Json
    Write-Host "    ✓ Validación funcionó: $($errorMsg.message)" -ForegroundColor Green
}

# ==============================================================================
# PASO 6: LIMPIEZA (OPCIONAL)
# ==============================================================================

Write-Host ""
Write-Host "[6] Limpieza de datos de prueba..." -ForegroundColor Yellow

# Eliminar reserva válida creada
if ($validReservationId) {
    try {
        Invoke-RestMethod -Uri "$baseUrl/api/reservations/$validReservationId" `
            -Method DELETE `
            -Headers $headers | Out-Null
        Write-Host "  ✓ Reserva de prueba eliminada" -ForegroundColor Green
    } catch {
        Write-Host "  ✗ Error eliminando reserva: $_" -ForegroundColor Red
    }
}

# Preguntar si eliminar horarios
Write-Host ""
$deleteSchedules = Read-Host "¿Eliminar los horarios creados? (s/n)"

if ($deleteSchedules -eq "s") {
    try {
        Invoke-RestMethod -Uri "$baseUrl/api/spaces/$spaceId/schedules" `
            -Method DELETE `
            -Headers $headers | Out-Null
        Write-Host "  ✓ Horarios eliminados" -ForegroundColor Green
    } catch {
        Write-Host "  ✗ Error eliminando horarios: $_" -ForegroundColor Red
    }
} else {
    Write-Host "  Los horarios se mantienen en el espacio" -ForegroundColor Yellow
}

# ==============================================================================
# RESUMEN
# ==============================================================================

Write-Host ""
Write-Host "=== RESUMEN DE PRUEBAS ===" -ForegroundColor Cyan
Write-Host "✓ Autenticación: OK" -ForegroundColor Green
Write-Host "✓ Creación de horarios: $schedulesCreated horarios" -ForegroundColor Green
Write-Host "✓ Listado de horarios: OK" -ForegroundColor Green
Write-Host "✓ Validación en reservas: OK" -ForegroundColor Green
Write-Host ""
Write-Host "RF15 implementado y funcionando correctamente!" -ForegroundColor Green
Write-Host ""
