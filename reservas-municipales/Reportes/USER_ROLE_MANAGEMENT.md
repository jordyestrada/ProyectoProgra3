# Gestión de Roles de Usuario - Documentación Completa

## 📋 Resumen
Se ha implementado un sistema completo para que los **ADMIN** puedan cambiar los roles de los usuarios y enviar notificaciones automáticas por correo electrónico.

## 🎯 Funcionalidad Implementada

### 1. Cambio de Rol por ADMIN
- Solo usuarios con rol **ROLE_ADMIN** pueden cambiar roles
- Se puede cambiar a: **ROLE_ADMIN**, **ROLE_SUPERVISOR** o **ROLE_USER**
- El sistema valida que el rol exista y sea válido
- No permite cambiar a un rol que el usuario ya tiene

### 2. Notificación Automática por Email
- Se envía un correo automático al usuario cuando cambia su rol
- Email con diseño HTML profesional y responsive
- Incluye:
  - Rol anterior y nuevo rol
  - Lista detallada de permisos del nuevo rol
  - Emoji distintivo según el rol (👑 Admin, ⭐ Supervisor, 👤 User)
  - Colores personalizados por rol

### 3. Seguridad
- Endpoint protegido con `@PreAuthorize("hasRole('ADMIN')")`
- Validación con Jakarta Validation
- Logs detallados de todas las operaciones
- Manejo robusto de errores

---

## 🔌 Endpoint API

### PATCH `/api/users/change-role`

**Autorización:** Solo ROLE_ADMIN

**Request Body:**
```json
{
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "roleCode": "ROLE_ADMIN"
}
```

**Roles válidos:**
- `ROLE_ADMIN` - Administrador con permisos completos
- `ROLE_SUPERVISOR` - Supervisor con permisos de gestión
- `ROLE_USER` - Usuario regular con permisos básicos

---

## ✅ Respuestas del API

### Respuesta Exitosa (200 OK)
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

### Errores Comunes

**Usuario no encontrado (400 Bad Request):**
```json
{
    "error": "Error al cambiar rol",
    "message": "Usuario no encontrado con ID: 550e8400-e29b-41d4-a716-446655440000"
}
```

**Rol inválido (400 Bad Request):**
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

---

## 📧 Email de Notificación

El email incluye:

### Header personalizado
- Color según el rol (Rojo para Admin, Naranja para Supervisor, Azul para User)
- Emoji distintivo (👑 ⭐ 👤)
- Gradiente de colores

### Contenido
- Saludo personalizado con el nombre del usuario
- Comparación visual del rol anterior vs nuevo
- Lista completa de permisos del nuevo rol
- Instrucción para cerrar sesión y volver a iniciar

### Permisos por Rol

**ROLE_ADMIN (👑):**
- Gestión completa de usuarios y roles
- Administración de espacios y horarios
- Gestión total de reservas
- Acceso a dashboard y métricas
- Cancelación sin restricciones de tiempo
- Exportación de datos de cualquier usuario

**ROLE_SUPERVISOR (⭐):**
- Visualización y gestión de reservas
- Gestión de horarios de espacios
- Acceso a dashboard y métricas
- Exportación de datos de usuarios
- Validación de códigos QR

**ROLE_USER (👤):**
- Crear y gestionar sus propias reservas
- Consultar espacios disponibles
- Crear reseñas de espacios utilizados
- Exportar sus propias reservas
- Ver y usar códigos QR de sus reservas

---

## 🧪 Ejemplo de Uso con cURL

```bash
# 1. Login como admin
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@test.com",
    "password": "testpass"
  }'

# Respuesta: { "token": "eyJhbGciOiJIUzI1NiJ9..." }

# 2. Cambiar rol de usuario
curl -X PATCH http://localhost:8080/api/users/change-role \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -d '{
    "userId": "550e8400-e29b-41d4-a716-446655440002",
    "roleCode": "ROLE_SUPERVISOR"
  }'
```

---

## 📁 Archivos Modificados/Creados

### Nuevos archivos:
1. **ChangeRoleRequest.java** - DTO para validar el request
   - Valida userId y roleCode
   - Usa Jakarta Validation

