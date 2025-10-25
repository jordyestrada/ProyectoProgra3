# 📧 GUÍA DE PRUEBAS - NOTIFICACIONES POR EMAIL
## Sistema de Reservas Municipales - Perfil Docker

---

## ⚙️ CONFIGURACIÓN ACTUAL

### Perfil Activo: `docker`
- **Base de Datos**: PostgreSQL en `localhost:5433`
- **Servidor Email**: MailHog en `localhost:1025`
- **Interfaz Web MailHog**: http://localhost:8025 ⬅️ **ABRE ESTO PRIMERO**
- **API**: http://localhost:8080

### ✅ Ventajas de usar MailHog:
- ✅ Captura TODOS los emails localmente (NO van a Gmail real)
- ✅ No necesita credenciales de Gmail
- ✅ Ver emails en tiempo real en http://localhost:8025
- ✅ Perfecto para testing y desarrollo

---

## 🚀 INICIO RÁPIDO (3 PASOS)

### 1️⃣ Abrir MailHog en el navegador:
```
http://localhost:8025
```
**Dejar esta pestaña abierta** - aquí verás los emails llegando en tiempo real.

### 2️⃣ Asegurarse que la aplicación esté corriendo:
Deberías ver en la terminal:
```
Started ReservasMunicipalesApplication in X seconds
Tomcat started on port 8080 (http)
The following 1 profile is active: "docker"
```

### 3️⃣ Hacer login en Postman:
```json
POST http://localhost:8080/api/auth/login
{
  "email": "harolah26@gmail.com",
  "password": "admin123"
}
```
✅ Si obtienes un token → **¡Listo para probar emails!**

---

## 👥 USUARIOS DE PRUEBA

| Email | Password | Rol | Notas |
|-------|----------|-----|-------|
| `admin@test.com` | `admin123` | ADMIN | Administrador |
| `supervisor@test.com` | `admin123` | SUPERVISOR | Supervisor |
| `user@test.com` | `admin123` | USER | Usuario normal |
| `harolah26@gmail.com` | `admin123` | USER | ⭐ **USAR ESTE** |

**⚠️ IMPORTANTE:** Usa `harolah26@gmail.com` para recibir las notificaciones de prueba.

---

## 🏢 ESPACIOS DISPONIBLES

| UUID | Nombre | Capacidad |
|------|--------|-----------|
| `3ea7914a-7e7c-4c97-b501-4479c678ea46` | Cancha Municipal San José | 22 |
| `e8547b7d-7685-47d5-b6f5-c6bba366b31e` | Parque Central Cartago | 50 |
| `2383a4c8-bf61-4069-a91a-664170ecabab` | Salón Municipal | 100 |
| `e1234567-89ab-cdef-0123-456789abcdef` | ⭐ Salón Municipal Principal | 150 |
| `e2234567-89ab-cdef-0123-456789abcdef` | Cancha de Fútbol Norte | 22 |

---

## 🧪 PRUEBAS EN POSTMAN

### 📍 Paso 1: LOGIN (Obtener Token JWT)

**Request:**
```
POST http://localhost:8080/api/auth/login
Content-Type: application/json
```

**Body:**
```json
{
  "email": "harolah26@gmail.com",
  "password": "admin123"
}
```

