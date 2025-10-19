# API REST - Reservas Municipales

## Autenticación

### Iniciar el Programa
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"

### Login
```
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
    "username": "admin@test.com",
    "password": "admin123"
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

### Eliminar espacio
```
DELETE http://localhost:8080/api/spaces/{id}
```

---

## ReservationController

### Obtener todas las reservas
```
GET http://localhost:8080/api/reservations
```

### Obtener reserva por ID
```
GET http://localhost:8080/api/reservations/{id}
```

### Obtener reservas por usuario
```
GET http://localhost:8080/api/reservations/user/{userId}
```

### Obtener reservas por espacio
```
GET http://localhost:8080/api/reservations/space/{spaceId}
```

### Obtener reservas por estado
```
GET http://localhost:8080/api/reservations/status/{status}
```

### Obtener reservas en rango de fechas
```
GET http://localhost:8080/api/reservations/date-range?startDate=2025-10-20T00:00:00-06:00&endDate=2025-10-30T23:59:59-06:00
```

### Crear reserva
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
```
PATCH http://localhost:8080/api/reservations/{id}/cancel?reason=Usuario no puede asistir
```

### Eliminar reserva
```
DELETE http://localhost:8080/api/reservations/{id}
```

---

## Estados válidos para reservas
- PENDING
- CONFIRMED
- CANCELLED
- COMPLETED

## Tipos de espacio disponibles
- 1: Parque
- 2: Salón Comunal
- 3: Campo Deportivo