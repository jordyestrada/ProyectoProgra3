# API REST - Reservas Municipales

## Autenticación

### Iniciar el Programa
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"

### Login
```
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
    "email": "admin@test.com",
    "password": "testpass"
}

{
    "email": "supervisor@test.com",
    "password": "testpass"
}

{
    "email": "user@test.com",
    "password": "testpass"
}
```

**Response:**
```json
{
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "type": "Bearer",
    "username": "admin@test.com",
    "roles": ["ROLE_ADMIN"]
}
```

**Header requerido para todas las consultas:**
```
Authorization: Bearer [token]
```

---

## SpaceController

### Obtener todos los espacios
```
GET http://localhost:8080/api/spaces
```

### Obtener espacio por ID
```
GET http://localhost:8080/api/spaces/{id}
```

### Crear espacio
```
POST http://localhost:8080/api/spaces
Content-Type: application/json

{
    "name": "Parque Central",
    "spaceTypeId": 1,
    "capacity": 100,
    "location": "Centro de la ciudad",
    "outdoor": true,
    "description": "Parque con juegos infantiles"
}
```

### Actualizar espacio
```
PUT http://localhost:8080/api/spaces/{id}
Content-Type: application/json

{
    "name": "Parque Central Actualizado",
    "capacity": 150,
    "description": "Descripción actualizada"
}
```

### Desactivar espacio (Soft Delete - RECOMENDADO)
**Solo ADMIN - Marca el espacio como inactivo sin eliminarlo de la base de datos**
```
DELETE http://localhost:8080/api/spaces/{id}
```

**Response exitoso (200 OK):**
```json
{
  "message": "Space deactivated successfully"
}
```

**Características:**
- ✅ NO elimina de la base de datos
- ✅ Solo cambia `active = false`
- ✅ Reversible: puede reactivarse
- ✅ Mantiene datos históricos y reservas

### Eliminar espacio permanentemente (Hard Delete - PELIGROSO)
**Solo ADMIN - Elimina físicamente el espacio de la base de datos**
```
DELETE http://localhost:8080/api/spaces/{id}/permanent
```

**Response exitoso (200 OK):**
```json
{
  "message": "Space permanently deleted"
}
```

**Response bloqueado por reservas (409 CONFLICT):**
```json
{
  "error": "Cannot delete space",
  "message": "Cannot delete space: it has 5 associated reservation(s). Please deactivate instead."
}
```

**⚠️ CARACTERÍSTICAS:**
- ❌ BORRA PERMANENTEMENTE de la base de datos (DELETE físico)
- ❌ NO reversible: los datos se pierden para siempre
- ✅ Validación integrada: NO permite borrar si tiene reservas asociadas
- ⚠️ Solo usar para limpiar espacios creados por error o pruebas

**Cuándo usar cada uno:**
- 🟢 **Soft Delete** (`/spaces/{id}`): Uso normal, cuando un espacio cierra temporalmente
- 🔴 **Hard Delete** (`/spaces/{id}/permanent`): Solo para eliminar datos de prueba sin reservas

### Búsqueda avanzada de espacios
```
GET http://localhost:8080/api/spaces/search?name=parque
GET http://localhost:8080/api/spaces/search?minCapacity=50&maxCapacity=200
GET http://localhost:8080/api/spaces/search?outdoor=true
GET http://localhost:8080/api/spaces/search?location=centro
GET http://localhost:8080/api/spaces/search?spaceTypeId=1
GET http://localhost:8080/api/spaces/search?name=parque&outdoor=true&minCapacity=100
```

### Buscar espacios disponibles por fechas
```
GET http://localhost:8080/api/spaces/available?startDate=2025-10-20T08:00:00-06:00&endDate=2025-10-20T18:00:00-06:00
GET http://localhost:8080/api/spaces/available?startDate=2025-10-20T14:00:00-06:00&endDate=2025-10-20T16:00:00-06:00&minCapacity=50&spaceTypeId=1
```

---

## ReservationController

### Obtener todas las reservas
**Roles permitidos: ADMIN, SUPERVISOR, USER**
```
GET http://localhost:8080/api/reservations
```

