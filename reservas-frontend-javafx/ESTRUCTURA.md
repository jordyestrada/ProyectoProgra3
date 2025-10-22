# Estructura del Proyecto - Frontend JavaFX

## 📁 Árbol de Directorios

```
reservas-frontend-javafx/
│
├── 📄 pom.xml                                    # Configuración Maven
├── 📄 README.md                                  # Documentación principal
├── 📄 .gitignore                                 # Archivos ignorados por Git
├── 📄 run.bat                                    # Script de inicio rápido (Windows)
│
└── src/
    └── main/
        ├── java/
        │   ├── 📄 module-info.java              # Definición de módulo Java
        │   │
        │   └── cr/una/reservas/frontend/
        │       │
        │       ├── 📄 App.java                  # ★ PUNTO DE ENTRADA
        │       │
        │       ├── 📂 data/                     # ═══════════════════════════
        │       │   │                            # MÓDULO DE RED (AISLADO)
        │       │   └── 📄 ApiClient.java        # Cliente HTTP REST
        │       │                                # - GET, POST, PUT, DELETE
        │       │                                # - Gestión de JWT token
        │       │                                # - Manejo de errores HTTP
        │       │                                # - ApiResponse<T>
        │       │
        │       ├── 📂 domain/                   # ═══════════════════════════
        │       │   │                            # DTOs (OBJETOS DE DATOS)
        │       │   ├── 📄 User.java
        │       │   ├── 📄 Space.java
        │       │   ├── 📄 Reservation.java
        │       │   ├── 📄 Review.java
        │       │   ├── 📄 JwtResponse.java
        │       │   └── 📄 LoginRequest.java
        │       │
        │       ├── 📂 viewmodel/                # ═══════════════════════════
        │       │   │                            # MVVM - LÓGICA DE PANTALLA
        │       │   └── 📄 LoginViewModel.java   # (sin código de red directa)
        │       │                                # - Properties para binding
        │       │                                # - Llamadas async a ApiClient
        │       │                                # - Estado de UI
        │       │
        │       └── 📂 ui/                       # ═══════════════════════════
        │           │                            # VISTAS JAVAFX
        │           │
        │           ├── 📂 login/
        │           │   └── 📄 LoginView.java    # Pantalla de login
        │           │
        │           ├── 📂 main/
        │           │   └── 📄 MainView.java     # Ventana principal (tabs)
        │           │
        │           ├── 📂 spaces/
        │           │   └── 📄 SpacesView.java   # Listado de espacios
        │           │
        │           ├── 📂 reservations/
        │           │   └── 📄 ReservationsView.java  # Mis reservas
        │           │
        │           ├── 📂 admin/
        │           │   └── 📄 AdminView.java    # Panel administración
        │           │                            # (solo ROLE_ADMIN)
        │           │
        │           └── 📂 reports/
        │               └── 📄 ReportsView.java  # Reportes y estadísticas
        │                                        # (solo ROLE_ADMIN)
        │
        └── resources/
            ├── 📄 application.properties        # Configuración
            │
            ├── 📂 fxml/                         # (vacío - para futuro)
            ├── 📂 css/                          # (vacío - para estilos)
            └── 📂 icons/                        # (vacío - para iconos)
```

## 🏗️ Arquitectura MVVM

```
┌──────────────┐
│     VIEW     │ (JavaFX UI)
│  LoginView   │ - TextField, Button, Label
│              │ - Binding bidireccional
└──────┬───────┘
       │ binding
       ↓
┌──────────────┐
│  VIEWMODEL   │ (Lógica de pantalla)
│ LoginVM      │ - Properties (email, password, error)
│              │ - Método login()
└──────┬───────┘
       │ llama
       ↓
┌──────────────┐
│   ApiClient  │ (Módulo de red aislado)
│              │ - post("/api/auth/login", ...)
│              │ - Maneja JWT token
└──────┬───────┘
       │ HTTP
       ↓
┌──────────────┐
│  API REST    │ (Backend Spring Boot)
│ :8080        │ - reservas-municipales
└──────────────┘
```

## 🎯 Flujo de Datos

### 1. Login
```
Usuario → LoginView → LoginViewModel → ApiClient → API REST
                                                      ↓
                                              JwtResponse + Token
                                                      ↓
Usuario ← MainView ← LoginViewModel ← ApiClient ← [guarda token]
```

### 2. Listar Espacios
```
Usuario → SpacesView → SpacesViewModel → ApiClient → GET /api/spaces/active
                                                            ↓
                                                      List<Space>
                                                            ↓
Usuario ← [actualiza tabla] ← SpacesViewModel ← ApiClient
```

### 3. Crear Reserva
```
Usuario → formulario → ReservationViewModel → ApiClient → POST /api/reservations
                                                                    ↓
                                                           ReservationDto
                                                                    ↓
Usuario ← [muestra confirmación] ← ReservationViewModel ← ApiClient
```

## 🔐 Gestión de Autenticación

```java
// 1. Login exitoso
JwtResponse response = ApiClient.getInstance()
    .post("/api/auth/login", loginRequest, JwtResponse.class)
    .getData();

// 2. Guardar token para requests futuros
ApiClient.setAuthToken(response.getToken());

// 3. Todas las llamadas posteriores incluyen el token automáticamente
// ApiClient agrega header: Authorization: Bearer {token}
```

## 📊 Roles y Permisos

### ROLE_USER (Usuario Regular)
- ✅ Login
- ✅ Ver espacios disponibles
- ✅ Buscar espacios
- ✅ Crear reserva
- ✅ Ver mis reservas
- ✅ Ver código QR
- ✅ Cancelar reserva

### ROLE_ADMIN (Administrador)
- ✅ Todo lo anterior +
- ✅ Gestión de espacios (CRUD completo)
- ✅ Ver todas las reservas
- ✅ Validar códigos QR
- ✅ Gestión de usuarios
- ✅ Reportes del sistema

## 🚀 Cómo Ejecutar

### Opción 1: Script rápido
```bash
run.bat
```

### Opción 2: Maven
```bash
# Compilar
mvn clean compile

# Ejecutar
mvn javafx:run
```

### Opción 3: JAR ejecutable
```bash
# Generar JAR
mvn clean package

# Ejecutar JAR
java -jar target/reservas-frontend-javafx-1.0-SNAPSHOT.jar
```

## 🔌 Endpoints API Consumidos

Ver `README.md` para lista completa de endpoints.

## 📝 Próximos Pasos de Desarrollo

1. **Implementar carga real de datos**
   - Conectar SpacesView con GET /api/spaces/active
   - Conectar ReservationsView con GET /api/reservations/user/{id}

2. **Formularios de creación**
   - Diálogo de nueva reserva
   - Formulario de creación de espacio (admin)

3. **Visualización QR**
   - Mostrar código QR de reserva
   - Validar QR escaneado

4. **Reportes**
   - Implementar generación de reportes
   - Exportar a PDF/Excel

5. **Mejoras UI**
   - Añadir CSS personalizado
   - Iconos y mejoras visuales
   - Animaciones y transiciones
