# Script para probar la autenticación JWT + Azure AD
Write-Host "=== Probando API de Reservas Municipales ===" -ForegroundColor Green

# Esperar a que la aplicación esté lista
Write-Host "Esperando a que la aplicación inicie..." -ForegroundColor Yellow
Start-Sleep -Seconds 5

# 1. Probar endpoint de ping
Write-Host "`n1. Probando endpoint de ping..." -ForegroundColor Cyan
try {
    $pingResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/ping" -Method GET
    Write-Host "✅ Ping successful: $pingResponse" -ForegroundColor Green
} catch {
    Write-Host "❌ Error en ping: $($_.Exception.Message)" -ForegroundColor Red
}

# 2. Probar login con usuario de prueba
Write-Host "`n2. Probando login con usuario de prueba..." -ForegroundColor Cyan
$loginData = @{
    email = "admin@test.com"
    password = "admin123"
} | ConvertTo-Json

$headers = @{
    'Content-Type' = 'application/json'
}

try {
    $loginResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method POST -Body $loginData -Headers $headers
    Write-Host "✅ Login successful!" -ForegroundColor Green
    Write-Host "Token: $($loginResponse.token)" -ForegroundColor Yellow
    Write-Host "User: $($loginResponse.user.email)" -ForegroundColor Yellow
    Write-Host "Role: $($loginResponse.user.role)" -ForegroundColor Yellow
    
    # Guardar token para siguientes pruebas
    $global:authToken = $loginResponse.token
    
} catch {
    Write-Host "❌ Error en login: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "Response body: $responseBody" -ForegroundColor Red
    }
}

# 3. Probar endpoint protegido con JWT
if ($global:authToken) {
    Write-Host "`n3. Probando endpoint protegido con JWT..." -ForegroundColor Cyan
    $authHeaders = @{
        'Authorization' = "Bearer $($global:authToken)"
        'Content-Type' = 'application/json'
    }
    
    try {
        $userResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/user" -Method GET -Headers $authHeaders
        Write-Host "✅ Usuario autenticado obtenido!" -ForegroundColor Green
        Write-Host "Email: $($userResponse.email)" -ForegroundColor Yellow
        Write-Host "Role: $($userResponse.role)" -ForegroundColor Yellow
    } catch {
        Write-Host "❌ Error al obtener usuario: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# 4. Probar validación de token
if ($global:authToken) {
    Write-Host "`n4. Probando validación de token..." -ForegroundColor Cyan
    try {
        $validateResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/validate" -Method POST -Headers $authHeaders
        Write-Host "✅ Token válido!" -ForegroundColor Green
        Write-Host "Valid: $($validateResponse.valid)" -ForegroundColor Yellow
    } catch {
        Write-Host "❌ Error en validación: $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host "`n=== Pruebas completadas ===" -ForegroundColor Green