### Obtener reserva por ID
**Roles permitidos: ADMIN, SUPERVISOR, USER**
```
GET http://localhost:8080/api/reservations/{id}
```

### Obtener reservas por usuario
**Roles permitidos: ADMIN, SUPERVISOR, USER**
```
GET http://localhost:8080/api/reservations/user/{userId}
```

### Obtener reservas por espacio
**Roles permitidos: ADMIN, SUPERVISOR, USER**
```
GET http://localhost:8080/api/reservations/space/{spaceId}
```

### Obtener reservas por estado
**Roles permitidos: ADMIN**
```
GET http://localhost:8080/api/reservations/status/{status}
```

### Obtener reservas en rango de fechas
**Roles permitidos: ADMIN, SUPERVISOR, USER**
```
GET http://localhost:8080/api/reservations/date-range?startDate=2025-10-20T00:00:00-06:00&endDate=2025-10-30T23:59:59-06:00
```

### Crear reserva
**Roles permitidos: ADMIN, SUPERVISOR, USER**
```
POST http://localhost:8080/api/reservations
Content-Type: application/json

{
    "spaceId": "[id_espacio]",
    "userId": "[id_usuario]",
    "startsAt": "2025-10-25T14:00:00-06:00",
    "endsAt": "2025-10-25T16:00:00-06:00",
    "status": "PENDING",
    "totalAmount": 15000.00,
    "currency": "CRC"
}
```

### Actualizar reserva
**Roles permitidos: ADMIN, SUPERVISOR, USER**
```
PUT http://localhost:8080/api/reservations/{id}
Content-Type: application/json

{
    "startsAt": "2025-10-25T15:00:00-06:00",
    "endsAt": "2025-10-25T17:00:00-06:00",
    "status": "CONFIRMED",
    "totalAmount": 20000.00
}
```

### Cancelar reserva
**Roles permitidos: ADMIN, SUPERVISOR, USER**
**⚠️ RESTRICCIONES:**
- Debe hacerse con al menos **24 horas** de anticipación (configurable en `application-docker.yml`).
- Usuarios con rol **USER** solo pueden cancelar con 24+ horas de anticipación.
- Usuarios con rol **ADMIN** pueden cancelar en cualquier momento.
- **No se puede cancelar una reserva que ya está cancelada.**

```
PATCH http://localhost:8080/api/reservations/{id}/cancel?reason=Usuario no puede asistir
```

**Respuesta exitosa (200 OK):**
```json
(Sin contenido - vacío)
```

**Respuesta de cancelación tardía (403 FORBIDDEN):**
```json
{
    "error": "Cancelación no permitida",
    "message": "La cancelación debe realizarse con al menos 24 horas de anticipación. Actualmente faltan 18 horas para la reserva. Solo un ADMIN puede cancelar con menos anticipación.",
    "timestamp": "2025-10-23T10:30:00-06:00"
}
```

**Respuesta si ya está cancelada (403 FORBIDDEN):**
```json
{
    "error": "Cancelación no permitida",
    "message": "Esta reserva ya ha sido cancelada previamente.",
    "timestamp": "2025-10-23T10:30:00-06:00"
}
```

### Eliminar reserva
```
DELETE http://localhost:8080/api/reservations/{id}
```

### ⏰ Auto-cancelación de Reservas Pendientes
**El sistema cancela automáticamente las reservas pendientes que no se confirman a tiempo.**

**Comportamiento automático:**
- ✅ Se ejecuta cada **5 minutos** en segundo plano
- ✅ Busca reservas con estado `PENDING` cuya hora de inicio ya pasó
- ✅ Cambia automáticamente el estado a `CANCELLED`
- ✅ Agrega un motivo de cancelación descriptivo con la fecha/hora
- ✅ Logs detallados para auditoría

**Ejemplo de motivo de cancelación automática:**
```
"Cancelada automáticamente - No se confirmó antes de la hora de inicio (25/10/2025 14:00)"
```

