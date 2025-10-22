# 🚀 INICIO RÁPIDO - Frontend JavaFX

## ✅ Requisitos Previos

1. ✅ **JDK 21** instalado
2. ✅ **Maven 3.8+** instalado
3. ✅ **API Backend** corriendo en `http://localhost:8080`
4. ✅ **PostgreSQL** con base de datos inicializada

## 🏃 Ejecutar Aplicación

### Windows
```cmd
run.bat
```

### Línea de comandos (cualquier OS)
```bash
mvn javafx:run
```

## 📦 Generar JAR Ejecutable

```bash
mvn clean package
```

El JAR estará en: `target/reservas-frontend-javafx-1.0-SNAPSHOT.jar`

## 🔑 Credenciales de Prueba

Usa las credenciales que hayas creado en la API:

**Usuario Regular:**
- Email: `usuario@test.com`
- Password: `password123`

**Administrador:**
- Email: `admin@test.com`
- Password: `admin123`

## 📁 Estructura del Proyecto

```
reservas-frontend-javafx/
├── src/main/java/cr/una/reservas/frontend/
│   ├── App.java                    # Punto de entrada
│   ├── data/ApiClient.java         # Cliente HTTP (módulo red aislado)
│   ├── domain/                     # DTOs (User, Space, Reservation, etc.)
│   ├── viewmodel/                  # MVVM - Lógica de pantalla
│   └── ui/                         # Vistas JavaFX
│       ├── login/
│       ├── main/
│       ├── spaces/
│       ├── reservations/
│       ├── admin/
│       └── reports/
└── src/main/resources/
    └── application.properties      # Configuración
```

## 🎯 Funcionalidades Implementadas

### ✅ Pantalla Login
- Login con email/password
- Validación de campos
- Manejo de errores
- Redirección automática después de login exitoso

### ✅ Ventana Principal
- Tabs según rol de usuario
- Header con información de usuario
- Cerrar sesión

### ✅ Gestión de Espacios (todos los usuarios)
- Listado de espacios disponibles
- Búsqueda por nombre/tipo/ubicación
- Ver detalles de espacio
- Crear reserva

### ✅ Mis Reservas (todos los usuarios)
- Ver mis reservas
- Filtrar por estado
- Ver detalles
- Ver código QR
- Cancelar reserva

### ✅ Panel de Administración (solo ADMIN)
- Gestión de espacios (CRUD)
- Gestión de reservas
- Gestión de usuarios

### ✅ Reportes (solo ADMIN)
- Reservas por estado
- Espacios más reservados
- Ingresos por período
- Ocupación de espacios

## 🔧 Configuración de API

Edita `src/main/resources/application.properties`:

```properties
api.base.url=http://localhost:8080
api.timeout.seconds=30
```

O directamente en `App.java`:

```java
ApiClient.initialize("http://localhost:8080");
```

## 📚 Documentación

- **README.md**: Documentación completa
- **ESTRUCTURA.md**: Arquitectura detallada y diagramas
- Este archivo: Inicio rápido

## 🐛 Solución de Problemas

### Error: "Connection refused"
- ✅ Verifica que la API esté corriendo en `http://localhost:8080`
- ✅ Prueba con: `curl http://localhost:8080/actuator/health`

### Error: "Unauthorized" o "Forbidden"
- ✅ Verifica credenciales de login
- ✅ Revisa que el token JWT esté siendo enviado correctamente

### Error: "Module not found"
- ✅ Ejecuta: `mvn clean install`
- ✅ Verifica que todas las dependencias se descargaron correctamente

## 📞 Soporte

Para problemas o preguntas:
1. Revisa la documentación en `README.md`
2. Revisa la arquitectura en `ESTRUCTURA.md`
3. Consulta los logs de la aplicación

---

**Proyecto desarrollado para Programación 3 - Universidad Nacional de Costa Rica**
