# Sistema de Reservas Municipales - Cliente JavaFX

Cliente de escritorio JavaFX para el sistema de reservas municipales.

## Arquitectura

El proyecto implementa el patrón **MVVM (Model-View-ViewModel)** con un módulo de red completamente aislado:

```
reservas-frontend-javafx/
│
├── app/                          # App.java (arranque + DI sencillo)
│
├── ui/                          # Vistas FXML + Controllers
│   ├── login/                   # Login
│   ├── spaces/                  # Gestión de espacios
│   ├── reservations/            # Gestión de reservas
│   ├── admin/                   # Panel de administración
│   └── reports/                 # Reportes
│
├── viewmodel/                   # MVVM: lógica de pantalla, sin código de red
│   ├── LoginViewModel.java
│   ├── SpacesViewModel.java
│   ├── ReservationsViewModel.java
│   └── ...
│
├── domain/                      # DTOs (Space, Reservation, User, etc.)
│   ├── Space.java
│   ├── Reservation.java
│   ├── User.java
│   └── ...
│
├── data/                        # Módulo de red aislado
│   ├── ApiClient.java           # Cliente HTTP (interceptores JWT, manejo de errores)
│   └── ...
│
└── resources/                   # FXML, CSS, iconos
    ├── fxml/
    ├── css/
    └── icons/
```

## Tecnologías

- **JavaFX 21**: Interfaz gráfica
- **Java 21**: Lenguaje base
- **Apache HttpClient 5**: Cliente HTTP para consumir API REST
- **Gson**: Serialización/deserialización JSON
- **Lombok**: Reducción de código boilerplate
- **ZXing**: Generación y lectura de códigos QR

## Requisitos

- JDK 21+
- Maven 3.8+ (o usar el Maven wrapper incluido: `mvnw.cmd`)
- API Backend corriendo en `http://localhost:8080`

> **Nota**: Este proyecto **NO usa el sistema de módulos Java** (`module-info.java`). Funciona con el classpath tradicional para simplificar la configuración y evitar conflictos de módulos.

## Configuración

La URL base de la API se configura en `App.java`:

```java
ApiClient.initialize("http://localhost:8080");
```

## Compilar y Ejecutar

### Compilar el proyecto:
```bash
mvn clean compile
```

### Ejecutar la aplicación:
```bash
mvn javafx:run
```

### Generar JAR ejecutable:
```bash
mvn clean package
```

El JAR se generará en `target/reservas-frontend-javafx-1.0-SNAPSHOT.jar`

## Funcionalidades por Rol

### Usuario Regular (ROLE_USER)
- ✅ Login con email/contraseña
- ✅ Ver listado de espacios disponibles
- ✅ Buscar espacios por nombre, tipo, ubicación
- ✅ Ver detalles de un espacio
- ✅ Crear nueva reserva
- ✅ Ver mis reservas
- ✅ Ver código QR de reserva
- ✅ Cancelar reserva

### Administrador (ROLE_ADMIN)
Todo lo anterior más:
- ✅ Gestión completa de espacios (CRUD)
- ✅ Gestión de todas las reservas
- ✅ Validar códigos QR
- ✅ Gestión de usuarios
- ✅ Reportes y estadísticas

## Endpoints de la API Consumidos

### Autenticación
- `POST /api/auth/login` - Login
- `POST /api/auth/validate` - Validar token JWT
- `GET /api/auth/me` - Datos del usuario actual

### Espacios
- `GET /api/spaces` - Listar todos
- `GET /api/spaces/active` - Listar activos
- `GET /api/spaces/{id}` - Obtener por ID
- `GET /api/spaces/search?query={query}` - Buscar
- `POST /api/spaces` - Crear (admin)
- `PUT /api/spaces/{id}` - Actualizar (admin)
- `DELETE /api/spaces/{id}` - Eliminar (admin)

### Reservas
- `GET /api/reservations` - Listar todas (admin)
- `GET /api/reservations/user/{userId}` - Mis reservas
- `GET /api/reservations/{id}` - Obtener por ID
- `GET /api/reservations/status/{status}` - Filtrar por estado
- `POST /api/reservations` - Crear reserva
- `PUT /api/reservations/{id}` - Actualizar
- `DELETE /api/reservations/{id}` - Cancelar
- `GET /api/reservations/{id}/qr` - Obtener QR
- `POST /api/reservations/{id}/validate-qr` - Validar QR

### Reseñas
- `GET /api/reviews/space/{spaceId}` - Reseñas de un espacio
- `POST /api/reviews` - Crear reseña
- `PUT /api/reviews/{id}` - Actualizar reseña
- `DELETE /api/reviews/{id}` - Eliminar reseña

### Usuarios
- `GET /api/users` - Listar usuarios (admin)
- `GET /api/users/{id}` - Obtener por ID

## Estructura de Módulos

### 1. Data Layer (Módulo de Red Aislado)
**`ApiClient.java`**: Cliente HTTP completamente aislado
- Maneja todas las llamadas HTTP (GET, POST, PUT, DELETE)
- Gestión automática de token JWT en headers
- Serialización/deserialización JSON con Gson
- Manejo de errores HTTP (4xx, 5xx)
- Respuesta encapsulada en `ApiResponse<T>`

### 2. Domain Layer (DTOs)
Objetos de transferencia de datos que coinciden exactamente con los DTOs de la API:
- `User.java`
- `Space.java`
- `Reservation.java`
- `Review.java`
- `JwtResponse.java`
- `LoginRequest.java`

### 3. ViewModel Layer (MVVM)
Lógica de negocio y estado de cada pantalla:
- `LoginViewModel.java`: Lógica de autenticación
- `SpacesViewModel.java`: Lógica de listado y búsqueda de espacios
- `ReservationsViewModel.java`: Lógica de gestión de reservas
- Sin conocimiento directo de JavaFX (solo Properties para binding)

### 4. View Layer (UI)
Vistas JavaFX con binding a ViewModels:
- `LoginView.java`: Pantalla de login
- `MainView.java`: Ventana principal con tabs
- `SpacesView.java`: Listado de espacios
- `ReservationsView.java`: Mis reservas
- `AdminView.java`: Panel de administración
- `ReportsView.java`: Reportes y estadísticas

## Convenciones de Código

- Nombres de clases en inglés
- Packages organizados por feature (`ui.login`, `ui.spaces`, etc.)
- ViewModels exponen `Properties` para binding con vistas
- Llamadas a API siempre en threads separados (no bloquear UI)
- Manejo de errores con `Alert` de JavaFX

## TODO - Próximas Implementaciones

- [ ] Implementar carga real de datos desde API
- [ ] Añadir diálogo de creación de reserva
- [ ] Implementar visualización de código QR
- [ ] Añadir validación de campos en formularios
- [ ] Implementar búsqueda de espacios
- [ ] Añadir paginación en listados
- [ ] Implementar generación de reportes
- [ ] Añadir caché local de datos
- [ ] Implementar modo offline limitado
- [ ] Mejorar estilos CSS

## Contacto

Proyecto desarrollado para el curso de Programación 3 - Universidad Nacional de Costa Rica