**Estados del flujo de vida de una reserva:**
1. **PENDING** → Recién creada, esperando confirmación
2. **CONFIRMED** → Confirmada por el usuario/admin
3. **CANCELLED** → Cancelada manualmente o automáticamente
4. **COMPLETED** → Reserva utilizada y finalizada

**Regla importante:**
- Si una reserva está en `PENDING` y pasa su hora de inicio sin confirmarse → Se cancela automáticamente
- Las reservas `CONFIRMED` NO se cancelan automáticamente

### 📊 Exportar reservas a Excel (Usuario autenticado)
**El usuario exporta sus propias reservas**
```
GET http://localhost:8080/api/reservations/export/excel
Authorization: Bearer [token]
```

**Response:**
- Content-Type: `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`
- Content-Disposition: `attachment; filename="reservas_[usuario]_[fecha].xlsx"`
- Archivo Excel con dos hojas:
  - **Hoja 1 "Reservaciones"**: Tabla con todas las reservas del usuario
  - **Hoja 2 "Resumen"**: Estadísticas y totales

### 📊 Exportar reservas a Excel (Admin/Supervisor)
**Admin o Supervisor puede exportar reservas de cualquier usuario**
```
GET http://localhost:8080/api/reservations/export/excel/{userId}
Authorization: Bearer [token]
```

**Ejemplo:**
```
GET http://localhost:8080/api/reservations/export/excel/550e8400-e29b-41d4-a716-446655440002
Authorization: Bearer [admin_token]
```

**Response:**
- Mismo formato que el endpoint anterior
- Solo usuarios con rol `ADMIN` o `SUPERVISOR` pueden acceder
- Permite consultar las reservas de cualquier usuario del sistema

**Columnas del Excel:**
- ID Reserva
- Espacio
- Fecha Inicio
- Fecha Fin
- Estado de la Reserva
- Monto Total
- Moneda
- Fecha Creación
- Observaciones

**Estadísticas incluidas:**
- Total de reservas
- Reservas confirmadas
- Reservas canceladas
- Reservas pendientes
- Reservas completadas
- Total de dinero pagado

### 📱 Obtener código QR (JSON con Base64)
```
GET http://localhost:8080/api/reservations/{id}/qr
```

### 🖼️ Obtener código QR como imagen PNG
```
GET http://localhost:8080/api/reservations/{id}/qr/image
```

### ✅ Validar QR y marcar asistencia
```
POST http://localhost:8080/api/reservations/{id}/validate-qr
Content-Type: application/json

{
    "qrContent": "RESERVA:99d7391f-3a53-459d-a41a-f5996cea0082:550e8400-e29b-41d4-a716-446655440000:21056e13-415e-486c-9fd6-94d5f6af08e8:1729377298000",
    "validationToken": "cualquier-token"
}
```

### 🔄 Regenerar código QR (Solo ADMIN/SUPERVISOR)
```
POST http://localhost:8080/api/reservations/{id}/regenerate-qr
```

---

## ReviewController

### Obtener todas las reseñas
```
GET http://localhost:8080/api/reviews
```

### Obtener reseña por ID
```
GET http://localhost:8080/api/reviews/[id]
```

### Obtener reseñas por espacio
```
GET http://localhost:8080/api/reviews/space/[id]
```

### Obtener reseñas por usuario
```
GET http://localhost:8080/api/reviews/user/[id]
```

### Obtener estadísticas de un espacio
```
GET http://localhost:8080/api/reviews/space/[id]/statistics
```

**Response:**
```json
{
    "spaceId": "[id]",
    "averageRating": 4.5,
    "totalReviews": 10,
    "ratingDistribution": {
        "1": 0,
        "2": 1,
        "3": 2,
        "4": 3,
        "5": 4
    }
}
```

### Crear reseña
**⚠️ RESTRICCIONES POST-USO:**
- Solo se pueden crear reseñas de reservas con estado **CONFIRMED** o **COMPLETED**
- La reseña solo puede crearse **después** de que pase la fecha de fin de la reserva
- Solo el usuario que realizó la reserva puede reseñar ese espacio
- No se puede crear más de una reseña por reserva

