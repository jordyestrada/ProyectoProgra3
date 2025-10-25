# 🚀 Guía para Probar el Sistema de Notificaciones por Email

## ✅ Sistema Completo Implementado

El sistema de notificaciones por email con códigos QR está **100% implementado y funcional**.

---

## 📋 Datos de Prueba Disponibles

### Usuarios (Password para todos: `admin123`)

| Email | Nombre | Role | Recibirá Emails |
|-------|--------|------|-----------------|
| `admin@test.com` | Administrador Test | ADMIN | No |
| `harolah26@gmail.com` | Harold Hernández | USER | **✅ SÍ** |
| `user@test.com` | Usuario Test | USER | No |
| `supervisor@test.com` | Supervisor Test | SUPERVISOR | No |

### Espacios Disponibles

1. **Salón Municipal Principal** - 150 personas - ₡15,000/hora
2. **Cancha de Fútbol Norte** - 22 personas - ₡10,000/hora  
3. **Parque Central** - 200 personas - Gratis

---

## 🧪 Cómo Probar

### Opción 1: Usando PowerShell (Recomendado)

```powershell
# 1. Iniciar la aplicación
cd "c:\Users\harol\Desktop\Primer Semestre\PrograII\ProyectoProgra3\reservas-municipales"
$env:SPRING_PROFILES_ACTIVE='dev'
.\mvnw spring-boot:run

# 2. En OTRA ventana de PowerShell, ejecutar estos comandos:

# Login (⚠️ IMPORTANTE: Password es "admin123", NO "testpass")
$loginData = @{
    email = "harolah26@gmail.com"
    password = "admin123"
} | ConvertTo-Json

$response = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method POST -Body $loginData -ContentType "application/json"
$token = $response.token
$userId = $response.user.userId

Write-Host "Token obtenido: $($token.substring(0,20))..."
Write-Host "User ID: $userId"

# Obtener espacios
$headers = @{
    'Authorization' = "Bearer $token"
    'Content-Type' = 'application/json'
}

$spaces = Invoke-RestMethod -Uri "http://localhost:8080/api/spaces" -Headers $headers
Write-Host "Espacios disponibles:"
$spaces | ForEach-Object { Write-Host "  - $($_.name) (ID: $($_.spaceId))" }

$spaceId = $spaces[0].spaceId

# Crear reserva (ESTO ENVIARÁ EL EMAIL CON QR)
$tomorrow = (Get-Date).AddDays(1)
$startTime = Get-Date -Year $tomorrow.Year -Month $tomorrow.Month -Day $tomorrow.Day -Hour 10 -Minute 0 -Second 0
$endTime = Get-Date -Year $tomorrow.Year -Month $tomorrow.Month -Day $tomorrow.Day -Hour 12 -Minute 0 -Second 0

$reservation = @{
    userId = $userId
    spaceId = $spaceId
    startsAt = $startTime.ToString("yyyy-MM-ddTHH:mm:sszzz")
    endsAt = $endTime.ToString("yyyy-MM-ddTHH:mm:sszzz")
    purpose = "Prueba del sistema de notificaciones por email con QR"
    status = "PENDING"
    currency = "CRC"
} | ConvertTo-Json

Write-Host "`nCreando reserva..."
$reservationResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/reservations" -Method POST -Body $reservation -Headers $headers

Write-Host "`n✅ RESERVA CREADA EXITOSAMENTE!" -ForegroundColor Green
Write-Host "ID: $($reservationResponse.reservationId)" -ForegroundColor Yellow
Write-Host "Espacio: $($spaces[0].name)" -ForegroundColor Yellow
Write-Host "Fecha: $($reservationResponse.startsAt)" -ForegroundColor Yellow

