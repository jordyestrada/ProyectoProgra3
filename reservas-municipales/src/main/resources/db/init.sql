-- =========================================================
-- Municipal Reservations (EN) - Simplified
-- Reduced model + Prices + Reviews
-- Direct translation from DBML to PostgreSQL
-- =========================================================

BEGIN;

-- Required extensions
CREATE EXTENSION IF NOT EXISTS citext;
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- =========================
-- 1) Simple security
-- =========================

CREATE TABLE role (
  code     text PRIMARY KEY,
  name     text NOT NULL,
  created_at  timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE app_user (
  user_id       uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  email         citext NOT NULL UNIQUE,
  full_name     text NOT NULL,
  phone         text,
  password_hash text,                        -- Para autenticación local
  role_code     text NOT NULL,
  active        boolean NOT NULL DEFAULT true,
  created_at    timestamptz NOT NULL DEFAULT now(),
  updated_at    timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_app_user_active ON app_user (active);

-- =========================
-- 2) Catalogs and spaces
-- =========================

CREATE TABLE space_type (
  space_type_id  smallserial PRIMARY KEY,
  name           text NOT NULL UNIQUE,
  description    text
);

CREATE TABLE space (
  space_id       uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  name           text NOT NULL UNIQUE,
  space_type_id  smallint NOT NULL,
  capacity       int NOT NULL,
  location       text,
  outdoor        boolean NOT NULL DEFAULT false,
  active         boolean NOT NULL DEFAULT true,
  description    text,
  created_at     timestamptz NOT NULL DEFAULT now(),
  updated_at     timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_space_active ON space (active);
CREATE INDEX idx_space_type   ON space (space_type_id);

CREATE TABLE space_image (
  image_id   bigserial PRIMARY KEY,
  space_id   uuid NOT NULL,
  url        text NOT NULL,
  main       boolean NOT NULL DEFAULT false,
  ord        int NOT NULL DEFAULT 0,
  created_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_space_image ON space_image (space_id);

CREATE TABLE feature (
  feature_id smallserial PRIMARY KEY,
  name       text NOT NULL UNIQUE,
  description text
);

CREATE TABLE space_feature (
  space_id    uuid NOT NULL,
  feature_id  smallint NOT NULL,
  PRIMARY KEY (space_id, feature_id)
);

CREATE TABLE space_schedule (
  schedule_id  bigserial PRIMARY KEY,
  space_id     uuid NOT NULL,
  weekday      smallint NOT NULL,
  time_from    time NOT NULL,
  time_to      time NOT NULL
);

CREATE INDEX idx_space_schedule_weekday ON space_schedule (space_id, weekday);

CREATE TABLE space_closure (
  closure_id   bigserial PRIMARY KEY,
  space_id     uuid NOT NULL,
  reason        text,
  starts_at     timestamptz NOT NULL,
  ends_at       timestamptz NOT NULL
);

-- =========================
-- 2.1) Prices (Rates per space)
-- =========================

CREATE TABLE space_rate (
  rate_id         bigserial PRIMARY KEY,
  space_id        uuid NOT NULL,
  name            text NOT NULL DEFAULT 'Base',
  unit            text NOT NULL,
  block_minutes   int NOT NULL DEFAULT 60,
  price           numeric(12,2) NOT NULL,
  currency        char(3) NOT NULL DEFAULT 'CRC',
  applies_from    date NOT NULL DEFAULT current_date,
  applies_to      date,
  active          boolean NOT NULL DEFAULT true,
  created_at      timestamptz NOT NULL DEFAULT now(),
  updated_at      timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_space_rate_active ON space_rate (space_id, active);
CREATE INDEX idx_space_rate_validity ON space_rate (space_id, applies_from);

-- =========================
-- 3) Reservations (state + price snapshot)
-- =========================

CREATE TABLE reservation (
  reservation_id      uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  space_id            uuid NOT NULL,
  user_id             uuid NOT NULL,
  starts_at           timestamptz NOT NULL,
  ends_at             timestamptz NOT NULL,
  status              text NOT NULL,
  cancel_reason       text,

  rate_id             bigint,
  total_amount        numeric(12,2),
  currency            char(3) NOT NULL DEFAULT 'CRC',

  created_at          timestamptz NOT NULL DEFAULT now(),
  updated_at          timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_reservation_space_time ON reservation (space_id, starts_at, ends_at);
CREATE INDEX idx_reservation_user_time ON reservation (user_id, starts_at);
CREATE INDEX idx_reservation_rate ON reservation (rate_id);

-- =========================
-- 4) Reviews (Space reviews)
-- =========================

CREATE TABLE review (
  review_id     bigserial PRIMARY KEY,
  space_id      uuid NOT NULL,
  user_id       uuid NOT NULL,
  reservation_id uuid,
  rating        smallint NOT NULL,
  comment       text,
  visible       boolean NOT NULL DEFAULT true,
  created_at    timestamptz NOT NULL DEFAULT now(),
  approved_at   timestamptz
);

CREATE INDEX idx_review_space ON review (space_id);
CREATE INDEX idx_review_user ON review (user_id);
CREATE UNIQUE INDEX idx_review_reservation ON review (reservation_id);
CREATE INDEX idx_review_rating ON review (rating);

-- =========================
-- Foreign keys
-- =========================

ALTER TABLE app_user
  ADD CONSTRAINT fk_app_user_role
  FOREIGN KEY (role_code) REFERENCES role(code) ON DELETE RESTRICT;

ALTER TABLE space
  ADD CONSTRAINT fk_space_type
  FOREIGN KEY (space_type_id) REFERENCES space_type(space_type_id);

ALTER TABLE space_image
  ADD CONSTRAINT fk_space_image
  FOREIGN KEY (space_id) REFERENCES space(space_id) ON DELETE CASCADE;

ALTER TABLE space_feature
  ADD CONSTRAINT fk_space_feature_space
  FOREIGN KEY (space_id) REFERENCES space(space_id) ON DELETE CASCADE;

ALTER TABLE space_feature
  ADD CONSTRAINT fk_space_feature_feature
  FOREIGN KEY (feature_id) REFERENCES feature(feature_id) ON DELETE RESTRICT;

ALTER TABLE space_schedule
  ADD CONSTRAINT fk_space_schedule_space
  FOREIGN KEY (space_id) REFERENCES space(space_id) ON DELETE CASCADE;

ALTER TABLE space_closure
  ADD CONSTRAINT fk_space_closure_space
  FOREIGN KEY (space_id) REFERENCES space(space_id) ON DELETE CASCADE;

ALTER TABLE space_rate
  ADD CONSTRAINT fk_space_rate_space
  FOREIGN KEY (space_id) REFERENCES space(space_id) ON DELETE CASCADE;

ALTER TABLE reservation
  ADD CONSTRAINT fk_reservation_space
  FOREIGN KEY (space_id) REFERENCES space(space_id) ON DELETE RESTRICT;

ALTER TABLE reservation
  ADD CONSTRAINT fk_reservation_user
  FOREIGN KEY (user_id) REFERENCES app_user(user_id) ON DELETE RESTRICT;

ALTER TABLE reservation
  ADD CONSTRAINT fk_reservation_rate
  FOREIGN KEY (rate_id) REFERENCES space_rate(rate_id) ON DELETE SET NULL;

ALTER TABLE review
  ADD CONSTRAINT fk_review_space
  FOREIGN KEY (space_id) REFERENCES space(space_id) ON DELETE CASCADE;

ALTER TABLE review
  ADD CONSTRAINT fk_review_user
  FOREIGN KEY (user_id) REFERENCES app_user(user_id) ON DELETE RESTRICT;

ALTER TABLE review
  ADD CONSTRAINT fk_review_reservation
  FOREIGN KEY (reservation_id) REFERENCES reservation(reservation_id) ON DELETE SET NULL;

COMMIT;
-- =========================================================
-- Reservas Municipales (ES) - Simplificado
-- Modelo reducido + Precios + Reviews
-- Traducción directa del DBML a PostgreSQL
-- =========================================================

BEGIN;

-- Extensiones necesarias
CREATE EXTENSION IF NOT EXISTS citext;
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- =========================
-- 1) Seguridad simple
-- =========================

CREATE TABLE rol (
  codigo     text PRIMARY KEY,               -- 'ADMIN', 'SUPERVISOR', 'USUARIO'
  nombre     text NOT NULL,
  creado_en  timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE usuario (
  id_usuario       uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  correo           citext NOT NULL UNIQUE,
  nombre_completo  text NOT NULL,
  telefono         text,
  rol_codigo       text NOT NULL,            -- FK -> rol.codigo
  activo           boolean NOT NULL DEFAULT true,
  creado_en        timestamptz NOT NULL DEFAULT now(),
  actualizado_en   timestamptz NOT NULL DEFAULT now()
);

-- Índices
CREATE INDEX idx_usuario_activo ON usuario (activo);

-- =========================
-- 2) Catálogos y espacios
-- =========================

CREATE TABLE tipo_espacio (
  id_tipo_espacio  smallserial PRIMARY KEY,
  nombre           text NOT NULL UNIQUE,
  descripcion      text
);

CREATE TABLE espacio (
  id_espacio       uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  nombre           text NOT NULL UNIQUE,
  id_tipo_espacio  smallint NOT NULL,
  capacidad        int NOT NULL,                 -- Validar > 0 en app/BD
  ubicacion        text,
  exterior         boolean NOT NULL DEFAULT false, -- al aire libre
  activo           boolean NOT NULL DEFAULT true,
  descripcion      text,
  creado_en        timestamptz NOT NULL DEFAULT now(),
  actualizado_en   timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_espacio_activo ON espacio (activo);
CREATE INDEX idx_espacio_tipo   ON espacio (id_tipo_espacio);

CREATE TABLE imagen_espacio (
  id_imagen   bigserial PRIMARY KEY,
  id_espacio  uuid NOT NULL,
  url         text NOT NULL,
  principal   boolean NOT NULL DEFAULT false,
  orden       int NOT NULL DEFAULT 0,
  creado_en   timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_imagen_espacio ON imagen_espacio (id_espacio);

-- (Opcional) Características
CREATE TABLE caracteristica (
  id_caracteristica smallserial PRIMARY KEY,
  nombre            text NOT NULL UNIQUE,
  descripcion       text
);

CREATE TABLE espacio_caracteristica (
  id_espacio        uuid NOT NULL,
  id_caracteristica smallint NOT NULL,
  PRIMARY KEY (id_espacio, id_caracteristica)
);

-- Horarios y cierres
CREATE TABLE horario_espacio (
  id_horario  bigserial PRIMARY KEY,
  id_espacio  uuid NOT NULL,
  dia_semana  smallint NOT NULL,         -- 0=domingo .. 6=sábado
  hora_desde  time NOT NULL,
  hora_hasta  time NOT NULL
);

CREATE INDEX idx_horario_espacio_dia ON horario_espacio (id_espacio, dia_semana);

CREATE TABLE cierre_espacio (
  id_cierre   bigserial PRIMARY KEY,
  id_espacio  uuid NOT NULL,
  motivo      text,
  inicia_en   timestamptz NOT NULL,
  termina_en  timestamptz NOT NULL
);

-- =========================
-- 2.1) Precios (Tarifas por espacio)
-- =========================

CREATE TABLE tarifa_espacio (
  id_tarifa       bigserial PRIMARY KEY,
  id_espacio      uuid NOT NULL,
  nombre          text NOT NULL DEFAULT 'Base',
  unidad          text NOT NULL,                 -- HORA | DIA | BLOQUE
  minutos_bloque  int NOT NULL DEFAULT 60,       -- usado si unidad = BLOQUE
  precio          numeric(12,2) NOT NULL,
  moneda          char(3) NOT NULL DEFAULT 'CRC',-- ISO 4217
  aplica_desde    date NOT NULL DEFAULT current_date,
  aplica_hasta    date,                          -- null = vigente
  activo          boolean NOT NULL DEFAULT true,
  creado_en       timestamptz NOT NULL DEFAULT now(),
  actualizado_en  timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_tarifa_espacio_activo ON tarifa_espacio (id_espacio, activo);
CREATE INDEX idx_tarifa_vigencia       ON tarifa_espacio (id_espacio, aplica_desde);

-- =========================
-- 3) Reservas (estado + snapshot de precio)
-- =========================

CREATE TABLE reserva (
  id_reserva          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  id_espacio          uuid NOT NULL,
  id_usuario          uuid NOT NULL,
  inicia_en           timestamptz NOT NULL,
  termina_en          timestamptz NOT NULL,
  estado              text NOT NULL,                 -- PENDIENTE|CONFIRMADA|CANCELADA|ASISTIO|NO_ASISTIO
  motivo_cancelacion  text,                          -- opcional; solo si estado = CANCELADA

  -- Snapshot de precio
  id_tarifa           bigint,                        -- FK opcional a tarifa_espacio
  monto_total         numeric(12,2),                 -- total calculado y congelado
  moneda              char(3) NOT NULL DEFAULT 'CRC',

  creado_en           timestamptz NOT NULL DEFAULT now(),
  actualizado_en      timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_reserva_espacio_tiempo ON reserva (id_espacio, inicia_en, termina_en);
CREATE INDEX idx_reserva_usuario_tiempo ON reserva (id_usuario, inicia_en);
CREATE INDEX idx_reserva_tarifa         ON reserva (id_tarifa);

-- =========================
-- 4) Reviews (Reseñas de espacios)
-- =========================

CREATE TABLE resena (
  id_resena     bigserial PRIMARY KEY,
  id_espacio    uuid NOT NULL,
  id_usuario    uuid NOT NULL,
  id_reserva    uuid,                     -- opcional (si la reseña proviene de una reserva)
  calificacion  smallint NOT NULL,        -- rango 1..5 (validar en app/BD)
  comentario    text,
  visible       boolean NOT NULL DEFAULT true, -- moderación simple
  creado_en     timestamptz NOT NULL DEFAULT now(),
  aprobado_en   timestamptz                 -- opcional
);

CREATE INDEX      idx_resena_espacio      ON resena (id_espacio);
CREATE INDEX      idx_resena_usuario      ON resena (id_usuario);
CREATE UNIQUE INDEX idx_resena_reserva    ON resena (id_reserva); -- una reseña por reserva
CREATE INDEX      idx_resena_calificacion ON resena (calificacion);

-- =========================
-- Relaciones (FKs)
-- =========================

-- Seguridad
ALTER TABLE usuario
  ADD CONSTRAINT fk_usuario_rol
  FOREIGN KEY (rol_codigo) REFERENCES rol(codigo) ON DELETE RESTRICT;

-- Catálogos y espacios
ALTER TABLE espacio
  ADD CONSTRAINT fk_espacio_tipo
  FOREIGN KEY (id_tipo_espacio) REFERENCES tipo_espacio(id_tipo_espacio);

ALTER TABLE imagen_espacio
  ADD CONSTRAINT fk_imagen_espacio
  FOREIGN KEY (id_espacio) REFERENCES espacio(id_espacio) ON DELETE CASCADE;

ALTER TABLE espacio_caracteristica
  ADD CONSTRAINT fk_esp_carac_espacio
  FOREIGN KEY (id_espacio) REFERENCES espacio(id_espacio) ON DELETE CASCADE;

ALTER TABLE espacio_caracteristica
  ADD CONSTRAINT fk_esp_carac_caracteristica
  FOREIGN KEY (id_caracteristica) REFERENCES caracteristica(id_caracteristica) ON DELETE RESTRICT;

ALTER TABLE horario_espacio
  ADD CONSTRAINT fk_horario_espacio
  FOREIGN KEY (id_espacio) REFERENCES espacio(id_espacio) ON DELETE CASCADE;

ALTER TABLE cierre_espacio
  ADD CONSTRAINT fk_cierre_espacio
  FOREIGN KEY (id_espacio) REFERENCES espacio(id_espacio) ON DELETE CASCADE;

-- Precios
ALTER TABLE tarifa_espacio
  ADD CONSTRAINT fk_tarifa_espacio
  FOREIGN KEY (id_espacio) REFERENCES espacio(id_espacio) ON DELETE CASCADE;

-- Reservas
ALTER TABLE reserva
  ADD CONSTRAINT fk_reserva_espacio
  FOREIGN KEY (id_espacio) REFERENCES espacio(id_espacio) ON DELETE RESTRICT;

ALTER TABLE reserva
  ADD CONSTRAINT fk_reserva_usuario
  FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario) ON DELETE RESTRICT;

ALTER TABLE reserva
  ADD CONSTRAINT fk_reserva_tarifa
  FOREIGN KEY (id_tarifa) REFERENCES tarifa_espacio(id_tarifa) ON DELETE SET NULL;

-- Reviews
ALTER TABLE resena
  ADD CONSTRAINT fk_resena_espacio
  FOREIGN KEY (id_espacio) REFERENCES espacio(id_espacio) ON DELETE CASCADE;

ALTER TABLE resena
  ADD CONSTRAINT fk_resena_usuario
  FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario) ON DELETE RESTRICT;

ALTER TABLE resena
  ADD CONSTRAINT fk_resena_reserva
  FOREIGN KEY (id_reserva) REFERENCES reserva(id_reserva) ON DELETE SET NULL;

COMMIT;

-- =========================================================
-- Nota: Línea del DBML que NO es un FK válido en SQL
--       (mismas tablas/columnas no clave). La conservo
--       como comentario para fidelidad al enunciado:
-- Ref: "espacio"."ubicacion" < "espacio"."actualizado_en"
-- =========================================================