```
POST http://localhost:8080/api/reviews
Content-Type: application/json

{
    "spaceId": "[id]",
    "userId": "[id]",
    "reservationId": "[id]",
    "rating": 5,
    "comment": "Excelente espacio, muy limpio y bien equipado",
    "visible": true
}
```

**Respuesta exitosa (200 OK):**
```json
{
    "reviewId": 1,
    "spaceId": "uuid-espacio",
    "userId": "uuid-usuario",
    "reservationId": "uuid-reserva",
    "rating": 5,
    "comment": "Excelente espacio, muy limpio y bien equipado",
    "visible": true,
    "createdAt": "2025-11-01T14:30:00-06:00"
}
```

**Errores comunes:**

**Estado de reserva inválido (400 Bad Request):**
```json
{
    "error": "Solo se pueden reseñar espacios de reservas confirmadas o completadas. Estado actual: PENDING"
}
```

**Reseña antes de usar el espacio (400 Bad Request):**
```json
{
    "error": "Solo se puede reseñar un espacio después de haber usado la reserva. La reserva finaliza el: 25/10/2025 16:00"
}
```

**Usuario no autorizado (400 Bad Request):**
```json
{
    "error": "Solo el usuario que realizó la reserva puede hacer una reseña de este espacio"
}
```

**Reseña duplicada (400 Bad Request):**
```json
{
    "error": "Ya existe una reseña para esta reserva"
}
```

### Actualizar reseña
```
PUT http://localhost:8080/api/reviews/[id]
Content-Type: application/json

{
    "rating": 4,
    "comment": "Muy buen espacio, pero podría mejorar la iluminación",
    "visible": true
}
```

### Eliminar reseña (Solo ADMIN)
```
DELETE http://localhost:8080/api/reviews/[id]
```

---

## UserController - Gestión de Usuarios

### Obtener todos los usuarios
```
GET http://localhost:8080/api/users
Authorization: Bearer [token]
```

### Obtener usuario por ID
```
GET http://localhost:8080/api/users/{id}
Authorization: Bearer [token]
```

### Cambiar rol de usuario (Solo ADMIN)
**Solo usuarios con rol ADMIN pueden cambiar roles de otros usuarios**
**El sistema envía automáticamente un correo al usuario notificando el cambio**

```
PATCH http://localhost:8080/api/users/change-role
Authorization: Bearer [admin_token]
Content-Type: application/json

{
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "roleCode": "ROLE_ADMIN"
}
```

**Roles válidos:**
- `ROLE_ADMIN` - Administrador con permisos completos
- `ROLE_SUPERVISOR` - Supervisor con permisos de gestión
- `ROLE_USER` - Usuario regular con permisos básicos

**Response exitoso (200 OK):**
```json
{
    "message": "Rol actualizado exitosamente",
    "user": {
        "userId": "550e8400-e29b-41d4-a716-446655440000",
        "email": "user@test.com",
        "fullName": "Usuario Test",
        "phone": "88888888",
        "active": true,
        "roleCode": "ROLE_ADMIN"
    }
}
```

**Errores comunes:**

**Usuario no encontrado (400 Bad Request):**
```json
{
    "error": "Error al cambiar rol",
    "message": "Usuario no encontrado con ID: 550e8400-e29b-41d4-a716-446655440000"
}
```

**Rol no encontrado (400 Bad Request):**
```json
{
    "error": "Error al cambiar rol",
    "message": "Rol no encontrado: ROLE_INVALID"
}
```

**Usuario ya tiene ese rol (400 Bad Request):**
```json
{
    "error": "Error al cambiar rol",
    "message": "El usuario ya tiene el rol: ROLE_ADMIN"
}
```

**Sin permisos (403 Forbidden):**
```json
{
    "error": "Access Denied",
    "message": "You don't have permission to access this resource"
}
```

**📧 Notificación por correo:**
- ✅ Se envía automáticamente un email al usuario cuando su rol cambia
- ✅ El email incluye el rol anterior y el nuevo rol
- ✅ Se detallan los permisos del nuevo rol
- ✅ Email con diseño HTML profesional y responsive
- ✅ Si falla el envío del email, el cambio de rol se completa de todas formas

