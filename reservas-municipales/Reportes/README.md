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