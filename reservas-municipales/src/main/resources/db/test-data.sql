-- =========================================================
-- DATOS DE PRUEBA - Sistema de Reservas Municipales
-- =========================================================

BEGIN;

-- =========================
-- 1) ROLES
-- =========================
INSERT INTO role (code, name) VALUES
('ADMIN', 'Administrador'),
('USER', 'Usuario'),
('SUPERVISOR', 'Supervisor')
ON CONFLICT (code) DO NOTHING;

-- =========================
-- 2) USUARIOS DE PRUEBA
-- Password para todos: "admin123" (hash bcrypt)
-- =========================
INSERT INTO app_user (user_id, email, full_name, phone, password_hash, role_code, active) VALUES
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'admin@test.com', 'Administrador Sistema', '8888-8888', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN', true),
('b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'harolah26@gmail.com', 'Harold Hernández', '8765-4321', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'USER', true),
('c2eebc99-9c0b-4ef8-bb6d-6bb9bd380a33', 'usuario@test.com', 'Usuario Prueba', '8888-1111', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'USER', true),
('d3eebc99-9c0b-4ef8-bb6d-6bb9bd380a44', 'supervisor@test.com', 'Supervisor Municipal', '8888-2222', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'SUPERVISOR', true)
ON CONFLICT (user_id) DO NOTHING;

-- =========================
-- 3) TIPOS DE ESPACIOS
-- =========================
INSERT INTO space_type (space_type_id, name, description) VALUES
(1, 'Salón de Eventos', 'Espacios cerrados para eventos sociales y corporativos'),
(2, 'Cancha Deportiva', 'Instalaciones deportivas al aire libre'),
(3, 'Parque Recreativo', 'Áreas verdes para actividades recreativas'),
(4, 'Auditorio', 'Espacios con capacidad para conferencias y presentaciones'),
(5, 'Piscina', 'Instalaciones acuáticas')
ON CONFLICT (space_type_id) DO NOTHING;

-- =========================
-- 4) ESPACIOS
-- =========================
INSERT INTO space (space_id, name, space_type_id, capacity, location, outdoor, active, description) VALUES
('e1234567-89ab-cdef-0123-456789abcdef', 'Salón Municipal Principal', 1, 150, 'Edificio Central, Planta 2', false, true, 'Amplio salón con aire acondicionado, sonido y proyector incluido'),
('e2234567-89ab-cdef-0123-456789abcdef', 'Cancha de Fútbol Norte', 2, 22, 'Complejo Deportivo Norte', true, true, 'Cancha de césped sintético con iluminación nocturna'),
('e3234567-89ab-cdef-0123-456789abcdef', 'Parque Central', 3, 200, 'Centro de la Ciudad', true, true, 'Espacio verde con juegos infantiles y áreas de picnic'),
('e4234567-89ab-cdef-0123-456789abcdef', 'Auditorio Municipal', 4, 300, 'Casa de la Cultura', false, true, 'Auditorio equipado con sistema de sonido profesional y escenario'),
('e5234567-89ab-cdef-0123-456789abcdef', 'Piscina Olímpica', 5, 50, 'Complejo Deportivo Sur', true, true, 'Piscina de 50 metros con carriles profesionales')
ON CONFLICT (space_id) DO NOTHING;

-- =========================
-- 5) CARACTERÍSTICAS
-- =========================
INSERT INTO feature (feature_id, name, description) VALUES
(1, 'Wi-Fi', 'Conexión inalámbrica a internet'),
(2, 'Aire Acondicionado', 'Sistema de climatización'),
(3, 'Proyector', 'Equipo de proyección multimedia'),
(4, 'Sonido', 'Sistema de audio profesional'),
(5, 'Estacionamiento', 'Área de parqueo disponible'),
(6, 'Acceso Discapacitados', 'Instalaciones accesibles'),
(7, 'Cocina', 'Área de preparación de alimentos'),
(8, 'Iluminación Nocturna', 'Luces para uso nocturno'),
(9, 'Vestidores', 'Camerinos con duchas'),
(10, 'Gradas', 'Área para espectadores')
ON CONFLICT (feature_id) DO NOTHING;

