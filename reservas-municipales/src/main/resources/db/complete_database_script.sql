-- =========================================================
-- SCRIPT COMPLETO - Reservas Municipales
-- Incluye TODOS los cambios implementados hasta ahora
-- Para ejecutar en pgAdmin con PostgreSQL
-- =========================================================

-- Limpiar y recrear desde cero (opcional)
-- DROP SCHEMA IF EXISTS public CASCADE;
-- CREATE SCHEMA public;

-- Extensiones necesarias
CREATE EXTENSION IF NOT EXISTS citext;
CREATE EXTENSION IF NOT EXISTS pgcrypto;

BEGIN;

-- =========================
-- 1) SEGURIDAD Y USUARIOS
-- =========================

-- Tabla de roles
CREATE TABLE IF NOT EXISTS role (
  code       text PRIMARY KEY,
  name       text NOT NULL,
  created_at timestamptz NOT NULL DEFAULT now()
);

-- Tabla de usuarios
CREATE TABLE IF NOT EXISTS app_user (
  user_id       uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  email         citext NOT NULL UNIQUE,
  full_name     text NOT NULL,
  phone         text,
  password_hash text,
  role_code     text NOT NULL,
  active        boolean NOT NULL DEFAULT true,
  created_at    timestamptz NOT NULL DEFAULT now(),
  updated_at    timestamptz NOT NULL DEFAULT now()
);

-- =========================
-- 2) ESPACIOS Y CATÁLOGOS
-- =========================

-- Tipos de espacios
CREATE TABLE IF NOT EXISTS space_type (
  space_type_id smallserial PRIMARY KEY,
  name          text NOT NULL UNIQUE,
  description   text
);

-- Espacios principales
CREATE TABLE IF NOT EXISTS space (
  space_id      uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  name          text NOT NULL UNIQUE,
  space_type_id smallint NOT NULL,
  capacity      int NOT NULL,
  location      text,
  outdoor       boolean NOT NULL DEFAULT false,
  active        boolean NOT NULL DEFAULT true,
  description   text,
  created_at    timestamptz NOT NULL DEFAULT now(),
  updated_at    timestamptz NOT NULL DEFAULT now()
);

-- Características/Features
CREATE TABLE IF NOT EXISTS feature (
  feature_id  smallserial PRIMARY KEY,
  name        text NOT NULL UNIQUE,
  description text
);

-- Relación espacios-características
CREATE TABLE IF NOT EXISTS space_feature (
  space_id   uuid NOT NULL,
  feature_id smallint NOT NULL,
  PRIMARY KEY (space_id, feature_id)
);

-- Imágenes de espacios
CREATE TABLE IF NOT EXISTS space_image (
  image_id   bigserial PRIMARY KEY,
  space_id   uuid NOT NULL,
  url        text NOT NULL,
  main       boolean NOT NULL DEFAULT false,
  ord        int NOT NULL DEFAULT 0,
  created_at timestamptz NOT NULL DEFAULT now()
);

-- Horarios de espacios
CREATE TABLE IF NOT EXISTS space_schedule (
  schedule_id bigserial PRIMARY KEY,
  space_id    uuid NOT NULL,
  weekday     smallint NOT NULL, -- 0=domingo, 1=lunes, ..., 6=sábado
  time_from   time NOT NULL,
  time_to     time NOT NULL
);

-- Cierres temporales de espacios
CREATE TABLE IF NOT EXISTS space_closure (
  closure_id bigserial PRIMARY KEY,
  space_id   uuid NOT NULL,
  reason     text,
  starts_at  timestamptz NOT NULL,
  ends_at    timestamptz NOT NULL
);

-- Tarifas por espacio
CREATE TABLE IF NOT EXISTS space_rate (
  rate_id       bigserial PRIMARY KEY,
  space_id      uuid NOT NULL,
  name          text NOT NULL DEFAULT 'Base',
  unit          text NOT NULL, -- 'HOUR', 'DAY', 'BLOCK'
  block_minutes int NOT NULL DEFAULT 60,
  price         numeric(12,2) NOT NULL,
  currency      char(3) NOT NULL DEFAULT 'CRC',
  applies_from  date NOT NULL DEFAULT current_date,
  applies_to    date,
  active        boolean NOT NULL DEFAULT true,
  created_at    timestamptz NOT NULL DEFAULT now(),
  updated_at    timestamptz NOT NULL DEFAULT now()
);