**Permisos por rol:**

**ROLE_ADMIN:**
- Gestión completa de usuarios y roles
- Administración de espacios y horarios
- Gestión total de reservas
- Acceso a dashboard y métricas
- Cancelación sin restricciones de tiempo
- Exportación de datos de cualquier usuario

**ROLE_SUPERVISOR:**
- Visualización y gestión de reservas
- Gestión de horarios de espacios
- Acceso a dashboard y métricas
- Exportación de datos de usuarios
- Validación de códigos QR

**ROLE_USER:**
- Crear y gestionar sus propias reservas
- Consultar espacios disponibles
- Crear reseñas de espacios utilizados
- Exportar sus propias reservas
- Ver y usar códigos QR de sus reservas

---

## Estados válidos para reservas
- PENDING
- CONFIRMED
- CANCELLED
- COMPLETED

## Rangos válidos para calificaciones
- Rating: 1-5 (1 = Muy malo, 5 = Excelente)

## Tipos de espacio disponibles
- 1: Parque
- 2: Salón Comunal
- 3: Campo Deportivo

---

## DashboardController - Métricas del Sistema

### Obtener métricas del dashboard (Solo ADMIN/SUPERVISOR)
```
GET http://localhost:8080/api/admin/dashboard
Authorization: Bearer [token]
```

**Response incluye 5 categorías de métricas:**

```json
{
  "generalMetrics": {
    "totalReservations": 245,
    "totalSpaces": 12,
    "totalUsers": 89,
    "activeReservations": 34
  },
  "reservationsByStatus": {
    "CONFIRMED": 28,
    "PENDING": 6,
    "CANCELLED": 15,
    "COMPLETED": 196
  },
  "revenueMetrics": {
    "currentMonthRevenue": 12500.0,
    "lastMonthRevenue": 10800.0,
    "percentageChange": 15.74
  },
  "topSpaces": [
    {
      "spaceId": "uuid",
      "spaceName": "Salón Comunal Norte",
      "reservationCount": 45,
      "totalRevenue": 4500.0
    }
  ],
  "temporalMetrics": {
    "reservationsToday": 5,
    "reservationsThisWeek": 34,
    "reservationsThisMonth": 128,
    "reservationsByDayOfWeek": {
      "MONDAY": 18,
      "FRIDAY": 30
    },
    "reservationsByHour": {
      "8": 5,
      "14": 18,
      "16": 22
    },
    "mostPopularDay": "FRIDAY",
    "mostPopularHour": 16
  }
}
```

**Características:**
- ✅ Cache de 10 minutos para optimización
- ✅ Análisis de ingresos con tendencias mes a mes
- ✅ Identificación de días y horas pico
- ✅ Top 5 espacios más rentables

---

## SpaceScheduleController - Horarios de Espacios (RF15)

### Obtener horarios de un espacio
```
GET http://localhost:8080/api/spaces/{spaceId}/schedules
Authorization: Bearer [token]
```

**Response:**
```json
[
  {
    "scheduleId": 1,
    "spaceId": "uuid-del-espacio",
    "weekday": 1,
    "weekdayName": "Monday",
    "timeFrom": "08:00:00",
    "timeTo": "12:00:00"
  }
]
```

### Crear horario (Solo ADMIN/SUPERVISOR)
```
POST http://localhost:8080/api/spaces/{spaceId}/schedules
Authorization: Bearer [token]
Content-Type: application/json

{
  "weekday": 1,
  "timeFrom": "08:00:00",
  "timeTo": "12:00:00"
}
```

**⏰ HORARIO POR DEFECTO:**
- Si NO se especifica `timeFrom`: se usa **06:00 AM** por defecto
- Si NO se especifica `timeTo`: se usa **08:00 PM** (20:00) por defecto
- Esto facilita la creación rápida de horarios estándar

**Días de la semana:** 0=Domingo, 1=Lunes, 2=Martes, 3=Miércoles, 4=Jueves, 5=Viernes, 6=Sábado

### Eliminar horario específico (Solo ADMIN/SUPERVISOR)
```
DELETE http://localhost:8080/api/spaces/{spaceId}/schedules/{scheduleId}
Authorization: Bearer [token]
```