-- =========================
-- 6) ASIGNACIÓN DE CARACTERÍSTICAS A ESPACIOS
-- =========================
INSERT INTO space_feature (space_id, feature_id) VALUES
-- Salón Municipal
('e1234567-89ab-cdef-0123-456789abcdef', 1),  -- Wi-Fi
('e1234567-89ab-cdef-0123-456789abcdef', 2),  -- Aire Acondicionado
('e1234567-89ab-cdef-0123-456789abcdef', 3),  -- Proyector
('e1234567-89ab-cdef-0123-456789abcdef', 4),  -- Sonido
('e1234567-89ab-cdef-0123-456789abcdef', 5),  -- Estacionamiento
('e1234567-89ab-cdef-0123-456789abcdef', 6),  -- Acceso Discapacitados
('e1234567-89ab-cdef-0123-456789abcdef', 7),  -- Cocina

-- Cancha de Fútbol
('e2234567-89ab-cdef-0123-456789abcdef', 8),  -- Iluminación Nocturna
('e2234567-89ab-cdef-0123-456789abcdef', 9),  -- Vestidores
('e2234567-89ab-cdef-0123-456789abcdef', 10), -- Gradas
('e2234567-89ab-cdef-0123-456789abcdef', 5),  -- Estacionamiento

-- Parque Central
('e3234567-89ab-cdef-0123-456789abcdef', 5),  -- Estacionamiento
('e3234567-89ab-cdef-0123-456789abcdef', 6),  -- Acceso Discapacitados

-- Auditorio
('e4234567-89ab-cdef-0123-456789abcdef', 1),  -- Wi-Fi
('e4234567-89ab-cdef-0123-456789abcdef', 2),  -- Aire Acondicionado
('e4234567-89ab-cdef-0123-456789abcdef', 3),  -- Proyector
('e4234567-89ab-cdef-0123-456789abcdef', 4),  -- Sonido
('e4234567-89ab-cdef-0123-456789abcdef', 5),  -- Estacionamiento
('e4234567-89ab-cdef-0123-456789abcdef', 6),  -- Acceso Discapacitados

-- Piscina
('e5234567-89ab-cdef-0123-456789abcdef', 9),  -- Vestidores
('e5234567-89ab-cdef-0123-456789abcdef', 10), -- Gradas
('e5234567-89ab-cdef-0123-456789abcdef', 5)   -- Estacionamiento
ON CONFLICT (space_id, feature_id) DO NOTHING;

-- =========================
-- 7) HORARIOS DE ESPACIOS
-- Lunes a Viernes: 0=Lunes, 4=Viernes
-- Sábado: 5, Domingo: 6
-- =========================

-- Salón Municipal: Lunes a Sábado 8:00-22:00
INSERT INTO space_schedule (space_id, weekday, time_from, time_to) VALUES
('e1234567-89ab-cdef-0123-456789abcdef', 1, '08:00', '22:00'),
('e1234567-89ab-cdef-0123-456789abcdef', 2, '08:00', '22:00'),
('e1234567-89ab-cdef-0123-456789abcdef', 3, '08:00', '22:00'),
('e1234567-89ab-cdef-0123-456789abcdef', 4, '08:00', '22:00'),
('e1234567-89ab-cdef-0123-456789abcdef', 5, '08:00', '22:00'),
('e1234567-89ab-cdef-0123-456789abcdef', 6, '08:00', '20:00');

-- Cancha de Fútbol: Todos los días 06:00-23:00
INSERT INTO space_schedule (space_id, weekday, time_from, time_to) VALUES
('e2234567-89ab-cdef-0123-456789abcdef', 0, '06:00', '23:00'),
('e2234567-89ab-cdef-0123-456789abcdef', 1, '06:00', '23:00'),
('e2234567-89ab-cdef-0123-456789abcdef', 2, '06:00', '23:00'),
('e2234567-89ab-cdef-0123-456789abcdef', 3, '06:00', '23:00'),
('e2234567-89ab-cdef-0123-456789abcdef', 4, '06:00', '23:00'),
('e2234567-89ab-cdef-0123-456789abcdef', 5, '06:00', '23:00'),
('e2234567-89ab-cdef-0123-456789abcdef', 6, '06:00', '23:00');

