-- V4 · RF-03 Generar, personalizar y guardar rutas turísticas
-- Trazabilidad: SCRUM-30, SCRUM-8
-- Tablas: ruta, ruta_lugar, ruta_guardada

-- 1. Ruta generada por el sistema
CREATE TABLE ruta (
  id              VARCHAR(36)  NOT NULL,
  nombre          VARCHAR(200) NOT NULL,
  descripcion     TEXT         NULL,
  municipio       VARCHAR(100) NOT NULL,
  distancia_km    DECIMAL(8,2) NULL,
  duracion_min    INT          NULL,
  estado          VARCHAR(10)  NOT NULL DEFAULT 'ACTIVO',
  fecha_creacion  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  CONSTRAINT chk_ruta_estado CHECK (estado IN ('ACTIVO','INACTIVO'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Lugares que componen una ruta (orden de visita)
CREATE TABLE ruta_lugar (
  ruta_id   VARCHAR(36) NOT NULL,
  lugar_id  VARCHAR(36) NOT NULL,
  orden     INT         NOT NULL,
  tiempo_estimado_min INT NULL,
  PRIMARY KEY (ruta_id, lugar_id),
  CONSTRAINT fk_rl_ruta  FOREIGN KEY (ruta_id)  REFERENCES ruta (id)  ON DELETE CASCADE,
  CONSTRAINT fk_rl_lugar FOREIGN KEY (lugar_id) REFERENCES lugar (id) ON DELETE RESTRICT,
  UNIQUE KEY uq_ruta_orden (ruta_id, orden)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Rutas guardadas por el turista
CREATE TABLE ruta_guardada (
  turista_id    VARCHAR(36) NOT NULL,
  ruta_id       VARCHAR(36) NOT NULL,
  nombre_custom VARCHAR(200) NULL,
  fecha_guardado DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (turista_id, ruta_id),
  CONSTRAINT fk_rg_turista FOREIGN KEY (turista_id) REFERENCES turista (id) ON DELETE CASCADE,
  CONSTRAINT fk_rg_ruta    FOREIGN KEY (ruta_id)    REFERENCES ruta (id)    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Índices para consultas frecuentes
CREATE INDEX idx_ruta_municipio ON ruta (municipio);
CREATE INDEX idx_ruta_estado ON ruta (estado);

