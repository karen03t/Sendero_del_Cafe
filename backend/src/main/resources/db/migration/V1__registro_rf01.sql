-- V1 · RF-01 Registro de turista (MySQL 8)
-- Trazabilidad: RN-06 (correo único + al menos un interés), CA-02, RNF-02 (Ley 1581)

CREATE TABLE turista (
  id VARCHAR(36) NOT NULL,
  nombre_completo VARCHAR(200) NOT NULL,
  correo VARCHAR(255) NOT NULL,
  password_hash VARCHAR(255) NULL,
  proveedor VARCHAR(10) NOT NULL DEFAULT 'LOCAL',
  proveedor_id VARCHAR(255) NULL,
  acepta_tratamiento TINYINT(1) NOT NULL DEFAULT 0,
  fecha_consentimiento DATETIME NULL,
  estado VARCHAR(10) NOT NULL DEFAULT 'ACTIVO',
  fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_turista_correo (correo),
  CONSTRAINT chk_turista_auth CHECK (
    (proveedor = 'LOCAL' AND password_hash IS NOT NULL)
    OR (proveedor <> 'LOCAL' AND proveedor_id IS NOT NULL AND password_hash IS NULL)
  ),
  CONSTRAINT chk_turista_consent CHECK (acepta_tratamiento = 1),
  CONSTRAINT chk_turista_proveedor CHECK (proveedor IN ('LOCAL','GOOGLE','APPLE')),
  CONSTRAINT chk_turista_estado CHECK (estado IN ('ACTIVO','INACTIVO'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE interes (
  id INT NOT NULL AUTO_INCREMENT,
  nombre VARCHAR(100) NOT NULL,
  activo TINYINT(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (id),
  UNIQUE KEY uq_interes_nombre (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE turista_interes (
  turista_id VARCHAR(36) NOT NULL,
  interes_id INT NOT NULL,
  fecha_asignacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (turista_id, interes_id),
  CONSTRAINT fk_ti_turista FOREIGN KEY (turista_id) REFERENCES turista (id) ON DELETE RESTRICT,
  CONSTRAINT fk_ti_interes FOREIGN KEY (interes_id) REFERENCES interes (id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT IGNORE INTO interes (nombre) VALUES
  ('ecológico'), ('gastronómico'), ('local');