### Archivos modificados:
1. **NotificationType.java** - Agregado `USER_ROLE_CHANGED`
2. **EmailNotificationSender.java** 
   - Método `buildHtmlRoleChanged()` con template HTML
   - Método `getPermissionsHtml()` para listar permisos
   - Método `darkenColor()` para gradientes
3. **UserService.java**
   - Método `changeUserRole()` con lógica de negocio
   - Envío de notificación automática
   - Logs detallados
4. **UserController.java**
   - Endpoint `PATCH /api/users/change-role`
   - Protegido con `@PreAuthorize("hasRole('ADMIN')")`
   - Manejo de errores
5. **README.md** - Documentación completa del endpoint

---

## 🔒 Seguridad y Validaciones

### Validaciones implementadas:
- ✅ Solo ADMIN puede cambiar roles
- ✅ El usuario debe existir
- ✅ El rol debe existir y ser válido
- ✅ No permite cambiar a un rol que ya tiene
- ✅ Validación de formato del roleCode con regex
- ✅ Transacción atómica (@Transactional)

### Logs de auditoría:
```
INFO: Admin admin@test.com solicitando cambio de rol para usuario 550e... a ROLE_SUPERVISOR
INFO: Rol del usuario user@test.com cambiado de ROLE_USER a ROLE_SUPERVISOR
INFO: Notificación de cambio de rol enviada a user@test.com
```

---

## 🎨 Características del Email

### Diseño:
- ✅ HTML responsive (mobile-friendly)
- ✅ Compatible con clientes de correo modernos
- ✅ Colores personalizados según el rol
- ✅ Emojis para identificación visual rápida
- ✅ Gradientes y sombras sutiles
- ✅ Tabla comparativa antes/después
- ✅ Footer con copyright

### Comportamiento:
- ✅ Si falla el envío del email, el cambio de rol **SÍ se completa**
- ✅ Error del email se registra en logs pero no revierte la transacción
- ✅ Manejo de excepciones robusto

---

## 🚀 Cómo Probar

1. **Iniciar la aplicación:**
   ```powershell
   .\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
   ```

2. **Login como admin:**
   ```
   POST http://localhost:8080/api/auth/login
   {
       "email": "admin@test.com",
       "password": "testpass"
   }
   ```

3. **Cambiar rol de un usuario:**
   ```
   PATCH http://localhost:8080/api/users/change-role
   Authorization: Bearer [token_admin]
   {
       "userId": "550e8400-e29b-41d4-a716-446655440002",
       "roleCode": "ROLE_SUPERVISOR"
   }
   ```

4. **Verificar el email:**
   - Revisar la bandeja del usuario
   - El email debe tener el asunto: "Tu rol en el sistema ha sido actualizado"
   - Debe incluir los detalles del cambio

---

## 📊 Estadísticas de Implementación

- **Archivos creados:** 1
- **Archivos modificados:** 5
- **Líneas de código agregadas:** ~350
- **Tiempo de compilación:** ✅ Exitoso
- **Compatibilidad:** Spring Boot 3.x, Java 21

---

## ✨ Mejoras Futuras (Opcionales)

1. **Historial de cambios de rol** - Tabla audit para registro
2. **Notificación al admin** - CC al admin cuando cambia un rol
3. **Bulk operations** - Cambiar rol a múltiples usuarios a la vez
4. **UI en frontend** - Interfaz gráfica para gestión de roles
5. **Confirmación por email** - Usuario debe confirmar el cambio

---

## 🐛 Solución de Problemas

### El email no llega:
1. Verificar configuración de SMTP en `application-dev.properties`
2. Revisar logs: `ERROR: Failed to send email`
3. El cambio de rol **SÍ se aplica** aunque falle el email

### Error 403 Forbidden:
- Usuario no tiene rol ADMIN
- Token expirado o inválido
- Verificar header: `Authorization: Bearer [token]`

### Error 400 Bad Request:
- UUID inválido
- RoleCode no válido (debe ser ROLE_ADMIN, ROLE_SUPERVISOR o ROLE_USER)
- Usuario o rol no existe

---

## 📞 Soporte

Para problemas o dudas, revisar:
- Logs de la aplicación
- README.md principal
- Documentación de Spring Security
