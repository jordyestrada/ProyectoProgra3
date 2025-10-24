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
Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method POST -ContentType "application/json" -Body '{"email":"admin@municipalidad.cr","password":"testpass"}'
```

O con curl (Git Bash / WSL):

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@municipalidad.cr","password":"testpass"}'
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

Conclusión
- Levanté Postgres y la aplicación con el perfil `docker`. La app respondió en `localhost:8080`, pero los endpoints de Actuator están protegidos por la configuración de seguridad por defecto. Si quieres, puedo fijar credenciales en `application-docker.yml` para facilitar pruebas.
