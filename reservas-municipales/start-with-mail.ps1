# ============================================
# Script para iniciar la app con notificaciones por email
# ============================================

Write-Host "🚀 Configurando variables SMTP para notificaciones..." -ForegroundColor Cyan

# === Configuración SMTP (MAILTRAP - recomendado para pruebas) ===
# Reemplaza TU_USER y TU_PASS con tus credenciales de Mailtrap
# Obtén tus credenciales en: https://mailtrap.io/inboxes

$env:SPRING_MAIL_HOST='smtp.mailtrap.io'
$env:SPRING_MAIL_PORT='587'
$env:SPRING_MAIL_USERNAME='TU_USER'           # ⚠️ CAMBIAR
$env:SPRING_MAIL_PASSWORD='TU_PASS'           # ⚠️ CAMBIAR

# === Alternativa: Gmail (descomenta si prefieres Gmail) ===
# Necesitas crear un "App Password" en tu cuenta Google
# Tutorial: https://support.google.com/accounts/answer/185833
# 
# $env:SPRING_MAIL_HOST='smtp.gmail.com'
# $env:SPRING_MAIL_PORT='587'
# $env:SPRING_MAIL_USERNAME='tu-cuenta@gmail.com'    # ⚠️ CAMBIAR
# $env:SPRING_MAIL_PASSWORD='TU_APP_PASSWORD'        # ⚠️ CAMBIAR (16 caracteres)

# === Propiedades SMTP (autenticación y TLS) ===
$env:SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH='true'
$env:SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE='true'

# === Configuración de notificaciones ===
$env:APP_NOTIFICATIONS_FROM='no-reply@reservas-municipales.local'
$env:APP_NOTIFICATIONS_ADMIN=''  # Opcional: email del admin para recibir copia de nuevas reservas

# === Perfil de Spring ===
$env:SPRING_PROFILES_ACTIVE='dev'

Write-Host "✅ Variables configuradas:" -ForegroundColor Green
Write-Host "   • SMTP Host: $env:SPRING_MAIL_HOST" -ForegroundColor Gray
Write-Host "   • SMTP Port: $env:SPRING_MAIL_PORT" -ForegroundColor Gray
Write-Host "   • SMTP User: $env:SPRING_MAIL_USERNAME" -ForegroundColor Gray
Write-Host "   • From: $env:APP_NOTIFICATIONS_FROM" -ForegroundColor Gray
Write-Host ""

# === Compilar y ejecutar ===
Write-Host "🔨 Compilando y ejecutando la aplicación..." -ForegroundColor Cyan
.\mvnw.cmd clean spring-boot:run