-- =========================
-- 3) RESERVAS (CON CAMPOS QR)
-- =========================

CREATE TABLE IF NOT EXISTS reservation (
  reservation_id      uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  space_id            uuid NOT NULL,
  user_id             uuid NOT NULL,
  starts_at           timestamptz NOT NULL,
  ends_at             timestamptz NOT NULL,
  status              text NOT NULL,
  cancel_reason       text,
  
  -- Información de precio
  rate_id             bigint,
  total_amount        numeric(12,2),
  currency            char(3) NOT NULL DEFAULT 'CRC',
  
  -- Campos QR y asistencia
  qr_code             text,
  qr_validation_token text,
  attendance_confirmed boolean NOT NULL DEFAULT false,
  attendance_confirmed_at timestamptz,
  confirmed_by_user_id uuid,
  
  -- Timestamps
  created_at          timestamptz NOT NULL DEFAULT now(),
  updated_at          timestamptz NOT NULL DEFAULT now()
);

-- =========================
-- 4) RESEÑAS
-- =========================

CREATE TABLE IF NOT EXISTS review (
  review_id      bigserial PRIMARY KEY,
  space_id       uuid NOT NULL,
  user_id        uuid NOT NULL,
  reservation_id uuid,
  rating         smallint NOT NULL CHECK (rating >= 1 AND rating <= 5),
  comment        text,
  visible        boolean NOT NULL DEFAULT true,
  created_at     timestamptz NOT NULL DEFAULT now(),
  approved_at    timestamptz
);

-- =========================
-- FOREIGN KEYS
-- =========================

-- Usuarios
ALTER TABLE app_user
  ADD CONSTRAINT IF NOT EXISTS fk_app_user_role
  FOREIGN KEY (role_code) REFERENCES role(code) ON DELETE RESTRICT;

-- Espacios
ALTER TABLE space
  ADD CONSTRAINT IF NOT EXISTS fk_space_type
  FOREIGN KEY (space_type_id) REFERENCES space_type(space_type_id);

ALTER TABLE space_feature
  ADD CONSTRAINT IF NOT EXISTS fk_space_feature_space
  FOREIGN KEY (space_id) REFERENCES space(space_id) ON DELETE CASCADE;

ALTER TABLE space_feature
  ADD CONSTRAINT IF NOT EXISTS fk_space_feature_feature
  FOREIGN KEY (feature_id) REFERENCES feature(feature_id) ON DELETE RESTRICT;

ALTER TABLE space_image
  ADD CONSTRAINT IF NOT EXISTS fk_space_image_space
  FOREIGN KEY (space_id) REFERENCES space(space_id) ON DELETE CASCADE;

ALTER TABLE space_schedule
  ADD CONSTRAINT IF NOT EXISTS fk_space_schedule_space
  FOREIGN KEY (space_id) REFERENCES space(space_id) ON DELETE CASCADE;

ALTER TABLE space_closure
  ADD CONSTRAINT IF NOT EXISTS fk_space_closure_space
  FOREIGN KEY (space_id) REFERENCES space(space_id) ON DELETE CASCADE;

ALTER TABLE space_rate
  ADD CONSTRAINT IF NOT EXISTS fk_space_rate_space
  FOREIGN KEY (space_id) REFERENCES space(space_id) ON DELETE CASCADE;

-- Reservas
ALTER TABLE reservation
  ADD CONSTRAINT IF NOT EXISTS fk_reservation_space
  FOREIGN KEY (space_id) REFERENCES space(space_id) ON DELETE RESTRICT;

ALTER TABLE reservation
  ADD CONSTRAINT IF NOT EXISTS fk_reservation_user
  FOREIGN KEY (user_id) REFERENCES app_user(user_id) ON DELETE RESTRICT;

ALTER TABLE reservation
  ADD CONSTRAINT IF NOT EXISTS fk_reservation_rate
  FOREIGN KEY (rate_id) REFERENCES space_rate(rate_id) ON DELETE SET NULL;

ALTER TABLE reservation
  ADD CONSTRAINT IF NOT EXISTS fk_reservation_confirmed_by_user
  FOREIGN KEY (confirmed_by_user_id) REFERENCES app_user(user_id) ON DELETE SET NULL;

-- Reseñas
ALTER TABLE review
  ADD CONSTRAINT IF NOT EXISTS fk_review_space
  FOREIGN KEY (space_id) REFERENCES space(space_id) ON DELETE CASCADE;