**Respuesta Esperada (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJoYXJvbGFoMjZAZ21haWwuY29tIiwicm9sZSI6IlVTRVIiLCJpYXQiOjE3Mjk3OTk5OTksImV4cCI6MTcyOTg4NjM5OX0...",
  "expiresIn": 86400000,
  "userId": "b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a22",
  "email": "harolah26@gmail.com",
  "fullName": "Harold Hernández",
  "role": "USER"
}
```

**⚠️ COPIAR EL TOKEN** - Lo necesitarás para las siguientes peticiones.

---

### 📍 Paso 2: OBTENER ESPACIOS (Opcional)

**Request:**
```
GET http://localhost:8080/api/spaces
Authorization: Bearer [TU_TOKEN_AQUI]
```

**Respuesta:** Lista de espacios disponibles.

---

### 📍 Paso 3: CREAR RESERVA ✉️ (Envía Email con QR)

**Request:**
```
POST http://localhost:8080/api/reservations
Authorization: Bearer [TU_TOKEN_AQUI]
Content-Type: application/json
```

**Body - Opción 1 (Salón Municipal Principal):**
```json
{
  "spaceId": "e1234567-89ab-cdef-0123-456789abcdef",
  "startDateTime": "2025-10-26T10:00:00-06:00",
  "endDateTime": "2025-10-26T12:00:00-06:00",
  "purpose": "Prueba de notificación por email con QR code",
  "expectedAttendees": 50
}
```

**Body - Opción 2 (Cancha de Fútbol):**
```json
{
  "spaceId": "e2234567-89ab-cdef-0123-456789abcdef",
  "startDateTime": "2025-10-27T14:00:00-06:00",
  "endDateTime": "2025-10-27T16:00:00-06:00",
  "purpose": "Entrenamiento de fútbol - Prueba email",
  "expectedAttendees": 22
}
```

**Respuesta Esperada (200 OK):**
```json
{
  "reservationId": "uuid-de-la-reserva",
  "userId": "b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a22",
  "spaceId": "e1234567-89ab-cdef-0123-456789abcdef",
  "startDateTime": "2025-10-26T10:00:00",
  "endDateTime": "2025-10-26T12:00:00",
  "status": "PENDING",
  "qrCode": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUg...",
  "totalCost": 30000.00,
  "currency": "CRC"
}
```

**📧 RESULTADO:** 
1. Se crea la reserva exitosamente
2. **SE ENVÍA EMAIL AUTOMÁTICAMENTE** con:
   - ✅ Asunto: "Confirmación de Reserva - Salón Municipal Principal"
   - ✅ QR Code embebido (inline, NO adjunto)
   - ✅ Detalles completos de la reserva
   - ✅ Destinatario: harolah26@gmail.com

---

### 📍 Paso 4: VERIFICAR EMAIL EN MAILHOG 🎯

**1. Abrir MailHog en navegador:**
```
http://localhost:8025
```

**2. Deberías ver:**
- ✉️ **From:** reservas.muni.pz@gmail.com
- ✉️ **To:** harolah26@gmail.com
- ✉️ **Subject:** Confirmación de Reserva - Salón Municipal Principal

**3. Al abrir el email verás:**
- Diseño profesional con tema azul
- Logo/header
- Detalles de la reserva:
  - Usuario: Harold Hernández
  - Espacio: Salón Municipal Principal
  - Fecha: 26 de octubre de 2025
  - Hora: 10:00 - 12:00
  - Estado: PENDING
- **QR CODE VISIBLE** (imagen embebida)
- Footer con información

---

### 📍 Paso 5: CANCELAR RESERVA ✉️ (Envía Email de Cancelación)

**Request:**
```
POST http://localhost:8080/api/reservations/[RESERVATION_ID]/cancel
Authorization: Bearer [TU_TOKEN_AQUI]
Content-Type: application/json
```

**Body:**
```json
{
  "reason": "Prueba de email de cancelación"
}
```

**Respuesta Esperada (200 OK):**
```json
{
  "reservationId": "uuid-de-la-reserva",
  "status": "CANCELLED",
  "cancellationReason": "Prueba de email de cancelación",
  "cancelledAt": "2025-10-24T15:30:00"
}
```

**📧 RESULTADO:**
1. Se cancela la reserva
2. **SE ENVÍA EMAIL DE CANCELACIÓN** con:
   - ✅ Asunto: "Reserva Cancelada - Salón Municipal Principal"
   - ✅ Tema rojo (alerta)
   - ✅ Razón de cancelación
   - ⚠️ SIN QR code (ya no es necesario)

**Verificar en MailHog:**
```
http://localhost:8025
```

---

## 🔍 LOGS DE VERIFICACIÓN

### En la consola de la aplicación deberías ver:

**Al crear reserva:**
```
DEBUG c.u.r.service.ReservationService: Sending confirmation email for reservation: [uuid]
DEBUG c.u.r.service.EmailService: Sending email to: harolah26@gmail.com
DEBUG c.u.r.service.EmailService: Email sent successfully
```

**Al cancelar reserva:**
```
DEBUG c.u.r.service.ReservationService: Sending cancellation email for reservation: [uuid]
DEBUG c.u.r.service.EmailService: Sending email to: harolah26@gmail.com
DEBUG c.u.r.service.EmailService: Email sent successfully
```

---

## ❌ SOLUCIÓN DE PROBLEMAS

### Problema 1: "Connection refused" al enviar email
**Solución:** Verificar que MailHog esté corriendo
```powershell
docker ps | Select-String mailhog
```
Si no está corriendo:
```powershell
docker-compose up -d
```

### Problema 2: No veo emails en MailHog
**Verificar:**
1. ¿Aplicación corriendo? → `http://localhost:8080/actuator/health`
2. ¿MailHog corriendo? → `http://localhost:8025`
3. ¿Logs muestran "Email sent successfully"?

