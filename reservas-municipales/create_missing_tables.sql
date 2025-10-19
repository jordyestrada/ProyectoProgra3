-- =========================================================
-- Script para crear las tablas faltantes en la base de datos
-- Ejecutar en pgAdmin para completar el esquema
-- =========================================================

-- Verificar si las extensiones existen, si no las crea
CREATE EXTENSION IF NOT EXISTS citext;
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ===========================
-- CREAR TABLAS FALTANTES
-- ===========================

-- Tabla de características/features
CREATE TABLE IF NOT EXISTS feature (
  feature_id smallserial PRIMARY KEY,
  name       text NOT NULL UNIQUE,
  description text
);

-- Tabla de imágenes de espacios
CREATE TABLE IF NOT EXISTS space_image (
  image_id   bigserial PRIMARY KEY,
  space_id   uuid NOT NULL,
  url        text NOT NULL,
  main       boolean NOT NULL DEFAULT false,
  ord        int NOT NULL DEFAULT 0,
  created_at timestamptz NOT NULL DEFAULT now(),
  
  CONSTRAINT fk_space_image_space
    FOREIGN KEY (space_id) REFERENCES space(space_id) ON DELETE CASCADE
);

-- Tabla intermedia space_feature
CREATE TABLE IF NOT EXISTS space_feature (
  space_id    uuid NOT NULL,
  feature_id  smallint NOT NULL,
  PRIMARY KEY (space_id, feature_id),
  
  CONSTRAINT fk_space_feature_space
    FOREIGN KEY (space_id) REFERENCES space(space_id) ON DELETE CASCADE,
  CONSTRAINT fk_space_feature_feature
    FOREIGN KEY (feature_id) REFERENCES feature(feature_id) ON DELETE RESTRICT
);

-- Tabla de horarios de espacios
CREATE TABLE IF NOT EXISTS space_schedule (
  schedule_id  bigserial PRIMARY KEY,
  space_id     uuid NOT NULL,
  weekday      smallint NOT NULL, -- 0=domingo, 1=lunes, ..., 6=sábado
  time_from    time NOT NULL,
  time_to      time NOT NULL,
  
  CONSTRAINT fk_space_schedule_space
    FOREIGN KEY (space_id) REFERENCES space(space_id) ON DELETE CASCADE
);

-- Tabla de cierres de espacios
CREATE TABLE IF NOT EXISTS space_closure (
  closure_id   bigserial PRIMARY KEY,
  space_id     uuid NOT NULL,
  reason       text,
  starts_at    timestamptz NOT NULL,
  ends_at      timestamptz NOT NULL,
  
  CONSTRAINT fk_space_closure_space
    FOREIGN KEY (space_id) REFERENCES space(space_id) ON DELETE CASCADE
);

-- Tabla de tarifas/precios por espacio
CREATE TABLE IF NOT EXISTS space_rate (
  rate_id         bigserial PRIMARY KEY,
  space_id        uuid NOT NULL,
  name            text NOT NULL DEFAULT 'Base',
  unit            text NOT NULL, -- 'HOUR', 'DAY', 'BLOCK'
  block_minutes   int NOT NULL DEFAULT 60,
  price           numeric(12,2) NOT NULL,
  currency        char(3) NOT NULL DEFAULT 'CRC',
  applies_from    date NOT NULL DEFAULT current_date,
  applies_to      date,
  active          boolean NOT NULL DEFAULT true,
  created_at      timestamptz NOT NULL DEFAULT now(),
  updated_at      timestamptz NOT NULL DEFAULT now(),
  
  CONSTRAINT fk_space_rate_space
    FOREIGN KEY (space_id) REFERENCES space(space_id) ON DELETE CASCADE
);