ALTER TABLE review
  ADD CONSTRAINT IF NOT EXISTS fk_review_user
  FOREIGN KEY (user_id) REFERENCES app_user(user_id) ON DELETE RESTRICT;

ALTER TABLE review
  ADD CONSTRAINT IF NOT EXISTS fk_review_reservation
  FOREIGN KEY (reservation_id) REFERENCES reservation(reservation_id) ON DELETE SET NULL;

-- =========================
-- ÍNDICES PARA OPTIMIZACIÓN
-- =========================

-- Usuarios
CREATE INDEX IF NOT EXISTS idx_app_user_active ON app_user (active);
CREATE INDEX IF NOT EXISTS idx_app_user_email ON app_user (email);

-- Espacios
CREATE INDEX IF NOT EXISTS idx_space_active ON space (active);
CREATE INDEX IF NOT EXISTS idx_space_type ON space (space_type_id);
CREATE INDEX IF NOT EXISTS idx_space_outdoor ON space (outdoor);
CREATE INDEX IF NOT EXISTS idx_space_location ON space (location);

-- Características
CREATE INDEX IF NOT EXISTS idx_space_image ON space_image (space_id);
CREATE INDEX IF NOT EXISTS idx_space_schedule_weekday ON space_schedule (space_id, weekday);

-- Tarifas
CREATE INDEX IF NOT EXISTS idx_space_rate_active ON space_rate (space_id, active);
CREATE INDEX IF NOT EXISTS idx_space_rate_validity ON space_rate (space_id, applies_from);

-- Reservas (CRÍTICOS PARA BÚSQUEDA AVANZADA)
CREATE INDEX IF NOT EXISTS idx_reservation_space_time ON reservation (space_id, starts_at, ends_at);
CREATE INDEX IF NOT EXISTS idx_reservation_user_time ON reservation (user_id, starts_at);
CREATE INDEX IF NOT EXISTS idx_reservation_status ON reservation (status);
CREATE INDEX IF NOT EXISTS idx_reservation_dates ON reservation (starts_at, ends_at);

-- Índices QR y asistencia
CREATE INDEX IF NOT EXISTS idx_reservation_attendance ON reservation (attendance_confirmed);
CREATE INDEX IF NOT EXISTS idx_reservation_qr_token ON reservation (qr_validation_token);
CREATE INDEX IF NOT EXISTS idx_reservation_confirmed_by ON reservation (confirmed_by_user_id);

-- Reseñas
CREATE INDEX IF NOT EXISTS idx_review_space ON review (space_id);
CREATE INDEX IF NOT EXISTS idx_review_user ON review (user_id);
CREATE UNIQUE INDEX IF NOT EXISTS idx_review_reservation ON review (reservation_id);
CREATE INDEX IF NOT EXISTS idx_review_rating ON review (rating);
CREATE INDEX IF NOT EXISTS idx_review_visible ON review (visible);
CREATE INDEX IF NOT EXISTS idx_review_created ON review (created_at);

-- =========================
-- DATOS DE PRUEBA
-- =========================

-- Roles básicos
INSERT INTO role (code, name) VALUES
('ADMIN', 'Administrador'),
('SUPERVISOR', 'Supervisor'),
('USER', 'Usuario')
ON CONFLICT (code) DO NOTHING;