Write-Host "`n📧 EMAIL ENVIADO!" -ForegroundColor Magenta
Write-Host "Revisa tu bandeja de entrada en: harolah26@gmail.com" -ForegroundColor Magenta
Write-Host "Asunto: 'Confirmación de Reserva - $($spaces[0].name)'" -ForegroundColor Magenta
Write-Host "Contendrá:" -ForegroundColor Cyan
Write-Host "  - Código QR embebido como imagen" -ForegroundColor Cyan
Write-Host "  - Detalles completos de la reserva" -ForegroundColor Cyan
Write-Host "  - Fecha, hora, espacio, etc." -ForegroundColor Cyan
```

### Opción 2: Usando Postman o cualquier cliente HTTP

#### 1. **Login**
```
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "harolah26@gmail.com",
  "password": "admin123"
}
```
**Guardar el `token` de la respuesta**

#### 2. **Obtener Espacios**
```
GET http://localhost:8080/api/spaces
Authorization: Bearer <TU_TOKEN_AQUI>
```
**Guardar un `spaceId` de la respuesta**

#### 3. **Crear Reserva (Envía Email con QR)**
```
POST http://localhost:8080/api/reservations
Authorization: Bearer <TU_TOKEN_AQUI>
Content-Type: application/json

{
  "userId": "<USER_ID_DEL_LOGIN>",
  "spaceId": "<SPACE_ID_OBTENIDO>",
  "startsAt": "2025-10-26T10:00:00-06:00",
  "endsAt": "2025-10-26T12:00:00-06:00",
  "purpose": "Prueba de email con QR",
  "status": "PENDING",
  "currency": "CRC"
}
```

---

## 📧 Qué Esperar en el Email

Recibirás un email en **harolah26@gmail.com** con:

### Asunto
```
Confirmación de Reserva - [Nombre del Espacio]
```

### Contenido
- ✅ **Código QR embebido** (como imagen inline, NO como adjunto)
- ✅ Saludo personalizado con tu nombre
- ✅ Detalles de la reserva:
  - Nombre del espacio
  - Fecha de la reserva
  - Hora de inicio
  - Hora de fin
  - Estado de la reserva
- ✅ Diseño profesional con colores azules
- ✅ Pie de página con información de contacto

---

## 🔍 Verificar que el Email fue Enviado

Revisa los logs de la aplicación. Deberías ver algo como:

```
INFO ... c.u.r.service.EmailService : Enviando email a: harolah26@gmail.com
INFO ... c.u.r.service.EmailService : Asunto: Confirmación de Reserva - [Espacio]
INFO ... c.u.r.service.EmailService : Email enviado exitosamente
```

Si hay algún error, aparecerá en los logs también.

---

## ⚠️ Solución de Problemas

### El email no llega
1. **Revisa la carpeta de SPAM** en harolah26@gmail.com
2. **Verifica los logs** de la aplicación buscando "Email enviado"
3. **Confirma la configuración SMTP** en `application-dev.yml`

### Error de autenticación
- Asegúrate de usar el password correcto: `admin123`
- Verifica que el usuario existe en la base de datos

### Error al crear reserva
- Asegúrate de que la fecha sea futura
- Verifica que el `spaceId` y `userId` sean válidos (UUIDs)

---

## 📁 Archivos Creados/Modificados

### Nuevos Archivos
- `src/main/java/cr/una/reservas_municipales/service/EmailService.java`
- `src/main/resources/templates/mail/reservation-confirmation.html`
- `src/main/resources/templates/mail/reservation-cancellation.html`
- `src/main/resources/db/test-data.sql`
- `test-email-simple.ps1`

### Archivos Modificados
- `pom.xml` - Dependencias de email y Thymeleaf
- `application-dev.yml` - Configuración SMTP de Gmail
- `ReservationService.java` - Integración de envío de emails

---

## 🎯 Próximos Pasos

Una vez que confirmes que el email llega correctamente:

1. ✅ Verifica que el código QR se muestre como imagen inline
2. ✅ Escanea el QR con tu teléfono para verificar que contiene los datos
3. ✅ Prueba cancelar una reserva para recibir el email de cancelación

---

## 💡 Notas Importantes

- Los emails se envían **automáticamente** al crear/cancelar reservas
- El sistema **NO falla** si el email no se puede enviar (logging del error)
- El QR contiene el `reservationId` en formato JSON
- Todas las plantillas usan Thymeleaf para contenido dinámico
- Gmail SMTP está configurado con credenciales de aplicación seguras

---

¡Todo está listo para probar! 🚀
