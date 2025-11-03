# 📖 Manual de Usuario - Sistema de Reservas Municipales

## 🎯 Introducción

Bienvenido al Sistema de Reservas Municipales, una plataforma web que permite a los ciudadanos reservar espacios públicos como parques, salones comunales y campos deportivos de manera fácil y eficiente.

Este manual te guiará paso a paso en el uso del sistema, desde tu primer inicio de sesión hasta completar reservas y dejar reseñas.

---

## 📋 Índice

1. [¿Qué es el Sistema de Reservas Municipales?](#qué-es-el-sistema-de-reservas-municipales)
2. [Primeros Pasos](#primeros-pasos)
3. [Funciones por Tipo de Usuario](#funciones-por-tipo-de-usuario)
4. [Guía de Uso](#guía-de-uso)
   - [Iniciar Sesión](#iniciar-sesión)
   - [Buscar Espacios Disponibles](#buscar-espacios-disponibles)
   - [Crear una Reserva](#crear-una-reserva)
   - [Gestionar mis Reservas](#gestionar-mis-reservas)
   - [Cancelar una Reserva](#cancelar-una-reserva)
   - [Dejar una Reseña](#dejar-una-reseña)
5. [Preguntas Frecuentes](#preguntas-frecuentes)

---

## 🌟 ¿Qué es el Sistema de Reservas Municipales?

Es una plataforma digital que permite:

- ✅ **Consultar** espacios públicos disponibles (parques, salones, campos deportivos)
- ✅ **Reservar** espacios para eventos o actividades
- ✅ **Gestionar** tus reservas (ver, modificar, cancelar)
- ✅ **Recibir códigos QR** para validar tu asistencia
- ✅ **Consultar el clima** para espacios al aire libre
- ✅ **Dejar reseñas** sobre tu experiencia
- ✅ **Exportar** un historial de tus reservas en Excel

---

## 🚀 Primeros Pasos

### Requisitos Previos

Para usar el sistema necesitas:

1. **Acceso a Internet** y un navegador web (Chrome, Firefox, Edge, Safari)
2. **Credenciales de acceso** (email y contraseña proporcionados por la municipalidad)
3. **Correo electrónico activo** para recibir notificaciones

### Acceso al Sistema

El sistema está disponible en: `http://localhost:8080` (durante pruebas)

---

## 👥 Funciones por Tipo de Usuario

El sistema maneja tres tipos de usuarios con diferentes permisos:

### 🔵 Usuario Regular (ROLE_USER)
**Permisos:**
- Ver espacios disponibles
- Crear y gestionar tus propias reservas
- Cancelar reservas con 24 horas de anticipación
- Descargar tu historial de reservas en Excel
- Dejar reseñas de espacios que has utilizado
- Ver códigos QR de tus reservas

### 🟢 Supervisor (ROLE_SUPERVISOR)
**Permisos adicionales:**
- Ver todas las reservas del sistema
- Gestionar horarios de espacios
- Acceder al panel de métricas
- Validar códigos QR de asistencia
- Exportar datos de cualquier usuario

### 🔴 Administrador (ROLE_ADMIN)
**Permisos completos:**
- Todos los permisos anteriores, más:
- Crear, editar y eliminar espacios
- Cambiar roles de usuarios
- Cancelar reservas sin restricciones de tiempo
- Eliminar reseñas
- Ver métricas completas del sistema

---

## 📚 Guía de Uso

### 1️⃣ Iniciar Sesión

**Paso a paso:**

1. Abre tu navegador y accede al sistema
2. Ingresa tu correo electrónico y contraseña
3. Haz clic en "Iniciar Sesión"

**Credenciales de ejemplo:**
- **Usuario regular:** `user@test.com` / `testpass`
- **Supervisor:** `supervisor@test.com` / `testpass`
- **Administrador:** `admin@test.com` / `testpass`

**¿Qué obtienes al iniciar sesión?**

Recibirás un **token de acceso** que el sistema utilizará automáticamente para todas tus acciones. Este token es válido por un tiempo limitado (generalmente 24 horas).

**💡 Consejo:** Si no puedes iniciar sesión, verifica que tu email esté escrito correctamente y que tu cuenta esté activa.

---

### 2️⃣ Buscar Espacios Disponibles

Una vez dentro del sistema, puedes buscar espacios de varias formas:

#### **Búsqueda Simple**
Ver todos los espacios disponibles en el sistema.

#### **Búsqueda por Nombre**
Buscar por palabras clave, por ejemplo: "parque", "salón", "cancha".

#### **Búsqueda por Capacidad**
Filtrar espacios que puedan albergar un número específico de personas:
- Ejemplo: Espacios con capacidad entre 50 y 200 personas

#### **Búsqueda por Tipo**
Filtrar por categoría:
- **Parques** (espacios verdes, áreas de juego)
- **Salones Comunales** (eventos bajo techo)
- **Campos Deportivos** (canchas, áreas deportivas)

#### **Búsqueda por Ubicación**
Filtrar por zona o dirección específica.

#### **Búsqueda por Disponibilidad en Fechas**
La búsqueda más útil: ver qué espacios están libres en un rango de fechas/horas específico.

**Ejemplo práctico:**
```
Quiero reservar para el 20 de octubre de 2025
Desde las 2:00 PM hasta las 6:00 PM
Con capacidad mínima de 50 personas
```

El sistema te mostrará solo los espacios que:
- ✅ Estén disponibles en ese horario
- ✅ Tengan capacidad para 50+ personas
- ✅ No tengan otras reservas en conflicto

**💡 Consejo:** Usa la búsqueda por disponibilidad para evitar conflictos y ahorrar tiempo.

---

### 3️⃣ Crear una Reserva

Una vez que encuentres el espacio perfecto:

**Paso a paso:**

1. **Selecciona el espacio** que deseas reservar
2. **Elige la fecha y hora:**
   - Fecha de inicio (ejemplo: 25/10/2025 14:00)
   - Fecha de fin (ejemplo: 25/10/2025 16:00)
3. **Verifica el monto** (el sistema lo calcula automáticamente)
4. **Confirma la reserva**

**Información que necesitas proporcionar:**
- ID del espacio (se obtiene de la búsqueda)
- Fecha y hora de inicio
- Fecha y hora de finalización
- Tu ID de usuario (se obtiene automáticamente al estar logueado)

**Estados de una reserva:**

- 🟡 **PENDING (Pendiente):** Recién creada, esperando confirmación
- 🟢 **CONFIRMED (Confirmada):** Aprobada y lista para usar
- 🔴 **CANCELLED (Cancelada):** Reserva cancelada
- ⚫ **COMPLETED (Completada):** Reserva utilizada correctamente

**⚠️ Importante:**
- Si no confirmas tu reserva antes de la hora de inicio, el sistema la cancelará automáticamente
- El sistema revisa cada 5 minutos las reservas pendientes
- Recibirás notificaciones por correo electrónico

**Restricciones de horario:**
- Algunos espacios tienen horarios específicos de operación
- Solo puedes reservar dentro de esos horarios
- Ejemplo: Un parque puede estar disponible solo de 6:00 AM a 8:00 PM

**💡 Consejo:** Confirma tus reservas lo antes posible para evitar que se cancelen automáticamente.

---

### 4️⃣ Gestionar mis Reservas

Puedes ver y administrar todas tus reservas desde tu panel personal.

#### **Ver todas mis reservas**
Consulta un listado completo con:
- Espacio reservado
- Fecha y hora
- Estado actual
- Monto pagado
- Código QR (si aplica)

#### **Ver reservas por estado**
Filtra tus reservas por:
- Pendientes de confirmación
- Confirmadas
- Canceladas
- Completadas

#### **Ver reservas en un rango de fechas**
Consulta las reservas que tienes entre dos fechas específicas.

**Ejemplo:**
```
Desde: 20/10/2025
Hasta: 30/10/2025
```

#### **Modificar una reserva**
Puedes cambiar:
- La fecha u hora (si el espacio está disponible)
- El estado (de PENDING a CONFIRMED)

**⚠️ Limitaciones:**
- No puedes modificar reservas que ya pasaron
- No puedes cambiar el espacio (debes crear una nueva reserva)
- Los cambios de horario deben respetar la disponibilidad

#### **Descargar mi historial en Excel**
Obtén un archivo Excel con:
- **Hoja 1 "Reservaciones":** Tabla detallada de todas tus reservas
- **Hoja 2 "Resumen":** Estadísticas (total gastado, reservas por estado, etc.)

**Columnas incluidas:**
- ID de reserva
- Nombre del espacio
- Fecha de inicio y fin
- Estado
- Monto total
- Fecha de creación
- Observaciones

**💡 Consejo:** Descarga tu historial regularmente para llevar un control personal de tus gastos.

---

### 5️⃣ Cancelar una Reserva

Si necesitas cancelar una reserva, el sistema tiene reglas específicas:

**Reglas de cancelación:**

1. ⏰ **Con 24 horas de anticipación:**
   - Usuarios regulares pueden cancelar sin problemas
   - Solo debes proporcionar un motivo

2. ⏰ **Con menos de 24 horas:**
   - Solo un **Administrador** puede cancelar
   - Los usuarios regulares recibirán un mensaje de error

3. ❌ **No puedes cancelar:**
   - Reservas que ya están canceladas
   - Reservas completadas (ya pasaron)

**Paso a paso para cancelar:**

1. Ve a "Mis Reservas"
2. Selecciona la reserva que deseas cancelar
3. Haz clic en "Cancelar Reserva"
4. Proporciona un motivo (ejemplo: "No podré asistir por compromisos laborales")
5. Confirma la cancelación

**Ejemplo de motivo:**
```
"Usuario no puede asistir por compromisos laborales"
```

**¿Qué pasa después de cancelar?**
- ✅ El estado cambia a **CANCELLED**
- ✅ El espacio queda disponible para otros usuarios
- ✅ El motivo queda registrado en el sistema
- ✅ Recibes una confirmación por email

**Mensajes de error comunes:**

**Error: Cancelación tardía**
```
"La cancelación debe realizarse con al menos 24 horas de anticipación.
Actualmente faltan 18 horas para la reserva.
Solo un ADMIN puede cancelar con menos anticipación."
```

**Error: Ya cancelada**
```
"Esta reserva ya ha sido cancelada previamente."
```

**💡 Consejo:** Cancela con anticipación para liberar el espacio y permitir que otros usuarios lo aprovechen.

---

### 6️⃣ Dejar una Reseña

Después de usar un espacio, puedes compartir tu experiencia:

**¿Cuándo puedo dejar una reseña?**

✅ **Solo puedes reseñar si:**
- La reserva está en estado **CONFIRMED** o **COMPLETED**
- Ya pasó la fecha de fin de tu reserva (después de usar el espacio)
- Eres el usuario que hizo la reserva
- No has dejado una reseña anteriormente para esa reserva

**Paso a paso:**

1. Ve a "Mis Reservas Completadas"
2. Selecciona la reserva que quieres reseñar
3. Haz clic en "Dejar Reseña"
4. **Califica el espacio:** Selecciona de 1 a 5 estrellas
   - ⭐ 1 estrella: Muy malo
   - ⭐⭐ 2 estrellas: Malo
   - ⭐⭐⭐ 3 estrellas: Regular
   - ⭐⭐⭐⭐ 4 estrellas: Bueno
   - ⭐⭐⭐⭐⭐ 5 estrellas: Excelente
5. **Escribe un comentario** (opcional pero recomendado)
6. Marca si quieres que la reseña sea visible públicamente
7. Envía la reseña

**Ejemplo de reseña:**
```
Calificación: 5/5
Comentario: "Excelente espacio, muy limpio y bien equipado.
El personal fue muy atento y las instalaciones estaban en perfecto estado.
Totalmente recomendado para eventos familiares."
```

**Modificar una reseña:**

Si cambias de opinión, puedes editar tu reseña:
- Cambiar la calificación
- Modificar el comentario
- Cambiar la visibilidad

**Errores comunes:**

**Error: Estado inválido**
```
"Solo se pueden reseñar espacios de reservas confirmadas o completadas.
Estado actual: PENDING"
```

**Error: Reseña anticipada**
```
"Solo se puede reseñar un espacio después de haber usado la reserva.
La reserva finaliza el: 25/10/2025 16:00"
```

**Error: No autorizado**
```
"Solo el usuario que realizó la reserva puede hacer una reseña de este espacio"
```

**Error: Reseña duplicada**
```
"Ya existe una reseña para esta reserva"
```

**💡 Consejo:** Las reseñas ayudan a otros usuarios a elegir el mejor espacio. Sé honesto y constructivo.

---

## ❓ Preguntas Frecuentes

### 1. ¿Cómo obtengo una cuenta?
Contacta con la municipalidad. Un administrador creará tu cuenta y te enviará tus credenciales por correo electrónico.

### 2. ¿Olvidé mi contraseña, qué hago?
Contacta con un administrador del sistema para que restablezca tu contraseña.

### 3. ¿Cuánto cuesta reservar un espacio?
El costo varía según el espacio y la duración. El sistema calcula automáticamente el monto al crear la reserva.

### 4. ¿Puedo reservar el mismo espacio varias veces?
Sí, puedes hacer múltiples reservas siempre que no se traslapen en fechas/horas.

### 5. ¿Qué pasa si llego tarde a mi reserva?
Tu reserva sigue siendo válida durante el horario reservado. Si llegas muy tarde, podrías perder tiempo de uso.

### 6. ¿Puedo transferir mi reserva a otra persona?
No directamente. Debes cancelar tu reserva y la otra persona debe crear una nueva.

### 7. ¿Qué es el código QR?
Es un código único para validar tu asistencia el día de la reserva. El personal del espacio lo escaneará para confirmar tu llegada.

### 8. ¿Puedo ver el clima antes de mi reserva?
Sí, para espacios al aire libre puedes consultar el pronóstico del clima directamente desde el sistema.

### 9. ¿Qué hago si el espacio no está en buenas condiciones?
Repórtalo inmediatamente al personal del lugar y deja una reseña detallada después de tu visita.

### 10. ¿Cuánto tiempo antes debo confirmar mi reserva?
Lo antes posible. Las reservas pendientes se cancelan automáticamente si no se confirman antes de la hora de inicio.

---

## 📞 Soporte y Contacto

Si tienes problemas técnicos o preguntas adicionales:

- 📧 **Email:** soporte@reservasmunicipales.cr
- 📞 **Teléfono:** 2222-3333
- 🕒 **Horario de atención:** Lunes a Viernes, 8:00 AM - 5:00 PM

---

## 📝 Notas Finales

- **Respeta los horarios:** Llega puntual y respeta el tiempo de otros usuarios
- **Cuida las instalaciones:** Deja el espacio en las mismas condiciones que lo encontraste
- **Lee las reglas:** Cada espacio puede tener reglas específicas de uso
- **Sé responsable:** Cancela con anticipación si no podrás asistir

---

**Última actualización:** Noviembre 2025  
**Versión del manual:** 1.0