### Problema 3: "Invalid credentials" al login
**Solución:** Verificar password en base de datos
```powershell
docker exec -i reservas-municipales-postgres-1 psql -U postgres -d reservas -c "SELECT email, LEFT(password_hash, 30) FROM app_user WHERE email = 'harolah26@gmail.com';"
```
Debería mostrar: `$2a$10$N9qo8uLOickgx2ZMRZoMyeI`

Si está mal:
```powershell
docker exec -i reservas-municipales-postgres-1 psql -U postgres -d reservas -c "UPDATE app_user SET password_hash = '\$2a\$10\$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy' WHERE email = 'harolah26@gmail.com';"
```

### Problema 4: QR Code no se muestra en email
**Verificación:**
- ✅ El QR viene en el response de crear reserva
- ✅ EmailService usa `sendEmailWithEmbeddedImage()` para confirmaciones
- ✅ Template usa `<img src="cid:qrCode" />` (inline, no attachment)

---

## 🎯 CHECKLIST DE PRUEBA COMPLETA

- [ ] Aplicación corriendo en `localhost:8080`
- [ ] MailHog corriendo en `localhost:8025`
- [ ] Login exitoso con `harolah26@gmail.com`
- [ ] Token JWT obtenido
- [ ] Reserva creada exitosamente
- [ ] Email de confirmación visible en MailHog
- [ ] QR Code se muestra correctamente en el email
- [ ] Reserva cancelada exitosamente
- [ ] Email de cancelación visible en MailHog
- [ ] Ambos emails tienen diseño profesional

---

## 📊 ESTADOS DE RESERVA

| Estado | Descripción | Email enviado |
|--------|-------------|---------------|
| `PENDING` | Reserva creada, pendiente confirmación | ✉️ Confirmación (con QR) |
| `CONFIRMED` | Reserva confirmada por admin | ✉️ Confirmación (con QR) |
| `CANCELLED` | Reserva cancelada | ✉️ Cancelación (sin QR) |
| `COMPLETED` | Reserva completada | No |

---

## 🌐 URLs IMPORTANTES

| Servicio | URL | Descripción |
|----------|-----|-------------|
| API | http://localhost:8080 | Backend Spring Boot |
| Health | http://localhost:8080/actuator/health | Estado de la app |
| MailHog Web | http://localhost:8025 | Ver emails capturados |
| MailHog SMTP | localhost:1025 | Servidor SMTP |
| PostgreSQL | localhost:5433 | Base de datos |

---

## 📝 NOTAS FINALES

1. **MailHog vs Gmail:**
   - Perfil `docker` → MailHog (localhost:1025)
   - Perfil `dev` → Gmail SMTP (smtp.gmail.com:587)

2. **Ver emails:**
   - Los emails NO llegan a Gmail real
   - Se capturan en MailHog: http://localhost:8025
   - Esto es IDEAL para testing sin spamear emails reales

3. **QR Code:**
   - Se genera automáticamente con la reserva
   - Contiene el UUID de la reserva
   - Se embebe como imagen inline en el email
   - Se puede escanear con celular para verificar

4. **Templates:**
   - `reservation-confirmation.html` - Email con QR (azul)
   - `reservation-cancellation.html` - Email sin QR (rojo)
   - Ubicados en: `src/main/resources/templates/mail/`

---

## ✅ PRUEBA RÁPIDA (Copy-Paste)

### 1. Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"harolah26@gmail.com","password":"admin123"}'
```

### 2. Crear Reserva (reemplaza TOKEN)
```bash
curl -X POST http://localhost:8080/api/reservations \
  -H "Authorization: Bearer TOKEN_AQUI" \
  -H "Content-Type: application/json" \
  -d '{
    "spaceId":"e1234567-89ab-cdef-0123-456789abcdef",
    "startDateTime":"2025-10-26T10:00:00-06:00",
    "endDateTime":"2025-10-26T12:00:00-06:00",
    "purpose":"Prueba email",
    "expectedAttendees":50
  }'
```

### 3. Ver Email
Abre: http://localhost:8025

---

**¡LISTO PARA PROBAR! 🚀**
