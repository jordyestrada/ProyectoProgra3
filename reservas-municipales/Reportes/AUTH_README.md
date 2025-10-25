# Configuración de Autenticación - Azure AD + JWT

## 🔐 Resumen de la Implementación

Se ha implementado un sistema de autenticación híbrido que soporta:
- **Azure AD** (producción) - RF01
- **JWT Tokens** para autorización
- **Autenticación local** (desarrollo/testing)

## 📋 Componentes Implementados

### 1. Dependencias Agregadas (`pom.xml`)
```xml
<!-- JWT Dependencies -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.3</version>
</dependency>

<!-- Azure AD Dependencies -->
<dependency>
    <groupId>com.azure.spring</groupId>
    <artifactId>spring-cloud-azure-starter-active-directory</artifactId>
    <version>5.8.0</version>
</dependency>
```

### 2. Servicios Creados
- `JwtService` - Generación y validación de tokens JWT
- `AzureAdService` - Integración con Microsoft Graph API
- `AuthenticationService` - Lógica principal de autenticación
- `DataInitializationService` - Datos de prueba

### 3. Configuración de Seguridad
- `JwtAuthenticationFilter` - Filtro para interceptar requests
- `DockerSecurityConfig` - Configuración para perfil docker
- `DevSecurityConfig` - Configuración para desarrollo

### 4. Controladores
- `AuthController` - Endpoints de autenticación (`/api/auth/*`)

### 5. DTOs
- `LoginRequest` - Request de login
- `JwtResponse` - Response con token JWT

## 🚀 Configuración de Azure AD

### Variables de Entorno Requeridas

Para **producción** (perfil `docker`):
```bash
AZURE_TENANT_ID=your-tenant-id-from-azure
AZURE_CLIENT_ID=your-application-client-id
AZURE_CLIENT_SECRET=your-client-secret
AZURE_APP_ID_URI=api://your-app-id
JWT_SECRET=your-256-bit-secret-key
```

### Configuración en Azure Portal

1. **Registrar aplicación en Azure AD:**
   ```
   Azure Portal > Azure Active Directory > App registrations > New registration
   ```

2. **Configurar permisos:**
   - Microsoft Graph > User.Read
   - Microsoft Graph > User.ReadBasic.All

3. **Obtener valores:**
   - Tenant ID: Directory (tenant) ID
   - Client ID: Application (client) ID
   - Client Secret: Certificates & secrets > New client secret

## 🔧 Uso de la API

### 1. Login con Azure AD
```bash
POST /api/auth/login
Content-Type: application/json

{
  "azureToken": "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1dCI6..."
}
```

### 2. Login Local (desarrollo)
```bash
POST /api/auth/login
Content-Type: application/json

{
  "email": "admin@test.com",
  "password": "testpass"
}
```

### 3. Response
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "username": "Administrador Test",
  "email": "admin@test.com",
  "roleCode": "ADMIN",
  "expiresIn": 86400000
}
```

### 4. Usar Token en Requests
```bash
GET /api/users
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

## 👥 Usuarios de Prueba

El sistema crea automáticamente estos usuarios:

| Email | Rol | Descripción |
|-------|-----|-------------|
| admin@test.com | ADMIN | Administrador completo |
| supervisor@test.com | SUPERVISOR | Supervisor de espacios |
| user@test.com | USER | Usuario común |

**Password temporal:** `testpass`

## 🧪 Testing

### Validar Token
```bash
POST /api/auth/validate?token=YOUR_JWT_TOKEN
```

### Obtener Usuario Actual
```bash
GET /api/auth/me
Authorization: Bearer YOUR_JWT_TOKEN
```

## 🔒 Seguridad por Roles

### Configuración de Endpoints:
- `/api/auth/**` - Público
- `/api/admin/**` - Solo ADMIN
- `/api/supervisor/**` - SUPERVISOR y ADMIN
- `/api/**` - Autenticado

### Uso en Controladores:
```java
@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/admin-only")
public String adminEndpoint() { ... }

@PreAuthorize("hasAnyRole('SUPERVISOR', 'ADMIN')")
@GetMapping("/supervisor-or-admin")
public String supervisorEndpoint() { ... }
```

## 📝 Próximos Pasos

1. **Configurar Azure AD real** - Reemplazar valores de prueba
2. **Implementar refresh tokens** - Para sesiones largas
3. **Agregar rate limiting** - Prevenir ataques de fuerza bruta
4. **Logging de seguridad** - Auditoría de autenticación
5. **Tests unitarios** - Para todos los servicios de auth

## 🚨 Notas Importantes

- **En desarrollo:** Azure AD está deshabilitado, usar autenticación local
- **En producción:** Configurar variables de entorno de Azure AD
- **Seguridad:** Cambiar JWT secret en producción
- **Base de datos:** Los usuarios deben existir en la BD local para funcionar

## 🐛 Troubleshooting

### Error: "User not found in local database"
- Verificar que el usuario existe en la tabla `app_user`
- Ejecutar el `DataInitializationService` para crear usuarios de prueba

### Error: "Invalid Azure AD token"
- Verificar configuración de Azure AD
- Verificar que el token no haya expirado
- Verificar permisos de la aplicación en Azure

### Error: "JWT signature does not match"
- Verificar que el JWT secret sea consistente
- Verificar que el token no esté corrupto