-- *** TABLA REVIEW (MUY IMPORTANTE PARA EL CONTROLADOR) ***
CREATE TABLE IF NOT EXISTS review (
  review_id      bigserial PRIMARY KEY,
  space_id       uuid NOT NULL,
  user_id        uuid NOT NULL,
  reservation_id uuid,
  rating         smallint NOT NULL CHECK (rating >= 1 AND rating <= 5),
  comment        text,
  visible        boolean NOT NULL DEFAULT true,
  created_at     timestamptz NOT NULL DEFAULT now(),
  approved_at    timestamptz,
  
  CONSTRAINT fk_review_space
    FOREIGN KEY (space_id) REFERENCES space(space_id) ON DELETE CASCADE,
  CONSTRAINT fk_review_user
    FOREIGN KEY (user_id) REFERENCES app_user(user_id) ON DELETE RESTRICT,
  CONSTRAINT fk_review_reservation
    FOREIGN KEY (reservation_id) REFERENCES reservation(reservation_id) ON DELETE SET NULL
);

-- ===========================
-- CREAR ÍNDICES PARA OPTIMIZACIÓN
-- ===========================

-- Índices para space_image
CREATE INDEX IF NOT EXISTS idx_space_image ON space_image (space_id);

-- Índices para space_schedule
CREATE INDEX IF NOT EXISTS idx_space_schedule_weekday ON space_schedule (space_id, weekday);

-- Índices para space_rate
CREATE INDEX IF NOT EXISTS idx_space_rate_active ON space_rate (space_id, active);
CREATE INDEX IF NOT EXISTS idx_space_rate_validity ON space_rate (space_id, applies_from);

-- Índices para review (MUY IMPORTANTES PARA PERFORMANCE)
CREATE INDEX IF NOT EXISTS idx_review_space ON review (space_id);
CREATE INDEX IF NOT EXISTS idx_review_user ON review (user_id);
CREATE UNIQUE INDEX IF NOT EXISTS idx_review_reservation ON review (reservation_id);
CREATE INDEX IF NOT EXISTS idx_review_rating ON review (rating);
CREATE INDEX IF NOT EXISTS idx_review_visible ON review (visible);
CREATE INDEX IF NOT EXISTS idx_review_created ON review (created_at);

-- ===========================
-- INSERTAR DATOS DE PRUEBA
-- ===========================

-- Insertar algunas características básicas
INSERT INTO feature (name, description) VALUES
('WiFi', 'Conexión a internet inalámbrica'),
('Aire Acondicionado', 'Sistema de climatización'),
('Proyector', 'Equipo de proyección'),
('Sonido', 'Sistema de audio'),
('Cocina', 'Área de preparación de alimentos'),
('Estacionamiento', 'Área de parqueo disponible'),
('Piscina', 'Área de natación'),
('Jardín', 'Área verde exterior')
ON CONFLICT (name) DO NOTHING;

-- Insertar una tarifa básica para cada espacio existente
INSERT INTO space_rate (space_id, name, unit, price, currency)
SELECT 
    space_id,
    'Tarifa Base',
    'HOUR',
    5000.00,
    'CRC'
FROM space
WHERE NOT EXISTS (
    SELECT 1 FROM space_rate sr WHERE sr.space_id = space.space_id
);

-- ===========================
-- VERIFICACIÓN DE TABLAS CREADAS
-- ===========================

-- Mostrar todas las tablas del esquema público
SELECT 
    'Tablas creadas:' as info,
    '' as schemaname,
    '' as tablename
UNION ALL
SELECT 
    '',
    schemaname,
    tablename
FROM pg_tables 
WHERE schemaname = 'public'
ORDER BY tablename;

-- Mostrar el conteo de registros en tablas principales
SELECT 
    'Conteo de registros:' as tabla, 
    '' as registros
UNION ALL
SELECT 'app_user', COUNT(*)::text FROM app_user
UNION ALL
SELECT 'space', COUNT(*)::text FROM space
UNION ALL
SELECT 'space_type', COUNT(*)::text FROM space_type
UNION ALL
SELECT 'role', COUNT(*)::text FROM role
UNION ALL
SELECT 'reservation', COUNT(*)::text FROM reservation
UNION ALL
SELECT 'review', COUNT(*)::text FROM review
UNION ALL
SELECT 'feature', COUNT(*)::text FROM feature
UNION ALL
SELECT 'space_rate', COUNT(*)::text FROM space_rate
UNION ALL
SELECT 'space_image', COUNT(*)::text FROM space_image
UNION ALL
SELECT 'space_schedule', COUNT(*)::text FROM space_schedule;