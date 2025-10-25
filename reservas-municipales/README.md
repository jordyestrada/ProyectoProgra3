Levantar y comprobar la aplicación (resumen corto)

Este README explica cómo levanté la base de datos en Docker y arranqué la aplicación Spring Boot con el perfil `docker`, lo que comprobé y los comandos que usé (PowerShell / Windows).

Resumen del resultado
- La base de datos PostgreSQL se levantó con `docker-compose` y ejecutó el script `src/main/resources/db/init.sql` (tablas e índices creados).
- La aplicación se construyó y arrancó con el perfil `docker`. Tomcat quedó escuchando en `http://localhost:8080`.
- El endpoint `/actuator/health` existe pero está protegido por Spring Security por defecto (retorna 401 si no se envían credenciales). En los logs de la aplicación aparece una contraseña generada si no se configuró ninguna.

Requisitos
- Docker y docker-compose instalados y funcionando.
- Java 21 instalado (o usar `./mvnw.cmd` para construir usando el wrapper).

Comandos usados (PowerShell)

1) Levantar Postgres (desde la raíz del proyecto):

```
docker-compose up -d
```

2) Verificar logs de inicialización de la base de datos (opcional):

```
docker-compose logs --no-color postgres --tail=200
```

3) Empaquetar la aplicación (mvn wrapper):

```
.\mvnw.cmd -DskipTests package
```

4) Arrancar la aplicación usando el perfil `docker` (PowerShell):

```
& java "-Dspring.profiles.active=docker" -jar "target\reservas-municipales-0.0.1-SNAPSHOT.jar"
```

Nota: intentos directos con `./mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=docker` pueden fallar por cómo PowerShell pasa los argumentos; la forma robusta es "package + java -jar" (arranque con la variable `-Dspring.profiles.active`). Si tienes `mvnd` instalado puedes usar `mvnd -Dspring-boot.run.profiles=docker spring-boot:run`.

5) Probar el endpoint de salud (PowerShell):

```
Invoke-WebRequest -UseBasicParsing -Uri http://localhost:8080/actuator/health
```

Obtendrás 401 si no envías credenciales.

Con credenciales (ejemplo con curl desde Git Bash o WSL):

```
curl -u admin:changeme http://localhost:8080/actuator/health
```

Si no configuraste credenciales, Spring Boot genera una contraseña aleatoria y la imprime en los logs al arrancar la app (línea similar a "Using generated security password: <password>"). Puedes leer la consola/ventana donde se arrancó la app para obtenerla.

Comandos útiles para controlar el entorno

Detener contenedores:

```
docker-compose down
```

Detener la aplicación (si la arrancaste con `java -jar` desde PowerShell): presiona Ctrl+C en la consola donde corre, o mata el proceso Java.

Notas importantes
- No modifiqué el script `src/main/resources/db/init.sql` (se mantuvo intacto).
- Si quieres evitar la protección por defecto y permitir `GET /actuator/health` sin autenticación para pruebas locales, añade en `application-docker.yml`:

```
spring:
  security:
    user:
      name: admin
      password: changeme
# o deshabilita security (solo para pruebas) con: spring.security.enabled=false
```

## Probar Métricas del Dashboard (RF10)

El sistema incluye un endpoint de métricas administrativas que muestra estadísticas en tiempo real del sistema de reservas.

### Endpoint del Dashboard

```
GET http://localhost:8080/api/admin/dashboard
```

**Requisitos:**
- Autenticación con JWT
- Rol: `ADMIN` o `SUPERVISOR`

### Pasos para Probar

#### 1. Autenticarse (obtener JWT)

```powershell
# PowerShell
Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method POST -ContentType "application/json" -Body '{"email":"admin@test.com","password":"admin123"}'
```

O con curl (Git Bash / WSL):

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@test.com","password":"admin123"}'
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": "uuid-del-usuario",
  "email": "admin@municipalidad.cr",
  "role": "ADMIN"
}
```

Copia el valor del campo `token`.

#### 2. Consultar Dashboard (usando el token)

```powershell
# PowerShell
$token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
Invoke-RestMethod -Uri "http://localhost:8080/api/admin/dashboard" -Headers @{Authorization="Bearer $token"}
```

O con curl:

```bash
curl -X GET http://localhost:8080/api/admin/dashboard \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### Estructura de la Respuesta