-- Parque Central: 24/7
INSERT INTO space_schedule (space_id, weekday, time_from, time_to) VALUES
('e3234567-89ab-cdef-0123-456789abcdef', 0, '00:00', '23:59'),
('e3234567-89ab-cdef-0123-456789abcdef', 1, '00:00', '23:59'),
('e3234567-89ab-cdef-0123-456789abcdef', 2, '00:00', '23:59'),
('e3234567-89ab-cdef-0123-456789abcdef', 3, '00:00', '23:59'),
('e3234567-89ab-cdef-0123-456789abcdef', 4, '00:00', '23:59'),
('e3234567-89ab-cdef-0123-456789abcdef', 5, '00:00', '23:59'),
('e3234567-89ab-cdef-0123-456789abcdef', 6, '00:00', '23:59');

-- Auditorio: Lunes a Domingo 07:00-22:00
INSERT INTO space_schedule (space_id, weekday, time_from, time_to) VALUES
('e4234567-89ab-cdef-0123-456789abcdef', 0, '07:00', '22:00'),
('e4234567-89ab-cdef-0123-456789abcdef', 1, '07:00', '22:00'),
('e4234567-89ab-cdef-0123-456789abcdef', 2, '07:00', '22:00'),
('e4234567-89ab-cdef-0123-456789abcdef', 3, '07:00', '22:00'),
('e4234567-89ab-cdef-0123-456789abcdef', 4, '07:00', '22:00'),
('e4234567-89ab-cdef-0123-456789abcdef', 5, '07:00', '22:00'),
('e4234567-89ab-cdef-0123-456789abcdef', 6, '07:00', '22:00');

-- Piscina: Martes a Domingo 09:00-18:00 (cerrado lunes)
INSERT INTO space_schedule (space_id, weekday, time_from, time_to) VALUES
('e5234567-89ab-cdef-0123-456789abcdef', 2, '09:00', '18:00'),
('e5234567-89ab-cdef-0123-456789abcdef', 3, '09:00', '18:00'),
('e5234567-89ab-cdef-0123-456789abcdef', 4, '09:00', '18:00'),
('e5234567-89ab-cdef-0123-456789abcdef', 5, '09:00', '18:00'),
('e5234567-89ab-cdef-0123-456789abcdef', 6, '09:00', '18:00'),
('e5234567-89ab-cdef-0123-456789abcdef', 0, '09:00', '18:00');

-- =========================
-- 8) TARIFAS
-- =========================
INSERT INTO space_rate (space_id, name, unit, block_minutes, price, currency, applies_from, active) VALUES
-- Salón Municipal
('e1234567-89ab-cdef-0123-456789abcdef', 'Tarifa por Hora', 'HORA', 60, 15000.00, 'CRC', '2025-01-01', true),
('e1234567-89ab-cdef-0123-456789abcdef', 'Tarifa por Día', 'DIA', 1440, 80000.00, 'CRC', '2025-01-01', true),

-- Cancha de Fútbol
('e2234567-89ab-cdef-0123-456789abcdef', 'Tarifa por Hora', 'HORA', 60, 10000.00, 'CRC', '2025-01-01', true),
('e2234567-89ab-cdef-0123-456789abcdef', 'Tarifa Medio Día (4h)', 'BLOQUE', 240, 35000.00, 'CRC', '2025-01-01', true),

-- Parque Central (Gratis)
('e3234567-89ab-cdef-0123-456789abcdef', 'Gratuito', 'DIA', 1440, 0.00, 'CRC', '2025-01-01', true),

-- Auditorio
('e4234567-89ab-cdef-0123-456789abcdef', 'Tarifa por Hora', 'HORA', 60, 25000.00, 'CRC', '2025-01-01', true),
('e4234567-89ab-cdef-0123-456789abcdef', 'Tarifa por Día', 'DIA', 1440, 150000.00, 'CRC', '2025-01-01', true),

-- Piscina
('e5234567-89ab-cdef-0123-456789abcdef', 'Entrada Individual', 'HORA', 120, 5000.00, 'CRC', '2025-01-01', true),
('e5234567-89ab-cdef-0123-456789abcdef', 'Reserva Completa', 'DIA', 1440, 200000.00, 'CRC', '2025-01-01', true);

COMMIT;

-- =========================================================
-- RESUMEN DE USUARIOS DE PRUEBA:
-- =========================================================
-- Email: admin@test.com          | Password: admin123 | Role: ADMIN
-- Email: harolah26@gmail.com     | Password: admin123 | Role: USER
-- Email: usuario@test.com        | Password: admin123 | Role: USER
-- Email: supervisor@test.com     | Password: admin123 | Role: SUPERVISOR
-- =========================================================