-- Usuarios de prueba
INSERT INTO app_user (user_id, email, full_name, role_code, password_hash) VALUES
('550e8400-e29b-41d4-a716-446655440000', 'admin@test.com', 'Administrador del Sistema', 'ADMIN', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'),
('550e8400-e29b-41d4-a716-446655440001', 'supervisor@test.com', 'Supervisor Municipal', 'SUPERVISOR', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'),
('550e8400-e29b-41d4-a716-446655440002', 'user@test.com', 'Usuario Regular', 'USER', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy')
ON CONFLICT (email) DO NOTHING;

-- Tipos de espacios
INSERT INTO space_type (space_type_id, name, description) VALUES
(1, 'Parque', 'Espacios recreativos al aire libre'),
(2, 'Salón Comunal', 'Espacios cerrados para eventos'),
(3, 'Campo Deportivo', 'Instalaciones deportivas')
ON CONFLICT (name) DO NOTHING;

-- Espacios de prueba
INSERT INTO space (space_id, name, space_type_id, capacity, location, outdoor, description) VALUES
('21056e13-415e-486c-9fd6-94d5f6af08e8', 'Parque Central', 1, 100, 'Centro de la ciudad', false, 'Parque principal del municipio'),
('21056e13-415e-486c-9fd6-94d5f6af08e9', 'Salón Municipal', 2, 150, 'Edificio Municipal', false, 'Salón para eventos oficiales'),
('21056e13-415e-486c-9fd6-94d5f6af08ea', 'Cancha de Fútbol', 3, 200, 'Complejo Deportivo', true, 'Cancha de fútbol profesional')
ON CONFLICT (name) DO NOTHING;

-- Características
INSERT INTO feature (name, description) VALUES
('WiFi', 'Conexión a internet inalámbrica'),
('Aire Acondicionado', 'Sistema de climatización'),
('Proyector', 'Equipo de proyección'),
('Sonido', 'Sistema de audio'),
('Cocina', 'Área de preparación de alimentos'),
('Estacionamiento', 'Área de parqueo disponible'),
('Piscina', 'Área de natación'),
('Jardín', 'Área verde exterior'),
('Iluminación', 'Sistema de iluminación nocturna'),
('Baños', 'Servicios sanitarios')
ON CONFLICT (name) DO NOTHING;

-- Tarifas básicas
INSERT INTO space_rate (space_id, name, unit, price, currency) VALUES
('21056e13-415e-486c-9fd6-94d5f6af08e8', 'Tarifa Parque', 'HOUR', 2500.00, 'CRC'),
('21056e13-415e-486c-9fd6-94d5f6af08e9', 'Tarifa Salón', 'HOUR', 15000.00, 'CRC'),
('21056e13-415e-486c-9fd6-94d5f6af08ea', 'Tarifa Cancha', 'HOUR', 8000.00, 'CRC')
ON CONFLICT DO NOTHING;

-- Algunas características a espacios
INSERT INTO space_feature (space_id, feature_id) VALUES
('21056e13-415e-486c-9fd6-94d5f6af08e8', 1), -- Parque - WiFi
('21056e13-415e-486c-9fd6-94d5f6af08e8', 6), -- Parque - Estacionamiento
('21056e13-415e-486c-9fd6-94d5f6af08e8', 8), -- Parque - Jardín
('21056e13-415e-486c-9fd6-94d5f6af08e9', 1), -- Salón - WiFi
('21056e13-415e-486c-9fd6-94d5f6af08e9', 2), -- Salón - A/C
('21056e13-415e-486c-9fd6-94d5f6af08e9', 3), -- Salón - Proyector
('21056e13-415e-486c-9fd6-94d5f6af08e9', 4), -- Salón - Sonido
('21056e13-415e-486c-9fd6-94d5f6af08ea', 6), -- Cancha - Estacionamiento
('21056e13-415e-486c-9fd6-94d5f6af08ea', 9)  -- Cancha - Iluminación
ON CONFLICT DO NOTHING;

COMMIT;

-- =========================
-- VERIFICACIÓN FINAL
-- =========================

-- Mostrar resumen de tablas creadas
SELECT 
    'TABLAS CREADAS:' as categoria,
    schemaname,
    tablename,
    '' as conteo
FROM pg_tables 
WHERE schemaname = 'public'

UNION ALL

SELECT 
    'CONTEO DE REGISTROS:' as categoria,
    '' as schemaname,
    tabla,
    registros::text as conteo
FROM (
    SELECT 'role' as tabla, COUNT(*) as registros FROM role
    UNION ALL
    SELECT 'app_user', COUNT(*) FROM app_user
    UNION ALL
    SELECT 'space_type', COUNT(*) FROM space_type
    UNION ALL
    SELECT 'space', COUNT(*) FROM space
    UNION ALL
    SELECT 'feature', COUNT(*) FROM feature
    UNION ALL
    SELECT 'space_rate', COUNT(*) FROM space_rate
    UNION ALL
    SELECT 'space_feature', COUNT(*) FROM space_feature
    UNION ALL
    SELECT 'reservation', COUNT(*) FROM reservation
    UNION ALL
    SELECT 'review', COUNT(*) FROM review
) counts

ORDER BY categoria, tablename;

-- Mostrar estructura de tabla reservation (con campos QR)
SELECT 
    'ESTRUCTURA RESERVATION:' as info,
    column_name,
    data_type,
    is_nullable
FROM information_schema.columns 
WHERE table_name = 'reservation'
  AND table_schema = 'public'
ORDER BY ordinal_position;