El dashboard devuelve 5 categorías de métricas:

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
      "spaceId": "uuid-del-espacio",
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
      "TUESDAY": 22,
      "WEDNESDAY": 20,
      "THURSDAY": 25,
      "FRIDAY": 30,
      "SATURDAY": 8,
      "SUNDAY": 5
    },
    "reservationsByHour": {
      "8": 5,
      "9": 12,
      "10": 15,
      "14": 18,
      "16": 22
    },
    "mostPopularDay": "FRIDAY",
    "mostPopularHour": 16
  }
}
```

### Descripción de las Métricas

#### **1. General Metrics**
- `totalReservations`: Total de reservas en el sistema
- `totalSpaces`: Total de espacios registrados
- `totalUsers`: Total de usuarios registrados
- `activeReservations`: Reservas actuales (CONFIRMED + PENDING)

#### **2. Reservations By Status**
Distribución de reservas por estado:
- `CONFIRMED`: Confirmadas
- `PENDING`: Pendientes de confirmación
- `CANCELLED`: Canceladas
- `COMPLETED`: Completadas

#### **3. Revenue Metrics**
- `currentMonthRevenue`: Ingresos del mes actual
- `lastMonthRevenue`: Ingresos del mes anterior
- `percentageChange`: Porcentaje de crecimiento/decrecimiento

#### **4. Top Spaces**
Top 5 espacios más reservados con:
- `reservationCount`: Cantidad de reservas
- `totalRevenue`: Ingresos generados

#### **5. Temporal Metrics**
Tendencias de uso:
- `reservationsToday`: Reservas creadas hoy
- `reservationsThisWeek`: Reservas de los últimos 7 días
- `reservationsThisMonth`: Reservas de los últimos 30 días
- `reservationsByDayOfWeek`: Distribución por día de la semana
- `reservationsByHour`: Distribución por hora del día (0-23)
- `mostPopularDay`: Día de la semana más popular
- `mostPopularHour`: Hora del día más popular

### Optimización con Cache

El dashboard utiliza cache de **10 minutos** (Caffeine):
- **Primera llamada**: Calcula todas las métricas (~150-200ms)
- **Llamadas subsecuentes (dentro de 10 min)**: Responde desde cache (~5-10ms)
- **Después de 10 min**: Recalcula y actualiza el cache

### Pruebas con Postman

1. Crear colección "Reservas Municipales"
2. Agregar request de **Login** (POST `/api/auth/login`)
3. Agregar request de **Dashboard** (GET `/api/admin/dashboard`)
4. En el request del Dashboard, usar el token del login en el header:
   ```
   Authorization: Bearer {{token}}
   ```

### Casos de Error

**403 Forbidden:**
```json
{
  "error": "Forbidden",
  "message": "Access Denied"
}
```
→ El usuario no tiene rol `ADMIN` o `SUPERVISOR`

**401 Unauthorized:**
```json
{
  "error": "Unauthorized"
}
```
→ Token inválido o expirado (hacer login nuevamente)

**500 Internal Server Error:**
→ Revisar logs de la aplicación, puede faltar configuración de base de datos

### Tecnologías Utilizadas

- **100% ORM**: Spring Data JPA con métodos derivados (sin queries manuales)
- **Cache**: Caffeine Cache (10 min TTL)
- **Security**: Role-based con `@PreAuthorize`
- **Logging**: SLF4J para trazabilidad

---

## Gestión de Horarios de Espacios (RF15)

El sistema permite configurar horarios operativos para cada espacio municipal. Cuando un espacio tiene horarios configurados, las reservas solo pueden crearse dentro de esos horarios.

### Características Principales

- **Horarios por día de semana**: Cada espacio puede tener diferentes horarios para cada día
- **Múltiples bloques**: Un espacio puede tener varios bloques horarios en el mismo día
- **Validación automática**: Al crear una reserva, el sistema valida que esté dentro del horario del espacio
- **Backward compatible**: Si un espacio no tiene horarios configurados, permite reservas en cualquier horario

### Estructura de Horarios

**Días de la semana:**
- `0` = Domingo (Sunday)
- `1` = Lunes (Monday)
- `2` = Martes (Tuesday)
- `3` = Miércoles (Wednesday)
- `4` = Jueves (Thursday)
- `5` = Viernes (Friday)
- `6` = Sábado (Saturday)

### Endpoints API

#### 1. Obtener horarios de un espacio

```
GET /api/spaces/{spaceId}/schedules
```

**Autenticación:** Requiere JWT (cualquier usuario autenticado)

**Ejemplo:**
```powershell
# PowerShell
$token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
$spaceId = "uuid-del-espacio"
Invoke-RestMethod -Uri "http://localhost:8080/api/spaces/$spaceId/schedules" -Headers @{Authorization="Bearer $token"}
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
  },
  {
    "scheduleId": 2,
    "spaceId": "uuid-del-espacio",
    "weekday": 1,
    "weekdayName": "Monday",
    "timeFrom": "14:00:00",
    "timeTo": "18:00:00"
  }
]
```

#### 2. Crear un horario

```
POST /api/spaces/{spaceId}/schedules
```

**Autenticación:** Requiere rol `ADMIN` o `SUPERVISOR`

**Request Body:**
```json
{
  "weekday": 1,
  "timeFrom": "08:00:00",
  "timeTo": "12:00:00"
}
```

**Ejemplo:**
```powershell
# PowerShell
$token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
$spaceId = "uuid-del-espacio"
$body = @{
  weekday = 1
  timeFrom = "08:00:00"
  timeTo = "12:00:00"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/spaces/$spaceId/schedules" -Method POST -Headers @{Authorization="Bearer $token"; "Content-Type"="application/json"} -Body $body
```

**Validaciones:**
- El espacio debe existir
- `weekday` debe estar entre 0 y 6
- `timeFrom` debe ser anterior a `timeTo`
- No debe solaparse con horarios existentes en el mismo día

**Errores comunes:**

```json
{
  "error": "Bad Request",
  "message": "End time must be after start time"
}
```

```json
{
  "error": "Bad Request",
  "message": "Schedule overlaps with existing schedule on Monday from 08:00 to 12:00"
}
```

#### 3. Eliminar un horario específico

```
DELETE /api/spaces/{spaceId}/schedules/{scheduleId}
```

**Autenticación:** Requiere rol `ADMIN` o `SUPERVISOR`

**Ejemplo:**
```powershell
# PowerShell
$token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
Invoke-RestMethod -Uri "http://localhost:8080/api/spaces/$spaceId/schedules/1" -Method DELETE -Headers @{Authorization="Bearer $token"}
```

**Response:** `204 No Content`

#### 4. Eliminar todos los horarios de un espacio

```
DELETE /api/spaces/{spaceId}/schedules
```

**Autenticación:** Requiere rol `ADMIN` solamente

**Ejemplo:**
```powershell
# PowerShell
$token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
Invoke-RestMethod -Uri "http://localhost:8080/api/spaces/$spaceId/schedules" -Method DELETE -Headers @{Authorization="Bearer $token"}
```

**Response:** `204 No Content`

### Validación en Reservas

Cuando se crea una reserva (`POST /api/reservations`), el sistema automáticamente:

1. **Verifica si el espacio tiene horarios configurados**
   - Si NO tiene horarios → permite cualquier horario (backward compatible)
   - Si SÍ tiene horarios → valida contra ellos

2. **Valida el día de la semana**
   - Extrae el día de la fecha de inicio
   - Busca horarios para ese día
   - Si no hay horarios para ese día → error

3. **Valida las horas**
   - La reserva debe estar **completamente dentro** de un bloque horario
   - No puede comenzar antes del `timeFrom`
   - No puede terminar después del `timeTo`

**Ejemplos de errores:**

```json
{
  "error": "Bad Request",
  "message": "El espacio no está disponible los domingos"
}
```

```json
{
  "error": "Bad Request",
  "message": "El espacio solo está disponible los lunes en los siguientes horarios: 08:00 - 12:00, 14:00 - 18:00"
}
```

### Ejemplo Completo: Configurar Horarios

#### Escenario: Salón Comunal abierto Lunes a Viernes

```powershell
# PowerShell
$token = "tu-jwt-token"
$spaceId = "uuid-del-espacio"

# Lunes a Viernes: 8:00-12:00 y 14:00-18:00
foreach ($day in 1..5) {
  # Bloque mañana
  $body1 = @{weekday=$day; timeFrom="08:00:00"; timeTo="12:00:00"} | ConvertTo-Json
  Invoke-RestMethod -Uri "http://localhost:8080/api/spaces/$spaceId/schedules" -Method POST -Headers @{Authorization="Bearer $token"; "Content-Type"="application/json"} -Body $body1
  
  # Bloque tarde
  $body2 = @{weekday=$day; timeFrom="14:00:00"; timeTo="18:00:00"} | ConvertTo-Json
  Invoke-RestMethod -Uri "http://localhost:8080/api/spaces/$spaceId/schedules" -Method POST -Headers @{Authorization="Bearer $token"; "Content-Type"="application/json"} -Body $body2
}

# Sábado: Solo mañana 9:00-13:00
$body3 = @{weekday=6; timeFrom="09:00:00"; timeTo="13:00:00"} | ConvertTo-Json
Invoke-RestMethod -Uri "http://localhost:8080/api/spaces/$spaceId/schedules" -Method POST -Headers @{Authorization="Bearer $token"; "Content-Type"="application/json"} -Body $body3
```

### Base de Datos

La tabla `space_schedule` almacena los horarios:

```sql
CREATE TABLE space_schedule (
  schedule_id  bigserial PRIMARY KEY,
  space_id     uuid NOT NULL,
  weekday      smallint NOT NULL,  -- 0=Sunday, 6=Saturday
  time_from    time NOT NULL,
  time_to      time NOT NULL,
  CONSTRAINT fk_space_schedule_space
    FOREIGN KEY (space_id) REFERENCES space(space_id) ON DELETE CASCADE
);

CREATE INDEX idx_space_schedule_weekday ON space_schedule (space_id, weekday);
```

### Implementación ORM (Sin @Query)

Todos los métodos usan **derived queries** de Spring Data JPA:

```java
// SpaceScheduleRepository
List<SpaceSchedule> findBySpace_SpaceId(UUID spaceId);
List<SpaceSchedule> findBySpace_SpaceIdAndWeekday(UUID spaceId, Short weekday);
void deleteBySpace_SpaceId(UUID spaceId);
boolean existsBySpace_SpaceId(UUID spaceId);
long countBySpace_SpaceId(UUID spaceId);
```

Siguiendo la recomendación del profesor de **evitar @Query** y usar métodos derivados.

### Casos de Uso

1. **Espacio nuevo sin horarios**: Funciona como antes, acepta cualquier horario
2. **Configurar horarios regulares**: ADMIN configura horarios de operación
3. **Reserva dentro de horario**: Se crea normalmente
4. **Reserva fuera de horario**: Sistema rechaza con mensaje descriptivo
5. **Modificar horarios**: ADMIN puede agregar/eliminar horarios según necesidades
6. **Eliminar horarios**: Si se eliminan todos los horarios, vuelve al comportamiento sin restricciones

---

## Sistema de Notificaciones por Email con QR Codes

El sistema ahora envía notificaciones automáticas por email cuando se crean o cancelan reservas.

### Características

- ✅ **Email de confirmación**: Se envía automáticamente al crear una reserva (incluye código QR embebido)
- ✅ **Email de cancelación**: Se envía automáticamente al cancelar una reserva
- ✅ **Templates HTML profesionales**: Diseño responsivo con Thymeleaf
- ✅ **QR Code embebido**: Como imagen inline, no como adjunto
- ✅ **Multi-perfil**: Gmail SMTP (dev) y MailHog (docker)

### Configuración de Perfiles

**Perfil `dev` (desarrollo local):**
- Servidor SMTP: Gmail (`smtp.gmail.com:587`)
- Emails se envían a direcciones reales
- Ver guía completa: `PRUEBA_EMAIL.md`

**Perfil `docker` (testing local):**
- Servidor SMTP: MailHog (`localhost:1025`)
- Emails se capturan localmente (NO se envían a Gmail real)
- Ver emails en: http://localhost:8025
- Ver guía completa: `PRUEBA_EMAIL_DOCKER.md`

### Probar Notificaciones

Ver guías detalladas:
- `PRUEBA_EMAIL.md` - Guía para perfil dev (Gmail)
- `PRUEBA_EMAIL_DOCKER.md` - Guía para perfil docker (MailHog)

### Usuarios de Prueba

Todos los usuarios usan el password: **`admin123`** (cifrado con BCrypt)

| Email | Nombre | Role |
|-------|--------|------|
| `admin@test.com` | Administrador Sistema | ADMIN |
| `harolah26@gmail.com` | Harold Hernández | USER |
| `usuario@test.com` | Usuario Prueba | USER |
| `supervisor@test.com` | Supervisor Municipal | SUPERVISOR |

**⚠️ IMPORTANTE:** El sistema ahora usa **BCrypt** para autenticación. Si tienes usuarios existentes en la base de datos de producción con passwords en texto plano, debes ejecutar el script de migración `migrate-passwords.sql` antes de desplegar. Ver detalles en `MIGRACION_PASSWORDS.md`.

---

Conclusión
- Levanté Postgres y la aplicación con el perfil `docker`. La app respondió en `localhost:8080`, pero los endpoints de Actuator están protegidos por la configuración de seguridad por defecto. Si quieres, puedo fijar credenciales en `application-docker.yml` para facilitar pruebas.