### Eliminar todos los horarios (Solo ADMIN)
```
DELETE http://localhost:8080/api/spaces/{spaceId}/schedules
Authorization: Bearer [token]
```

**Validación automática en reservas:**
- ✅ Si el espacio tiene horarios configurados, las reservas solo pueden **crearse y actualizarse** dentro de esos horarios
- ✅ Si el espacio NO tiene horarios, permite cualquier horario (backward compatible)
- ✅ La reserva debe estar completamente dentro de un bloque horario
- ✅ Mensajes de error descriptivos en español

---

## WeatherController - Información del Clima para Espacios al Aire Libre

### Obtener clima para un espacio específico
**Consulta el clima para espacios al aire libre. Solo funciona con espacios que tengan `outdoor: true`.**

```
GET http://localhost:8080/api/weather/space/{spaceId}
Authorization: Bearer [token]
```

**Ejemplo:**
```
GET http://localhost:8080/api/weather/space/21056e13-415e-486c-9fd6-94d5f6af08e8
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Response exitoso (200 OK):**
```json
{
  "location": "Parque Central",
  "temperature": 28.5,
  "feels_like": 32.1,
  "description": "cielo claro",
  "humidity": 74,
  "wind_speed": 2.57,
  "cloudiness": 20,
  "rain_probability": 15.0,
  "is_outdoor_friendly": true,
  "recommendation": "Condiciones ideales para actividades al aire libre. ¡Disfruta tu reserva!",
  "data_source": "API",
  "fetched_at": "2025-10-28T10:30:00-06:00",
  "latitude": 9.9333,
  "longitude": -84.0833
}
```

**Criterios para "condiciones aptas" (`is_outdoor_friendly: true`):**
- ✅ Temperatura entre 15°C y 30°C
- ✅ Probabilidad de lluvia < 30%
- ✅ Velocidad del viento < 10 m/s

**Errores comunes:**

**404 Not Found** (Espacio no existe):
```json
{
  "error": "Espacio no encontrado",
  "message": "Espacio no encontrado con ID: 21056e13-415e-486c-9fd6-94d5f6af08e8",
  "timestamp": "2025-10-28T10:30:00-06:00"
}
```

**400 Bad Request** (Espacio interior):
```json
{
  "error": "Espacio interior",
  "message": "El espacio 'Salón de Eventos' es interior y no requiere información del clima",
  "timestamp": "2025-10-28T10:30:00-06:00"
}
```

**503 Service Unavailable** (API del clima caída):
```json
{
  "location": "Parque Central",
  "temperature": 0.0,
  "description": "Información del clima no disponible",
  "is_outdoor_friendly": false,
  "recommendation": "No se pudo obtener información del clima. Por favor, intente más tarde.",
  "data_source": "FALLBACK",
  "fetched_at": "2025-10-28T10:30:00-06:00"
}
```

### Obtener clima por ubicación/ciudad
**Consulta el clima para cualquier ciudad del mundo.**

```
GET http://localhost:8080/api/weather/location?location={ciudad}
Authorization: Bearer [token]
```

**Ejemplos:**
```
GET http://localhost:8080/api/weather/location?location=San Jose,CR
GET http://localhost:8080/api/weather/location?location=Cartago,CR
GET http://localhost:8080/api/weather/location?location=Madrid,ES
```

**Response (200 OK):**
```json
{
  "location": "San Jose,CR",
  "temperature": 27.3,
  "feels_like": 30.8,
  "description": "nubes dispersas",
  "humidity": 68,
  "wind_speed": 3.2,
  "cloudiness": 40,
  "rain_probability": 20.0,
  "is_outdoor_friendly": true,
  "recommendation": "Condiciones ideales para actividades al aire libre. ¡Disfruta tu reserva!",
  "data_source": "API",
  "fetched_at": "2025-10-28T10:35:00-06:00",
  "latitude": 9.9281,
  "longitude": -84.0907
}
```

**Formato del parámetro `location`:**
- `"Ciudad,PaísISO"` (ej: `"San Jose,CR"`, `"London,GB"`)
- `"Ciudad"` (ej: `"Madrid"` - si es única)
- Códigos ISO de país: CR (Costa Rica), US (Estados Unidos), ES (España), etc.

**Error si la ubicación no existe (400 Bad Request):**
```json
{
  "error": "WEATHER_API_ERROR",
  "message": "No se pudieron obtener coordenadas para: CiudadInvalida",
  "timestamp": "2025-10-28T10:40:00-06:00"
}
```

### Health check del servicio (Solo ADMIN)
**Verifica el estado de la conexión con la API del clima.**

```
GET http://localhost:8080/api/weather/health
Authorization: Bearer [admin_token]
```

**Response (200 OK):**
```json
{
  "status": "UP",
  "service": "Weather API Integration",
  "healthy": true,
  "timestamp": "2025-10-28T10:45:00-06:00"
}
```

**Response si la API falla (200 OK - con estado DOWN):**
```json
{
  "status": "DOWN",
  "service": "Weather API Integration",
  "healthy": false,
  "timestamp": "2025-10-28T10:45:00-06:00"
}
```

---

## Características del Sistema de Clima

### Cache automático
- **Duración**: 5 minutos (300 segundos)
- **Beneficio**: Reduce llamadas a la API externa y mejora tiempos de respuesta
- **Tamaño máximo**: 500 entradas en memoria

### Tolerancia a fallos (Resilience4j)
- **Retry**: Reintenta hasta 3 veces con backoff exponencial (500ms, 1s, 2s)
- **Circuit Breaker**: Si 50% de las requests fallan, abre el circuito por 30s
- **Fallback**: Si falla todo, retorna datos genéricos (temp 0°C, "no disponible")
- **Timeout**: Cancela requests que tarden más de 2 segundos

### Fuentes de datos (`data_source`)
- **`API`**: Datos obtenidos de OpenWeatherMap en tiempo real
- **`CACHE`**: Datos servidos desde cache (no visible en response, pero más rápido)
- **`FALLBACK`**: Datos genéricos cuando la API falla

### API externa utilizada
- **Proveedor**: OpenWeatherMap One Call API 3.0
- **URL base**: `https://api.openweathermap.org/data/3.0`
- **Documentación**: https://openweathermap.org/api/one-call-3
- **Límite gratuito**: 1,000 requests/día
- **Unidades**: Métricas (Celsius, m/s, %)
- **Idioma**: Español (descripciones en español)

