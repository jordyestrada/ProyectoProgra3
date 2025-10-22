# Sistema de Reservas Municipales - Cliente JavaFX
**Municipalidad de Pérez Zeledón**

Cliente de escritorio JavaFX para el sistema de reservas de espacios municipales.

---

## 📋 Tabla de Contenidos

- [Características](#-características)
- [Arquitectura](#-arquitectura)
- [Tecnologías](#-tecnologías)
- [Requisitos](#-requisitos)
- [Instalación y Ejecución](#-instalación-y-ejecución)
- [Estructura del Proyecto](#-estructura-del-proyecto)
- [Sistema de Diseño](#-sistema-de-diseño)
- [Funcionalidades por Rol](#-funcionalidades-por-rol)
- [API Endpoints](#-api-endpoints)
- [Documentación Adicional](#-documentación-adicional)

---

## ✨ Características

- 🎨 **Sistema de Diseño Completo**: Paleta de colores, tipografía y componentes estandarizados
- 🔐 **Autenticación Segura**: Login con Azure AD y gestión de sesiones JWT
- 📱 **8 Pantallas Implementadas**: Login, Dashboard, Espacios, Reservas, Admin, Reportes
- 🎯 **Roles de Usuario**: Cliente regular y Administrador con permisos diferenciados
- 📊 **Reportes y Estadísticas**: Visualización de datos con gráficos (BarChart, PieChart)
- 📝 **Gestión Completa**: CRUD de espacios y reservas
- 📲 **Códigos QR**: Generación y validación de QR para reservas
- 🎨 **UI Moderna**: Diseño inspirado en Material Design con paleta municipal

---

## 🏗 Arquitectura

El proyecto implementa el patrón **MVVM (Model-View-ViewModel)** con separación clara de responsabilidades:

```
reservas-frontend-javafx/
│
├── src/main/java/cr/una/reservas/frontend/
│   ├── app/                     # Arranque de la aplicación
│   ├── ui/                      # Controllers de vistas FXML
│   │   ├── LoginController.java
│   │   ├── DashboardController.java
│   │   ├── EspaciosController.java
│   │   ├── MisReservasController.java
│   │   └── admin/
│   ├── viewmodel/               # Lógica de negocio (MVVM)
│   ├── domain/                  # DTOs y modelos
│   │   ├── Space.java
│   │   ├── Reservation.java
│   │   └── User.java
│   └── data/                    # Capa de datos y API
│       └── ApiClient.java
│
└── src/main/resources/
    ├── fxml/                    # Archivos FXML de vistas
    │   ├── login.fxml
    │   ├── dashboard.fxml
    │   ├── espacios.fxml
    │   ├── espacio-detalle.fxml
    │   ├── reserva-form.fxml
    │   ├── mis-reservas.fxml
    │   ├── admin-espacios.fxml
    │   └── admin-reportes.fxml
    ├── styles.css               # Hoja de estilos principal
    ├── fonts/                   # Fuentes personalizadas
    │   ├── Playfair-Display.ttf
    │   └── Inter.ttf
    └── images/                  # Recursos gráficos
```

---

## 🛠 Tecnologías

| Tecnología | Versión | Propósito |
|------------|---------|-----------|
| **Java** | 21+ | Lenguaje base |
| **JavaFX** | 21 | Framework de interfaz gráfica |
| **Maven** | 3.8+ | Gestión de dependencias |
| **Apache HttpClient** | 5.x | Cliente HTTP para API REST |
| **Gson** | 2.10+ | Serialización JSON |
| **Lombok** | 1.18+ | Reducción de boilerplate |
| **ZXing** | 3.5+ | Códigos QR |
| **Scene Builder** | 21.0+ | Diseñador visual FXML |

---

## 📦 Requisitos

### Requisitos de Sistema
- ☕ **JDK 21 o superior**
- 🔧 **Maven 3.8+** (o usar wrapper incluido)
- 🌐 **Conexión a Internet** (para cargar fuentes y recursos)
- 🖥️ **Sistema Operativo**: Windows 10/11, macOS 10.14+, Linux

### Backend API
- 🔗 API Backend corriendo en `http://localhost:8080`
- 📡 Endpoints REST configurados según especificación

---

## 🚀 Instalación y Ejecución

### 1. Clonar el Repositorio
```bash
git clone https://github.com/jordyestrada/ProyectoProgra3.git
cd ProyectoProgra3/reservas-frontend-javafx
```

### 2. Instalar Fuentes (Opcional pero Recomendado)

**Descargar fuentes:**
- [Playfair Display](https://fonts.google.com/specimen/Playfair+Display) (Pesos: 400, 600, 700)
- [Inter](https://fonts.google.com/specimen/Inter) (Pesos: 400, 500, 600, 700)

**Copiar a:**
```
src/main/resources/fonts/
├── Playfair-Display.ttf
├── Playfair-Display-Bold.ttf
├── Inter-Regular.ttf
├── Inter-Medium.ttf
├── Inter-SemiBold.ttf
└── Inter-Bold.ttf
```

### 3. Compilar el Proyecto
```bash
mvn clean compile
```

O con wrapper de Windows:
```bash
.\mvnw.cmd clean compile
```

### 4. Ejecutar la Aplicación
```bash
mvn javafx:run
```

O con wrapper:
```bash
.\mvnw.cmd javafx:run
```

### 5. Generar JAR Ejecutable
```bash
mvn clean package
```

El JAR se generará en `target/reservas-frontend-javafx-1.0-SNAPSHOT.jar`

### 6. Ejecutar JAR
```bash
java -jar target/reservas-frontend-javafx-1.0-SNAPSHOT.jar
```

---

## 📱 Pantallas Implementadas

### 1. **Login** (`login.fxml`)
- Diseño centrado 1024×768px
- Autenticación con Azure AD
- Validación de credenciales
- Navegación al Dashboard tras login exitoso

### 2. **Dashboard** (`dashboard.fxml`)
- Vista general del sistema
- 4 tarjetas de estadísticas (Total Reservas, Activas, Completadas, Espacios Disponibles)
- Próximas reservas del usuario
- Acciones rápidas (Nueva Reserva, Ver Espacios, Reportes)

### 3. **Catálogo de Espacios** (`espacios.fxml`)
- Grid de 3 columnas con cards de espacios
- Barra de búsqueda
- Filtros por tipo y capacidad
- Navegación a detalle de espacio

### 4. **Detalle de Espacio** (`espacio-detalle.fxml`)
- Galería de imágenes (principal + thumbnails)
- Información completa (precio, capacidad, rating)
- Características y amenidades
- Calendario de disponibilidad
- Botón de reserva

### 5. **Formulario de Reserva** (`reserva-form.fxml`)
- Wizard de 3 pasos
  - Paso 1: Fecha y hora
  - Paso 2: Detalles (propósito, número de personas)
  - Paso 3: Confirmación y resumen
- Validación de disponibilidad
- Generación de código QR

### 6. **Mis Reservas** (`mis-reservas.fxml`)
- Listado de reservas del usuario
- Filtros: Todas, Activas, Completadas, Canceladas
- Visualización de código QR
- Acciones: Ver detalle, Cancelar, Descargar

### 7. **Admin - Gestión de Espacios** (`admin-espacios.fxml`)
- TableView con todas las columnas
- Búsqueda y filtros avanzados
- CRUD completo (Crear, Editar, Eliminar)
- Paginación

### 8. **Admin - Reportes** (`admin-reportes.fxml`)
- 4 KPIs con gradientes
- Gráfico de barras (Reservas por mes)
- Gráfico circular (Espacios más populares)
- Tabla de últimas reservas
- Exportación a Excel

---

## 🎨 Sistema de Diseño

### Paleta de Colores

| Color | Hexadecimal | Uso |
|-------|-------------|-----|
| **Navy Blue** | `#002855` | Principal - Navbar, botones primarios |
| **Blue** | `#0066cc` | Secundario - Enlaces, acentos |
| **Gold** | `#d4af37` | Acento - CTAs importantes |
| **Success** | `#10b981` | Confirmaciones, estados positivos |
| **Warning** | `#f59e0b` | Advertencias, pendientes |
| **Error** | `#ef4444` | Errores, cancelaciones |
| **Info** | `#3b82f6` | Información general |

### Tipografía

- **Títulos**: Playfair Display (Serif) - 48px, 36px, 24px
- **Texto**: Inter (Sans-serif) - 16px, 14px, 12px

### Espaciado

Sistema basado en múltiplos de 4px:
- XS: 4px
- SM: 8px
- MD: 16px (base)
- LG: 24px
- XL: 32px
- 2XL: 48px
- 3XL: 64px

**Ver documentación completa:** [Sistema de Diseño](./GUIA_IMPLEMENTACION_DISEÑO.md)

---

## 👥 Funcionalidades por Rol

### 🔵 Usuario Regular (`ROLE_USER`)

- ✅ Login con email/contraseña
- ✅ Ver catálogo de espacios disponibles
- ✅ Buscar y filtrar espacios
- ✅ Ver detalles de espacios
- ✅ Crear nuevas reservas
- ✅ Ver mis reservas
- ✅ Visualizar código QR de reserva
- ✅ Cancelar mis reservas
- ✅ Dejar reseñas de espacios

### 🔴 Administrador (`ROLE_ADMIN`)

Todo lo anterior más:

- ✅ **Gestión de Espacios**: CRUD completo
- ✅ **Gestión de Reservas**: Ver, editar y cancelar todas las reservas
- ✅ **Validación QR**: Escanear y validar códigos QR
- ✅ **Gestión de Usuarios**: CRUD de usuarios
- ✅ **Reportes y Estadísticas**: Visualización completa
- ✅ **Exportar Datos**: Excel, PDF

---

## 🌐 API Endpoints

### Autenticación
```
POST   /api/auth/login          - Login (email + password)
POST   /api/auth/validate       - Validar token JWT
GET    /api/auth/me             - Datos del usuario actual
POST   /api/auth/logout         - Cerrar sesión
```

### Espacios
```
GET    /api/spaces              - Listar todos los espacios
GET    /api/spaces/active       - Listar espacios activos
GET    /api/spaces/{id}         - Obtener espacio por ID
GET    /api/spaces/search       - Buscar espacios
POST   /api/spaces              - Crear espacio (admin)
PUT    /api/spaces/{id}         - Actualizar espacio (admin)
DELETE /api/spaces/{id}         - Eliminar espacio (admin)
```

### Reservas
```
GET    /api/reservations             - Listar todas (admin)
GET    /api/reservations/user/{id}   - Mis reservas
GET    /api/reservations/{id}        - Obtener por ID
GET    /api/reservations/status/{s}  - Filtrar por estado
POST   /api/reservations             - Crear reserva
PUT    /api/reservations/{id}        - Actualizar reserva
DELETE /api/reservations/{id}        - Cancelar reserva
GET    /api/reservations/{id}/qr     - Obtener código QR
POST   /api/reservations/{id}/validate-qr - Validar QR
```

### Reseñas
```
GET    /api/reviews/space/{id}  - Reseñas de un espacio
POST   /api/reviews             - Crear reseña
PUT    /api/reviews/{id}        - Actualizar reseña
DELETE /api/reviews/{id}        - Eliminar reseña
```

### Usuarios
```
GET    /api/users        - Listar usuarios (admin)
GET    /api/users/{id}   - Obtener usuario por ID (admin)
POST   /api/users        - Crear usuario (admin)
PUT    /api/users/{id}   - Actualizar usuario (admin)
DELETE /api/users/{id}   - Eliminar usuario (admin)
```

---

## 📖 Documentación Adicional

- **[Guía de Implementación del Diseño](./GUIA_IMPLEMENTACION_DISEÑO.md)** - Sistema de diseño completo con ejemplos de código
- **[Estructura del Proyecto](./ESTRUCTURA.md)** - Detalles de la arquitectura
- **[Inicio Rápido](./INICIO_RAPIDO.md)** - Guía de inicio rápido
- **[Documentación FXML](./README-FXML.md)** - Guía de archivos FXML

---

## 🧪 Testing

```bash
# Ejecutar tests
mvn test

# Ejecutar con cobertura
mvn verify
```

---

## 📝 TODO - Próximas Implementaciones

- [ ] Implementar controllers Java para cada pantalla
- [ ] Conectar con API Backend real
- [ ] Implementar generación de códigos QR
- [ ] Añadir validación de formularios
- [ ] Implementar paginación en listados
- [ ] Añadir caché local de datos
- [ ] Implementar modo offline limitado
- [ ] Añadir notificaciones push
- [ ] Implementar impresión de reportes
- [ ] Tests unitarios y de integración

---

## 🤝 Contribución

1. Fork el proyecto
2. Crear rama feature (`git checkout -b feature/AmazingFeature`)
3. Commit cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abrir Pull Request

---

## 📄 Licencia

Este proyecto es desarrollado para fines educativos en el curso de Programación 3.

---

## 👨‍💻 Autores

**Equipo de Desarrollo**
- Universidad Nacional de Costa Rica
- Curso: Programación 3
- Proyecto: Sistema de Reservas Municipales

---

## 📧 Contacto

Para consultas o soporte:
- 📧 Email: reservas@perezzeleton.go.cr
- 🌐 Web: https://www.perezzeleton.go.cr
- 📞 Tel: +506 2771-2000

---

**Última actualización:** Octubre 2025  
**Versión:** 2.0


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
