# Configuración de Gmail para Notificaciones por Email

## ✅ Estado Actual

La aplicación está **completamente configurada** para enviar emails usando Gmail SMTP.

### 📧 Cuenta configurada:
- **Email**: reservas.muni.pz@gmail.com
- **App Password**: `atofhusshbwiboya` (16 caracteres)
- **SMTP**: smtp.gmail.com:587
- **TLS**: Habilitado

## 🚀 Cómo ejecutar

### Opción 1: Usar el script (Recomendado)
```powershell
.\start-with-gmail.ps1
```

### Opción 2: Manual
```powershell
cd reservas-municipales
$env:SPRING_PROFILES_ACTIVE='dev'
.\mvnw.cmd spring-boot:run
```

## 🔧 Configuración en `application-dev.yml`

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: reservas.muni.pz@gmail.com
    password: atofhusshbwiboya  # Gmail App Password
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
            required: true
    default-encoding: UTF-8

app:
  notifications:
    from: reservas.muni.pz@gmail.com  # Debe coincidir con username
    admin: ""  # Opcional: email para copias admin
```

## 📨 Tipos de emails que se envían

1. **Reserva Creada** (`RESERVATION_CREATED`)
   - Asunto: "Reserva creada"
   - Template: `reservation-created.html`
   - Incluye: Espacio, fecha inicio/fin, código QR

2. **Estado Actualizado** (`RESERVATION_STATUS_CHANGED`)
   - Asunto: "Estado de tu reserva actualizado"
   - Template: `reservation-status-changed.html`
   - Incluye: Estado anterior → Estado nuevo

3. **Reserva Cancelada** (`RESERVATION_CANCELLED`)
   - Asunto: "Reserva cancelada"
   - Template: `reservation-cancelled.html`
   - Incluye: Motivo de cancelación

4. **Asistencia Confirmada** (`QR_VALIDATED`)
   - Asunto: "Asistencia confirmada"
   - Template: `qr-validated.html`
   - Incluye: Confirmación de presencia

## 🛠️ Si Gmail bloquea el envío

### Paso 1: Verificar App Password
1. Ir a https://myaccount.google.com/
2. Seguridad → Verificación en dos pasos (debe estar activa)
3. Contraseñas de aplicaciones → Crear nueva
4. Copiar el código de 16 caracteres
5. Actualizar en `application-dev.yml`

### Paso 2: Desbloqueo temporal
Si Gmail sigue bloqueando, abrir este link:
https://accounts.google.com/DisplayUnlockCaptcha

Hacer clic en **"Continuar"** y volver a intentar.

### Paso 3: Verificar configuración de Gmail
1. Entrar a Gmail con la cuenta `reservas.muni.pz@gmail.com`
2. Configuración ⚙️ → Ver toda la configuración
3. Pestaña "Reenvío y correo POP/IMAP"
4. **Habilitar IMAP** (recomendado, no obligatorio para SMTP)

## 🧪 Probar el envío de emails

### Crear una reserva (ejemplo con cURL):
```bash
curl -X POST http://localhost:8080/api/reservations \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "spaceId": "e1234567-89ab-cdef-0123-456789abcdef",
    "userId": "4642dd20-7b80-49d9-be64-7e23103209a2",
    "startsAt": "2025-11-01T10:00:00Z",
    "endsAt": "2025-11-01T12:00:00Z"
  }'
```

### Verificar logs:
```
2025-10-25T21:00:00.000  INFO --- Email sent: type=RESERVATION_CREATED, to=usuario@example.com
```

## 📂 Archivos de plantillas HTML

Las plantillas están en: `src/main/resources/templates/mail/`

- `reservation-created.html` - Diseño azul 🔵
- `reservation-status-changed.html` - Diseño morado 🟣
- `reservation-cancelled.html` - Diseño rojo 🔴
- `qr-validated.html` - Diseño verde 🟢

Todas las plantillas son **responsive** y tienen estilo profesional con gradientes.

## ⚠️ Notas importantes

1. **La App Password NO ES la contraseña normal de Gmail**
2. **Requiere 2FA (Verificación en dos pasos) activa**
3. **El `from` debe coincidir con `spring.mail.username`**
4. **Gmail tiene límite de ~500 emails/día para cuentas gratuitas**
5. **Los emails van directo (no se guardan en BD)**

## 🔍 Troubleshooting

### Error: "Authentication failed"
- Verificar que la App Password sea correcta (16 caracteres)
- Verificar que 2FA esté activo en la cuenta de Gmail
- Intentar generar una nueva App Password

### Error: "Connection timeout"
- Verificar conexión a internet
- Verificar que el puerto 587 no esté bloqueado por firewall
- Intentar con port 465 (SSL) si 587 falla

### Email no llega
- Revisar carpeta de SPAM
- Verificar que el email destino sea válido
- Revisar logs de la aplicación para errores

### Error: "Mail server connection failed"
```
Caused by: javax.mail.MessagingException: Could not connect to SMTP host
```
**Solución**: Verificar conexión a internet y que Gmail SMTP no esté bloqueado por firewall/proxy.

## 📞 Soporte

Si tienes problemas:
1. Revisa los logs de la aplicación
2. Verifica que Docker esté corriendo (PostgreSQL)
3. Verifica la configuración de Gmail
4. Intenta el desbloqueo temporal de Gmail

---

**Última actualización**: 25 de octubre de 2025
**Versión**: 1.0.0
