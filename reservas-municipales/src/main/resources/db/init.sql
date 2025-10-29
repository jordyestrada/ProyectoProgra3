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

-- =========================
-- Datos iniciales básicos
-- =========================

-- Roles básicos (necesarios para autenticación Azure AD)
INSERT INTO role (code, name) VALUES
('ADMIN', 'Administrador'),
('SUPERVISOR', 'Supervisor'),
('USER', 'Usuario')
ON CONFLICT (code) DO NOTHING;

COMMIT;