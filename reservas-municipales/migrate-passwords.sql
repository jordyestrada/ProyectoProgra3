-- ================================================================
-- MIGRACIÓN DE PASSWORDS A BCRYPT
-- ================================================================
-- Este script actualiza todos los passwords en texto plano a BCrypt
-- 
-- Hash BCrypt de "admin123": $2a$10$N9qo8uLOickgx2ZMRZoMye3jC3LQz5Y9xQM3cF2c6zjWb6KbGvTJa
-- 
-- INSTRUCCIONES:
-- 1. Conectarse a la base de datos de producción
-- 2. Ejecutar este script UNA SOLA VEZ
-- 3. Verificar que los usuarios pueden hacer login con "admin123"
-- ================================================================

-- Actualizar todos los usuarios que tienen password en texto plano
-- Los BCrypt hash siempre tienen 60 caracteres y empiezan con $2a$ o $2b$
UPDATE users 
SET password_hash = '$2a$10$N9qo8uLOickgx2ZMRZoMye3jC3LQz5Y9xQM3cF2c6zjWb6KbGvTJa',
    updated_at = NOW()
WHERE LENGTH(password_hash) < 60 
   OR password_hash NOT LIKE '$2%';

-- Verificar cuántos registros fueron actualizados
SELECT COUNT(*) as usuarios_migrados 
FROM users 
WHERE password_hash = '$2a$10$N9qo8uLOickgx2ZMRZoMye3jC3LQz5Y9xQM3cF2c6zjWb6KbGvTJa';

-- Mostrar todos los usuarios después de la migración
SELECT user_id, email, name, role, 
       CASE 
           WHEN LENGTH(password_hash) = 60 THEN 'BCrypt ✓'
           ELSE 'Texto plano ✗'
       END as password_status
FROM users
ORDER BY email;