---

## Tests Unitarios

### Comando para ejecutar todos los tests
```powershell
.\mvnw.cmd test
```

### Estado de Tests (132/132 implementados - 100%)

#### ✅ Tests de Servicios Implementados
- ✅ AuthenticationServiceTest (3 tests)
- ✅ AzureAdServiceTest (1 test)
- ✅ DataInitializationServiceTest (8 tests)
- ✅ FeatureServiceTest (1 test)
- ✅ JwtServiceTest (7 tests)
- ✅ MetricsServiceTest (6 tests)
- ✅ QRCodeServiceTest (13 tests)
- ✅ ReservationAutoStatusServiceTest (8 tests)
- ✅ ReservationExportServiceTest (6 tests)
- ✅ ReservationServiceTest (16 tests)
- ✅ ReviewServiceTest (11 tests)
- ✅ SpaceImageServiceTest (2 tests)
- ✅ SpaceRateServiceTest (7 tests)
- ✅ SpaceScheduleServiceTest (13 tests)
- ✅ SpaceServiceTest (22 tests)
- ✅ UserServiceTest (6 tests)
- ✅ WeatherServiceTest (3 tests)
- ✅ ReservasMunicipalesApplicationTests (1 test - integración)

#### ❌ Tests de Controllers Pendientes
- ❌ AuthControllerTest
- ❌ DashboardControllerTest
- ❌ ReservationControllerTest
- ❌ ReviewControllerTest
- ❌ SpaceControllerTest
- ❌ SpaceScheduleControllerTest
- ❌ UserControllerTest
- ❌ WeatherControllerTest
- ❌ PingControllerTest