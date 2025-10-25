# 🔒 Migración de Passwords a BCrypt

## ⚠️ IMPORTANTE: Ejecutar ANTES de desplegar

El sistema ahora usa **BCrypt** para validar passwords en lugar de texto plano.

---

## 📋 ¿Qué cambió?

### Antes (texto plano):
```java
if (!password.equals(user.getPasswordHash())) {
    throw new AuthenticationException("Invalid credentials");
}
```

### Ahora (BCrypt):
```java
if (!passwordEncoder.matches(password, user.getPasswordHash())) {
    throw new AuthenticationException("Invalid credentials");
}
```

---

## 🚨 Acción Requerida

**Todos los usuarios existentes en la base de datos deben tener sus passwords actualizados a BCrypt.**

---

## 🛠️ Cómo Migrar (Opción Recomendada)

### Paso 1: Conectarse a PostgreSQL

```bash
# Desde PowerShell o CMD
psql -h localhost -p 5433 -U postgres -d reservas_municipales
```

### Paso 2: Ejecutar el script de migración

```sql
-- Copiar y pegar el contenido de migrate-passwords.sql
-- O ejecutar directamente:
\i migrate-passwords.sql
```

### Paso 3: Verificar

```sql
-- Todos los passwords deben tener 60 caracteres
SELECT email, LENGTH(password_hash) as hash_length 
FROM users;

-- Resultado esperado: 60 para todos los usuarios
```

---

## 🔑 Password por Defecto

Después de la migración, **TODOS los usuarios tendrán el password: `admin123`**

### Para producción:
1. Ejecutar la migración
2. Notificar a los usuarios que reseteen su password
3. Implementar endpoint de "Cambiar Password" (si no existe)

---

## ✅ Verificación Post-Migración

### Probar Login:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "cualquier-email@test.com",
    "password": "admin123"
  }'
```

**Debe responder con un token JWT.**

---

## 📊 Estado Actual

| Ambiente | Estado | Acción |
|----------|--------|--------|
| **Dev (local)** | ✅ Listo | `test-data.sql` ya tiene BCrypt |
| **Docker** | ✅ Listo | `test-data.sql` ya tiene BCrypt |
| **Producción** | ⚠️ Pendiente | Ejecutar `migrate-passwords.sql` |

---

## 🔄 Rollback (si algo sale mal)

Si necesitas volver atrás:

```sql
-- Restaurar backup de la tabla users
-- O revertir el commit en git y redesplegar
```

**Recomendación:** Hacer backup de la tabla `users` antes de migrar.

```bash
# Backup antes de migrar
pg_dump -h localhost -p 5433 -U postgres -d reservas_municipales -t users > users_backup.sql
```

---

## 🎯 Checklist de Migración

- [ ] Hacer backup de la tabla `users`
- [ ] Ejecutar `migrate-passwords.sql`
- [ ] Verificar que todos los passwords tienen 60 caracteres
- [ ] Probar login con `admin123`
- [ ] Notificar a usuarios que cambien su password
- [ ] (Opcional) Implementar endpoint de cambio de password

---

## 💡 Generar Nuevos Hashes BCrypt

Si necesitas generar un hash BCrypt para otro password:

### Opción 1: Usar el archivo GenerateHash.java

```bash
cd reservas-municipales
javac -cp ".;target/classes" GenerateHash.java
java -cp ".;target/classes" GenerateHash
```

### Opción 2: Online (NO RECOMENDADO para producción)
- https://bcrypt-generator.com/

### Opción 3: Desde la aplicación
```java
BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
String hash = encoder.encode("mi-nuevo-password");
System.out.println(hash);
```

---

## ⚙️ Alternativa: Migración Automática (Avanzado)

Si quieres que la aplicación migre automáticamente al iniciar:

```java
@Component
public class PasswordMigration implements CommandLineRunner {
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public void run(String... args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        userRepository.findAll().forEach(user -> {
            if (user.getPasswordHash().length() < 60) {
                // Migrar a BCrypt
                user.setPasswordHash(encoder.encode("admin123"));
                userRepository.save(user);
            }
        });
    }
}
```

**⚠️ Esta opción NO es recomendada porque:**
- Se ejecuta cada vez que arranca la app
- Puede causar problemas de performance
- Mejor hacer la migración una sola vez con SQL

---

## 📞 Soporte

Si tienes problemas durante la migración:
1. Revisar los logs de PostgreSQL
2. Verificar que el script se ejecutó completamente
3. Restaurar desde backup si es necesario

---

**Última actualización:** 24/10/2025
